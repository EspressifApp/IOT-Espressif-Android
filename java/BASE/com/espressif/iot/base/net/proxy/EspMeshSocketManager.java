package com.espressif.iot.base.net.proxy;

import java.util.ArrayList;
import java.util.List;

public class EspMeshSocketManager
{
    /*
     * Singleton lazy initialization start
     */
    private EspMeshSocketManager()
    {
        mMeshSocketList = new ArrayList<EspMeshSocket>();
    }
    
    private static class InstanceHolder
    {
        static EspMeshSocketManager instance = new EspMeshSocketManager();
    }
    
    public static EspMeshSocketManager getInstance()
    {
        return InstanceHolder.instance;
    }
    
    /*
     * Singleton lazy initialization end
     */
    
    private static final boolean DEBUG = true;
    private static final boolean USE_LOG4J = true;
    private static final Class<?> CLASS = EspMeshSocketManager.class;
    
    private static void print(String msg)
    {
        MeshLog.i(DEBUG, USE_LOG4J, CLASS, msg);
    }
    
    private volatile LoopCheckThread mLoopCheckThread;
    
    private List<EspMeshSocket> mMeshSocketList;
    
    public synchronized void start()
    {
        if (mLoopCheckThread == null)
        {
            print("start() start check loop");
            mLoopCheckThread = new LoopCheckThread();
            mLoopCheckThread.startThread();
        }
        else
        {
            print("start() check loop thread has started");
        }
    }
    
    public synchronized void stop()
    {
        if (mLoopCheckThread != null)
        {
            print("stop() stop check loop");
            mLoopCheckThread.stopThread();
            mLoopCheckThread = null;
        }
        else
        {
            print("stop() check loop thread is null");
        }
        mMeshSocketList.clear();
    }
    
    public synchronized void accept(EspProxyTask task)
    {
        EspMeshSocket taskSocket = null;
        String taskHostAddress = task.getTargetInetAddress().getHostAddress();
        synchronized (mMeshSocketList)
        {
            for (int i = 0; i < mMeshSocketList.size(); ++i)
            {
                EspMeshSocket socket = mMeshSocketList.get(i);
                String socketHostAddress = socket.getInetAddress().getHostAddress();
                if (socketHostAddress.equals(taskHostAddress))
                {
                    print("accept() task mesh socket exist: " + taskHostAddress);
                    taskSocket = socket;
                    taskSocket.offer(task);
                    break;
                }
            }
        }
        if (taskSocket == null)
        {
            print("accept() new a task mesh socket: " + taskHostAddress);
            taskSocket = new EspMeshSocketImpl(task.getTargetInetAddress());
            taskSocket.offer(task);
            mMeshSocketList.add(taskSocket);
            synchronized (mMeshSocketList)
            {
                mMeshSocketList.notify();
            }
        }
    }
    
    private class LoopCheckThread extends BlockingFinishThread
    {
        private volatile boolean mIsInterrupted;
        
        public LoopCheckThread()
        {
            mIsInterrupted = false;
        }
        
        @Override
        void startThreadsInit()
        {
        }

        @Override
        void endThreadsDestroy()
        {
            mIsInterrupted = true;
            interrupt();
        }

        @Override
        public void execute()
        {
            while (mIsStart)
            {
//                print("LoopCheckThread mMeshSocketList size = " + mMeshSocketList.size());
                synchronized (mMeshSocketList)
                {
                    if (mMeshSocketList.isEmpty())
                    {
                        try
                        {
                            mMeshSocketList.wait();
                        }
                        catch (InterruptedException e)
                        {
                            if (mIsInterrupted)
                            {
                                print("LoopCheckThread execute() is interrupted");
                                break;
                            }
                            else
                            {
                                e.printStackTrace();
                            }
                        }
                    }
                }
                
                for (int i = 0; i < mMeshSocketList.size(); i++)
                {
                    EspMeshSocket meshSocket = mMeshSocketList.get(i);
                    if (meshSocket.isClosed())
                    {
                        print("LoopCheckThread " + meshSocket.getInetAddress().getHostAddress()
                            + " is closed or expired");
                        synchronized (mMeshSocketList)
                        {
                            mMeshSocketList.remove(i--);
                        }
                        List<EspProxyTask> unExecutedTaskList = meshSocket.getRefreshProxyTaskList();
                        for (EspProxyTask task : unExecutedTaskList)
                        {
                            print("LoopCheckThread " + task + " is accept()");
                            accept(task);
                        }
                    }
                    else
                    {
                        if (meshSocket.isExpired())
                        {
                            print("LoopCheckThread " + meshSocket + " halfClose()");
                            meshSocket.halfClose();
                        }
                        if (meshSocket.isConnected())
                        {
//                            print("LoopCheckThread " + meshSocket + " checkProxyTaskStateAndProc()");
                            meshSocket.checkProxyTaskStateAndProc();
                        }
                    }
                }
                
                try
                {
                    Thread.sleep(100);
                }
                catch (InterruptedException e)
                {
                    if (mIsInterrupted)
                    {
                        print("LoopCheckThread execute() is interrupted");
                        break;
                    }
                    else
                    {
                        e.printStackTrace();
                    }
                }
            } // while end
            print("LoopCheckThread finish");
        }
    }
}
