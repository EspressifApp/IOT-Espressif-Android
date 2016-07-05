package com.espressif.iot.base.net.proxy;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class EspMeshSocketImpl implements EspMeshSocket
{
    private static final boolean DEBUG = true;
    private static final boolean USE_LOG4J = true;
    private static final Class<?> CLASS = EspMeshSocketImpl.class;
    
    private static final int DEVICE_AVAILABLE_RETRY = 1;
    private static final int DEVICE_AVAILABLE_INTERVAL = 500;
    private static final int DEVICE_AVAILABLE_TIMEOUT = 6000;
    private static final int SO_TIMEOUT = 4000;
    private static final int SO_CONNECT_TIMEOUT = 2000;
    private static final int SO_CONNECT_INTERVAL = 500;
    private static final int SO_CONNECT_RETRY = 3;
    private static final int DEVICE_MESH_PORT = 7000;
    private static final int BUFFER_SIZE_MAX = 1300;
    private static final int DEFAULT_ESPMESH_SOCKET_TIMEOUT = 8000;
    
    /**
     * the new proxy task list
     */
    private final BlockingQueue<EspProxyTask> mRefreshProxyTaskQueue;
    
    /**
     * the proxy task list which has been sent request
     */
    private final List<EspProxyTask> mSentProxyTaskList;
    
    /**
     * long socket serial is used to close long socket request after half close
     */
    private final Map<String, Integer> mLongSocketSerialMap;
    
    /**
     * long socket buffer is used to cache long socket response some special time
     */
    private final Map<String, byte[]> mLongSocketBufferMap;
    
    private boolean mIsClosed;
    
    private boolean mIsHalfClosed;
    
    private EspSocket mSocket;
    
    private InetAddress mTargetInetAddr;
    
    private volatile int mTimeout;
    
    private volatile long mRefreshTimestamp;
    
    private final Lock mConditionLock;
    
    private final Condition mConditionHalfClosed;
    
    private final BlockingQueue<Object> mDeviceAvailableToken;
    
    private final static Object TOKEN_TRUE = new Object();
    
    private final byte[] mBuffer;
    
    private int mBufferOffset = 0;
    
    private EspMeshResponse mMeshResponse = null;
    
    private final Thread mMainThread;
    
    EspMeshSocketImpl(InetAddress inetAddress)
    {
        mTargetInetAddr = inetAddress;
        mRefreshProxyTaskQueue = new LinkedBlockingDeque<EspProxyTask>();
        mSentProxyTaskList = new ArrayList<EspProxyTask>();
        mLongSocketSerialMap = new HashMap<String, Integer>();
        mLongSocketBufferMap = new HashMap<String, byte[]>();
        mIsClosed = false;
        mIsHalfClosed = false;
        mTimeout = DEFAULT_ESPMESH_SOCKET_TIMEOUT;
        mRefreshTimestamp = System.currentTimeMillis();
        mConditionLock = new ReentrantLock();
        mConditionHalfClosed = mConditionLock.newCondition();
        mDeviceAvailableToken = new LinkedBlockingDeque<Object>();
        mBuffer = new byte[BUFFER_SIZE_MAX];
        
        mMainThread = new Thread()
        {
            public void run()
            {
                loop();
            }
        };
        mMainThread.start();
    }

    private void putLongSocketBuffer(String targetBssid, byte[] buffer)
    {
        MeshLog.d(DEBUG, USE_LOG4J, CLASS, "putLongSocketBuffer() targetBssid:" + targetBssid + ", buffer:"
            + new String(buffer));
        synchronized (mLongSocketBufferMap)
        {
            mLongSocketBufferMap.put(targetBssid, buffer);
        }
    }
    
    private void clearLongSocketBuffer(String targetBssid)
    {
        MeshLog.d(DEBUG, USE_LOG4J, CLASS, "clearLongSocketBuffer() targetBssid:" + targetBssid);
        synchronized (mLongSocketBufferMap)
        {
            mLongSocketBufferMap.remove(targetBssid);
        }
    }
    
    private void clearLongSocketBuffer()
    {
        MeshLog.d(DEBUG, USE_LOG4J, CLASS, "clearLongSocketBuffer()");
        synchronized (mLongSocketBufferMap)
        {
            mLongSocketBufferMap.clear();
        }
    }
    
    private void putLongSocketSerialMap(String targetBssid, int serial)
    {
        MeshLog.d(DEBUG, USE_LOG4J, CLASS, "putLongSocketSerialMap() targetBssid: " + targetBssid + ",serial: " + serial);
        synchronized (mLongSocketSerialMap)
        {
            mLongSocketSerialMap.put(targetBssid, serial);
        }
    }
    
    private boolean isLongSocketExist(String targetBssid)
    {
        synchronized (mLongSocketSerialMap)
        {
            return mLongSocketSerialMap.containsKey(targetBssid);
        }
    }
    
    private boolean isLongSocketSerialExistMap(String targetBssid, int serial)
    {
        synchronized (mLongSocketSerialMap)
        {
            Integer serialInteger = mLongSocketSerialMap.get(targetBssid);
            if (serialInteger != null && serialInteger.intValue() == serial)
            {
                return true;
            }
            else
            {
                return false;
            }
        }
    }
    
    private void clearLongSocketSerialMap()
    {
        synchronized (mLongSocketSerialMap)
        {
            mLongSocketSerialMap.clear();
        }
    }
    
    private EspSocket open(InetAddress remoteInetAddr)
    {
        EspSocket socket = EspSocket.createEspSocket();
        try
        {
            socket.setSoTimeout(SO_TIMEOUT);
        }
        catch (SocketException e)
        {
            e.printStackTrace();
        }
        SocketAddress remoteSckAddr = new InetSocketAddress(remoteInetAddr, DEVICE_MESH_PORT);
        boolean isConnected = false;
        for (int retry = 0; !isConnected && retry < SO_CONNECT_RETRY; ++retry)
        {
            // connect to target device(root device)
            try
            {
                socket.connect(remoteSckAddr, SO_CONNECT_TIMEOUT);
                isConnected = true;
                break;
            }
            catch (IOException e)
            {
                String message = EspSocketUtil.getStrackTrace(e);
                MeshLog.e(DEBUG, USE_LOG4J, CLASS, message);
            }
            try
            {
                Thread.sleep(SO_CONNECT_INTERVAL);
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
                break;
            }
        }
        if (!isConnected)
        {
            MeshLog.d(DEBUG, USE_LOG4J, CLASS, "open() fail for remoteInetAddr:" + remoteInetAddr.getHostAddress()
                + ", return null");
            try
            {
                socket.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            close();
            return null;
        }
        else
        {
            MeshLog.d(DEBUG, USE_LOG4J, CLASS, "open() suc for remoteInetAddr:" + remoteInetAddr.getHostAddress()
                + ", so mDeviceAvailableToken.add(TOKEN_TRUE)");
            mDeviceAvailableToken.add(TOKEN_TRUE);
        }
        MeshLog.d(DEBUG, USE_LOG4J, CLASS, "open() for remoteInetAddr:" + remoteInetAddr.getHostAddress() + " suc");
        return socket;
    }
    
    private void sendRequestBytes(byte[] requestBytes, String targetBssid, List<String> targetBssidList, int proto)
    {
        if (!isConnected())
        {
            MeshLog.d(DEBUG, USE_LOG4J, CLASS, "sendRequestBytes() socket isn't connected, return");
            return;
        }
        try
        {
            OutputStream sckOutputStream = mSocket.getOutputStream();
            EspMeshRequest meshRequest =
                targetBssidList == null ? EspMeshRequest.createInstance(proto, targetBssid, requestBytes)
                    : EspMeshRequest.createInstance(proto, targetBssidList, requestBytes);
            requestBytes = meshRequest.getRequestBytes();
            
            // write requestBytes
            EspSocketUtil.writeBytes(sckOutputStream, requestBytes);
            // flush
            EspSocketUtil.flush(sckOutputStream);
            refresh();
            MeshLog.d(DEBUG, USE_LOG4J, CLASS, "sendRequestBytes() targetBssid:" + targetBssid + ", write suc");
        }
        catch (IOException e)
        {
            String message = EspSocketUtil.getStrackTrace(e);
            MeshLog.e(DEBUG, USE_LOG4J, CLASS, "sendRequestBytes() targetBssid:" + targetBssid + ", IOException e:"
                + message + " ,so close EspMeshSocket");
            close();
        }
    }
    
    private void loop()
    {
        EspProxyTask proxyTask = null;
        try
        {
            proxyTask = mRefreshProxyTaskQueue.take();
            MeshLog.d(DEBUG, USE_LOG4J, CLASS, "loop() take1 proxyTask: " + proxyTask + " from mRefreshProxyTaskQueue");
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
        MeshLog.d(DEBUG, USE_LOG4J, CLASS,  "loop() take proxyTask: " + proxyTask);
        
        String targetBssid = proxyTask.getTargetBssid();
        if (proxyTask != null && proxyTask != EspProxyTaskImpl.CLOSE_PROXYTASK)
        {
            // connect to the target
            if (mSocket == null)
            {
                MeshLog.d(DEBUG, USE_LOG4J, CLASS,  "loop() try to open() " + mTargetInetAddr.getHostAddress());
                mSocket = open(mTargetInetAddr);
                if (mSocket == null)
                {
                    MeshLog.w(DEBUG, USE_LOG4J, CLASS, "loop() fail to open() " + mTargetInetAddr.getHostAddress() + ", so proxyTask: " + proxyTask + " replyClose()");
                    proxyTask.replyClose();
                    return;
                }
                else
                {
                    refresh();
                }
            }
        }
        while (isConnected() && !mIsClosed)
        {
            boolean isDeviceAvailable = false;
            for (int retry = 0; !isDeviceAvailable && retry < DEVICE_AVAILABLE_RETRY; ++retry)
            {
                if (retry != 0)
                {
                    try
                    {
                        Thread.sleep(DEVICE_AVAILABLE_INTERVAL);
                    }
                    catch (InterruptedException e)
                    {
                        e.printStackTrace();
                        break;
                    }
                }
                MeshLog.d(DEBUG, USE_LOG4J, CLASS, "loop() sendIsDeviceAvailable() " + retry + " time");
                
                // wait is device available
                int timeout = proxyTask.isReadOnlyTask() ? proxyTask.getTaskTimeout() : DEVICE_AVAILABLE_TIMEOUT;
                Object token = waitDeviceAvailableToken(timeout);
                if (token == null)
                {
                    MeshLog.w(DEBUG, USE_LOG4J, CLASS,  "loop() waitDeviceAvailableToken() get null, break");
                    break;
                }
                isDeviceAvailable = token == TOKEN_TRUE;
                MeshLog.d(DEBUG, USE_LOG4J, CLASS, "loop() waitDeviceAvailableToken() " + retry + " time, isDeviceAvailable: " + isDeviceAvailable);
            }
            
            if (!isDeviceAvailable)
            {
                halfClose();
                proxyTask.replyClose();
                MeshLog.w(DEBUG, USE_LOG4J, CLASS, "loop() isDeviceAvailable is false, halfClose() proxyTask replyClose() and break");
                break;
            }
            else
            {
                proxyTask.updateTimestamp();
            }
            
            byte[] requestBytes = proxyTask.getRequestBytes();
            // send request
            if (!proxyTask.isReadOnlyTask())
            {
                int proto = proxyTask.getProtoType();
                List<String> targetBssidList = proxyTask.getGroupBssidList();
                sendRequestBytes(requestBytes, targetBssid, targetBssidList, proto);
                MeshLog.i(DEBUG, USE_LOG4J, CLASS, "loop() sendRequestBytes to " + targetBssid + " suc");
            }
            else
            {
                // return device available ticket
                mDeviceAvailableToken.add(TOKEN_TRUE);
                refresh();
                MeshLog.i(DEBUG, USE_LOG4J, CLASS, "loop() send dummy request for proxy task is read only, return device available");
            }
            
            if (isConnected())
            {
                if (proxyTask.isReplyRequired())
                {
                    MeshLog.i(DEBUG, USE_LOG4J, CLASS, "loop() add proxyTask of " + targetBssid + " into mSentProxyTaskList");
                    // add proxyTask into sentTaskList
                    synchronized (mSentProxyTaskList)
                    {
                        mSentProxyTaskList.add(proxyTask);
                    }
                }
                else
                {
                    MeshLog.i(DEBUG, USE_LOG4J, CLASS, "loop() reply a non reply request");
                    try
                    {
                        proxyTask.replyResponse();
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                }
            }
            else
            {
                MeshLog.w(DEBUG, USE_LOG4J, CLASS, "loop() fail to sendRequestBytes to " + targetBssid + " :"
                    + ", so proxyTask: " + proxyTask + " replyClose() and break");
                proxyTask.replyClose();
                break;
            }
            
            // try to get next proxyTask
            try
            {
                proxyTask = mRefreshProxyTaskQueue.take();
                MeshLog.d(DEBUG, USE_LOG4J, CLASS, "loop() take2 proxyTask: " + proxyTask + " from mRefreshProxyTaskQueue");
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
            MeshLog.i(DEBUG, USE_LOG4J, CLASS, "loop() take proxyTask: " + proxyTask);
            if (proxyTask == null || proxyTask == EspProxyTaskImpl.CLOSE_PROXYTASK)
            {
                break;
            }
            else
            {
                // don't forget to update bssid
                targetBssid = proxyTask.getTargetBssid();
            }
        }
    }

    @Override
    public InetAddress getInetAddress()
    {
        return mTargetInetAddr;
    }
    
    @Override
    public List<EspProxyTask> getRefreshProxyTaskList()
    {
        List<EspProxyTask> proxyTaskList = new ArrayList<EspProxyTask>();
        for (EspProxyTask proxyTask : mRefreshProxyTaskQueue)
        {
            if (proxyTask != EspProxyTaskImpl.CLOSE_PROXYTASK && !proxyTask.isFinished())
            {
                proxyTask.updateTimestamp();
                proxyTaskList.add(proxyTask);
            }
        }
        MeshLog.d(DEBUG, USE_LOG4J, CLASS, "mRefreshProxyTaskQueue: " + mRefreshProxyTaskQueue + ",getRefreshProxyTaskList() " + proxyTaskList);
        return proxyTaskList;
    }
    
    private void addNewProxyTask(EspProxyTask proxyTask)
    {
        // add new proxy task to mRefreshProxyTaskQueue
        mRefreshProxyTaskQueue.add(proxyTask);
        MeshLog.d(DEBUG, USE_LOG4J, CLASS, "addNewProxyTask() proxyTask: " + proxyTask);
        // put long serial map if necessary
        int serial = proxyTask.getLongSocketSerial();
        if (serial != MeshCommunicationUtils.SERIAL_NORMAL_TASK)
        {
            String targetBssid = proxyTask.getTargetBssid();
            putLongSocketSerialMap(targetBssid, serial);
        }
    }
    
    private void increaseTimeout(EspProxyTask proxyTask)
    {
        MeshLog.i(DEBUG, USE_LOG4J, CLASS, "increaseTimeout() " + proxyTask.getTaskTimeout());
        mTimeout += proxyTask.getTaskTimeout();
    }
    
    private void decreaseTiemout(EspProxyTask proxyTask)
    {
        MeshLog.i(DEBUG, USE_LOG4J, CLASS, "decreaseTimeout() " + proxyTask.getTaskTimeout());
        mTimeout -= proxyTask.getTaskTimeout();
    }
    
    @Override
    public void offer(EspProxyTask proxyTask)
    {
        proxyTask.updateTimestamp();
        // check whether the target InetAddress is valid
        if (!proxyTask.getTargetInetAddress().equals(mTargetInetAddr))
        {
            throw new IllegalArgumentException("EspProxyTask's target InetAddress is wrong");
        }
        // update the EspMeshSocketImpl timeout if necessary
        if (proxyTask.getTaskTimeout() != 0)
        {
            increaseTimeout(proxyTask);
        }
        addNewProxyTask(proxyTask);
    }
    
    /**
     * close the EspMeshSocket half, don't accept more new request
     */
    @Override
    public void halfClose()
    {
        MeshLog.e(DEBUG, USE_LOG4J, CLASS, "halfClose()");
        mIsHalfClosed = true;
    }
    
    @Override
    public synchronized void close()
    {
        if (!mIsClosed)
        {
            if (mSocket != null)
            {
                try
                {
                    mSocket.close();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
            
            synchronized (mSentProxyTaskList)
            {
                for (EspProxyTask proxyTask : mSentProxyTaskList)
                {
                    proxyTask.replyClose();
                    MeshLog.d(DEBUG,
                        USE_LOG4J,
                        CLASS,
                        "close() proxyTask in mSentProxyTaskList :" + proxyTask + " replyClose()");
                }
            }

//            for (EspProxyTask proxyTask : mRefreshProxyTaskQueue)
//            {
//                boolean isClosedNecessary = false;
//                if (proxyTask != EspProxyTaskImpl.CLOSE_PROXYTASK && proxyTask.isExpired())
//                {
//                    isClosedNecessary = true;
//                }
//                if (!isClosedNecessary)
//                {
//                    int serial =
//                        proxyTask != EspProxyTaskImpl.CLOSE_PROXYTASK ? proxyTask.getLongSocketSerial()
//                            : MeshCommunicationUtils.SERIAL_NORMAL_TASK;
//                    String targetBssid = proxyTask.getTargetBssid();
//                    if (serial != MeshCommunicationUtils.SERIAL_NORMAL_TASK)
//                    {
//                        if (isLongSocketSerialExistMap(targetBssid, serial))
//                        {
//                            isClosedNecessary = true;
//                        }
//                    }
//                }
//                if (isClosedNecessary)
//                {
//                    proxyTask.replyClose();
//                    MeshLog.d(DEBUG, USE_LOG4J, CLASS, "close() expired proxyTask in mRefreshProxyTaskQueue :"
//                        + proxyTask + " replyClose()");
//                }
//            }
            
            clearLongSocketBuffer();
            clearLongSocketSerialMap();
            mRefreshProxyTaskQueue.add(EspProxyTaskImpl.CLOSE_PROXYTASK);
            mIsClosed = true;
            mConditionLock.lock();
            mConditionHalfClosed.signalAll();
            mConditionLock.unlock();
            mMainThread.interrupt();
            MeshLog.d(DEBUG, USE_LOG4J, CLASS, "EspMeshSocketImpl is closed");
        }
    }
    
    @Override
    public boolean isExpired()
    {
        long consume = System.currentTimeMillis() - mRefreshTimestamp;
        boolean isExpired = consume > mTimeout;
        return isExpired;
    }
    
    /**
     * make the EspSocket keep refresh
     */
    private void refresh()
    {
        if (!isHalfClosed())
        {
            mRefreshTimestamp = System.currentTimeMillis();
        }
    }
    
    /**
     * reply proxy task
     * 
     * @param targetBssid the target device's bssid
     * @param responseBytes the response bytes from buffer or null if response bytes are in mMeshResponse
     * @return whether the proxy task of the target bssid is exist
     */
    private boolean replyProxyTask(String targetBssid, byte[] responseBytes)
    {
        MeshLog.d(DEBUG, USE_LOG4J, CLASS, "replyProxyTask() entrance");
        // try to find the ProxyTask in the mSentProxyTaskList
        EspProxyTask finishedProxyTask = null;
        synchronized (mSentProxyTaskList)
        {
            for (int i = 0; i < mSentProxyTaskList.size(); ++i)
            {
                EspProxyTask proxyTask = mSentProxyTaskList.get(i);
                if (proxyTask.getTargetBssid().equals(targetBssid))
                {
                    finishedProxyTask = proxyTask;
                    mSentProxyTaskList.remove(i--);
                    MeshLog.i(DEBUG, USE_LOG4J, CLASS, "replyProxyTask() remove " + targetBssid
                        + " from mSentProxyTaskList");
                    break;
                }
            }
        }
        if (finishedProxyTask != null)
        {
            MeshLog.i(DEBUG, USE_LOG4J, CLASS, "replyProxyTask() proxyTask: " + finishedProxyTask);
            if (responseBytes == null)
            {
                // get response bytes from mMeshResponse if necessary
                responseBytes = mMeshResponse.getPureResponseBytes();
            }
            finishedProxyTask.setResponseBuffer(responseBytes);
            try
            {
                finishedProxyTask.replyResponse();
            }
            catch (IOException e)
            {
                // for we don't think the local socket will encounter IOException at most time.
                // when local socket IOException occur, it shouldn't affect the socket between app and device.
                // just log error for debug when it happen
                String message = EspSocketUtil.getStrackTrace(e);
                MeshLog.e(DEBUG, USE_LOG4J, CLASS, "replyProxyTask() IOException e:" + message);
            }
            // don't open the log for released version, or apk will crash sometime for concurrent exception
//            MeshLog.e(DEBUG, USE_LOG4J, CLASS, "replyProxyTask() proxyTask is removed from mSentProxyTaskList: "
//                + finishedProxyTask + ", mSentProxyTaskList: " + mSentProxyTaskList);
            
            // decrease timeout if necessary
            if (finishedProxyTask.getTaskTimeout() != 0)
            {
                decreaseTiemout(finishedProxyTask);
            }
            return true;
        }
        else
        {
            List<String> bssidList = new ArrayList<String>();
            synchronized (mSentProxyTaskList)
            {
                for (EspProxyTask proxyTask : mSentProxyTaskList)
                {
                    bssidList.add(proxyTask.getTargetBssid());
                }
            }
            MeshLog.e(DEBUG, USE_LOG4J, CLASS, "replyProxyTask() can't find " + targetBssid + ", mSentProxyTaskList bssidList: " + bssidList);
            return false;
        }
    }
    
    private boolean receiveBufferBytes()
    {
        String targetBssid = null;
        byte[] responseBytes = null;
        // check whether there exist response in mLongSocketBufferMap
        synchronized (mLongSocketBufferMap)
        {
            for (String bufferBssid : mLongSocketBufferMap.keySet())
            {
                synchronized (mSentProxyTaskList)
                {
                    for (EspProxyTask proxyTask : mSentProxyTaskList)
                    {
                        if (proxyTask.getTargetBssid().equals(bufferBssid)
                            && proxyTask.getLongSocketSerial() != MeshCommunicationUtils.SERIAL_NORMAL_TASK)
                        {
                            targetBssid = bufferBssid;
                            responseBytes = mLongSocketBufferMap.get(bufferBssid);
                            clearLongSocketBuffer(targetBssid);
                            break;
                        }
                    }
                }
            }
        }
        // reply to target device if necessary
        if (targetBssid != null)
        {
            if (replyProxyTask(targetBssid, responseBytes))
            {
                clearLongSocketBuffer(targetBssid);
            }
            else
            {
                MeshLog.e(DEBUG, USE_LOG4J, CLASS, "receiveBufferBytes() can't find targetBssid: " + targetBssid);
            }
        }
        boolean isReplyAlready = targetBssid != null;
        MeshLog.d(DEBUG, USE_LOG4J, CLASS, "receiveBufferBytes() isReplyAlready: " + (targetBssid != null));
        return isReplyAlready;
    }
    
    /**
     * receive response bytes from mesh device
     */
    private void receiveResponseBytes()
    {
        if (!isConnected())
        {
            MeshLog.w(DEBUG, USE_LOG4J, CLASS, "receiveResponseBytes() socket isn't connected, return");
            return;
        }
        
        if (receiveBufferBytes())
        {
            MeshLog.i(DEBUG, USE_LOG4J, CLASS, "receiveResponseBytes() receive response from buffer, return");
            return;
        }
        
        try
        {
            // clear mBufferOffset
            mBufferOffset = 0;
            InputStream sckInputStream = mSocket.getInputStream();
            byte[] buffer = mBuffer;
            int byteCount = 4;
            // read first 4 bytes
            EspSocketUtil.readBytes(sckInputStream, buffer, mBufferOffset, byteCount);
            mBufferOffset += byteCount;
            // update mMeshResponse
            mMeshResponse = EspMeshResponse.createInstance(buffer);
            
            // read other bytes
            int packageLength = mMeshResponse.getPackageLength();
            byteCount = packageLength - byteCount;
            EspSocketUtil.readBytes(sckInputStream, buffer, mBufferOffset, byteCount);
            
            mBufferOffset += byteCount;
            if (!mMeshResponse.fillInAll(buffer))
            {
                MeshLog.w(DEBUG, USE_LOG4J, CLASS, "receiveResponseBytes() mMeshResponse fail to fill in all, so close() and return");
                close();
                return;
            }
            
            MeshLog.d(DEBUG, true, CLASS, "receiveResponseBytes() meshResponse: " + mMeshResponse);
            
            // check whether it has mesh option
            if (mMeshResponse.hasMeshOption())
            {
                EspMeshOption meshOption = mMeshResponse.getMeshOption();
                for (int i = 0; i < meshOption.getDeviceAvailableCount(); ++i)
                {
                    mDeviceAvailableToken.add(TOKEN_TRUE);
                    MeshLog.d(DEBUG, USE_LOG4J, CLASS, "receiveResponseBytes() receive device available");
                }
            }
            
            if (mMeshResponse.isBodyEmpty())
            {
                MeshLog.d(DEBUG, USE_LOG4J, CLASS, "receiveResponseBytes() mMeshResponse.isBodyEmpty(), return");
                return;
            }
            
            // get targetBssid and reply resposne
            String targetBssid = mMeshResponse.getTargetBssid();
            
            if (targetBssid == null)
            {
                throw new IllegalStateException("receiveResponseBytes() can't filter the targetBssid");
            }
            else
            {
                // check whether there's device available info in the package
                if (mMeshResponse.isDeviceAvailable())
                {
                    mDeviceAvailableToken.add(TOKEN_TRUE);
                    MeshLog.d(DEBUG, USE_LOG4J, CLASS, "receiveResponseBytes() receive device available");
                }
                // can't find proxy task and the bssid exist long socket
                if (!replyProxyTask(targetBssid, null) && isLongSocketExist(targetBssid))
                {
                    byte[] responseBytes = mMeshResponse.getPureResponseBytes();
                    MeshLog.d(DEBUG, USE_LOG4J, CLASS, "receiveResponseBytes() can't find proxy task, so put into long socket buffer");
                    putLongSocketBuffer(targetBssid, responseBytes);
                }
            }
            
        }
        catch (IOException e)
        {
            String message = EspSocketUtil.getStrackTrace(e);
            MeshLog.e(DEBUG, USE_LOG4J, CLASS, "receiveResponseBytes() IOException e:" + message + " , so close EspMeshSocket");
            close();
        }
    }
    
    @Override
    public void checkProxyTaskStateAndProc()
    {
        while (isNewDataArrive())
        {
            MeshLog.d(DEBUG, USE_LOG4J, CLASS, "checkProxyTaskStateAndProc() receiveResponseBytes()");
            receiveResponseBytes();
        }
        List<EspProxyTask> expiredSentProxyTaskList = new ArrayList<EspProxyTask>();
        synchronized (mSentProxyTaskList)
        {
            for (int i = 0; i < mSentProxyTaskList.size(); ++i)
            {
                EspProxyTask proxyTask = mSentProxyTaskList.get(i);
                if (proxyTask != EspProxyTaskImpl.CLOSE_PROXYTASK && proxyTask.isExpired())
                {
                    expiredSentProxyTaskList.add(proxyTask);
                    mSentProxyTaskList.remove(i--);
                    MeshLog.i(DEBUG, USE_LOG4J, CLASS, "checkProxyTaskStateAndProc() remove " + proxyTask.getTargetBssid()
                            + " from mSentProxyTaskList");
                }
            }
        }
        if (expiredSentProxyTaskList.size() > 0)
        {
            MeshLog.d(DEBUG, USE_LOG4J, CLASS, "checkProxyTaskStateAndProc() half close");
            halfClose();
            MeshLog.d(DEBUG, USE_LOG4J, CLASS, "checkProxyTaskStateAndProc() expiredSentProxyTaskList is: " + expiredSentProxyTaskList);
        }
        for (EspProxyTask proxyTask : expiredSentProxyTaskList)
        {
            MeshLog.d(DEBUG, USE_LOG4J, CLASS, "checkProxyTaskStateAndProc() proxyTask.replyClose(): " + proxyTask);
            proxyTask.replyClose();
        }
        if (isHalfClosed())
        {
            MeshLog.d(DEBUG, USE_LOG4J, CLASS, "checkProxyTaskStateAndProc() is in the halfClose state");
            synchronized (mSentProxyTaskList)
            {
                if (mSentProxyTaskList.isEmpty())
                {
                    MeshLog.d(DEBUG, USE_LOG4J, CLASS, "checkProxyTaskStateAndProc() close for mSentProxyTaskList is empty already");
                    close();
                }
            }
        }
    }
    
    /**
     * get whether there's new data arrive
     * 
     * @return whether there's new data arrive
     */
    private boolean isNewDataArrive()
    {
        boolean isAvailable = false;
        // check whether there's buffer for long socket
        for (String bufferBssid : mLongSocketBufferMap.keySet())
        {
            synchronized (mSentProxyTaskList)
            {
                for (EspProxyTask proxyTask : mSentProxyTaskList)
                {
                    if (proxyTask.getTargetBssid().equals(bufferBssid))
                    {
                        return true;
                    }
                }
            }
        }
        // check whether there's new data from socket
        try
        {
            isAvailable = mSocket.getInputStream().available() > 0;
        }
        catch (IOException e)
        {
            String message = EspSocketUtil.getStrackTrace(e);
            MeshLog.e(DEBUG, USE_LOG4J, CLASS, "isNewDataArrive() IOException e:" + message);
        }
        if (isAvailable)
        {
            MeshLog.i(DEBUG, USE_LOG4J, CLASS, "isNewDataArrive() isAvailable: " + isAvailable + " for " + mTargetInetAddr.getHostAddress());
        }
        return isAvailable;
    }
    
    /**
     * wait the mesh root device is available token
     * 
     * @param timeout timeout in milliseconds
     * @return the mesh root device is available token or null(if timeout)
     */
    private Object waitDeviceAvailableToken(int timeout)
    {
        Object deviceAvailableToken = null;
        if (!isConnected())
        {
            MeshLog.d(DEBUG, USE_LOG4J, CLASS, "waitDeviceAvailableToken() socket isn't connected, return false");
            return deviceAvailableToken;
        }
        try
        {
            deviceAvailableToken = mDeviceAvailableToken.poll(timeout, TimeUnit.MILLISECONDS);
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
        
        if (DEBUG)
        {
            MeshLog.i(DEBUG, USE_LOG4J, CLASS, "waitDeviceAvailableToken() " + deviceAvailableToken);
        }
        return deviceAvailableToken;
    }
    
    @Override
    public boolean isConnected()
    {
        return mSocket != null && mSocket.isConnected();
    }
    
    @Override
    public boolean isClosed()
    {
        return mSocket != null && mSocket.isClosed() || mIsClosed;
    }
    
    /**
     * get whether the EspMeshSocket is half closed
     * 
     * @return whether the EspMeshSocket is half closed
     */
    private boolean isHalfClosed()
    {
        return mIsHalfClosed;
    }

    @Override
    public String toString()
    {
        long expireTime = mTimeout - (System.currentTimeMillis() - mRefreshTimestamp);
        return "[mTargetInetAddr: " + mTargetInetAddr.getHostAddress() + ", isClosed:" + mIsClosed + ", isHalfClosed:"
            + mIsHalfClosed + ", expireTime:" + expireTime + "]";
    }
    
}
