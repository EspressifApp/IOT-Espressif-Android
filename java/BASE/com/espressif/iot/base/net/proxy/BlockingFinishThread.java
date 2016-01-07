package com.espressif.iot.base.net.proxy;

import java.util.concurrent.LinkedBlockingQueue;

abstract class BlockingFinishThread extends Thread
{
    private static final Object FINISH = new Object();
    private LinkedBlockingQueue<Object> mFinishQueue = new LinkedBlockingQueue<Object>();
    
    protected volatile boolean mIsStart = false;
    
    abstract void startThreadsInit();
    
    abstract void endThreadsDestroy();
    
    public synchronized void startThread()
    {
        if (mIsStart)
        {
            stopThread();
        }
        startThreadsInit();
        mIsStart = true;
        super.start();
    }
    
    public synchronized void stopThread()
    {
        if (mIsStart)
        {
            endThreadsDestroy();
            mIsStart = false;
            waitFinish();
        }
    }
    
    @Override
    public void run()
    {
        execute();
        
        notifyFinish();
    }
    
    public abstract void execute();
    
    private void notifyFinish()
    {
        mFinishQueue.add(FINISH);
    }
    
    private void waitFinish()
    {
        try
        {
            mFinishQueue.take();
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }
}
