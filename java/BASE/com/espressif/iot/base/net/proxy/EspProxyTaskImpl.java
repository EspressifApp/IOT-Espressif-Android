package com.espressif.iot.base.net.proxy;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

public class EspProxyTaskImpl implements EspProxyTask
{
    private final static boolean DEBUG = true;
    private static final boolean USE_LOG4J = true;
    private static final Class<?> CLASS = EspProxyTaskImpl.class;
    
    private InetAddress mTargetInetAddress;
    private String mTargetBssid;
    
    private volatile int mTargetTimeout;
    private volatile long mTimestamp;
    
    private byte[] mRequestBuffer;
    private byte[] mResponseBuffer;
    
    private EspSocket mSourceSocket;
    
    private boolean mRequestValid;
    private boolean mResponseValid;
    
    private boolean mIsReadOnlyTask = false;
    private boolean mIsReplyRequired = true;
    private int mProtoType = M_PROTO_HTTP;
    private int mLongSocketSerial = 0;
    private int mTaskTimeout = 0;
    private List<String> mGroupBssidList = null;
    
    private volatile boolean mFinished;
    
    // the close token used by other
    final static EspProxyTask CLOSE_PROXYTASK = new EspProxyTaskImpl();
    
    private EspProxyTaskImpl()
    {
    }
    
    EspProxyTaskImpl(String meshHost, String meshBssid, byte[] requestBuff, int targetTimeout)
        throws UnknownHostException
    {
        MeshLog.e(DEBUG, USE_LOG4J, CLASS, "EspProxyTaskImpl is created, meshBssid: " + meshBssid);
        mTargetInetAddress = InetAddress.getByName(meshHost);
        mTargetBssid = meshBssid;
        mRequestBuffer = requestBuff;
        mTargetTimeout = targetTimeout;
    }
    
    void setSourceSocket(EspSocket socket)
    {
        if (this == CLOSE_PROXYTASK)
        {
            MeshLog.e(DEBUG, USE_LOG4J, CLASS, "CLOSE_PROXYTASK shouldn't call setSourceSocket()");
            throw new IllegalStateException("CLOSE_PROXYTASK shouldn't call setSourceSocket()");
        }
        mSourceSocket = socket;
    }
    
    @Override
    public InetAddress getTargetInetAddress()
    {
        if (this == CLOSE_PROXYTASK)
        {
            MeshLog.e(DEBUG, USE_LOG4J, CLASS, "CLOSE_PROXYTASK shouldn't call getTargetInetAddress()");
            throw new IllegalStateException("CLOSE_PROXYTASK shouldn't call getTargetInetAddress()");
        }
        return mTargetInetAddress;
    }

    @Override
    public String getTargetBssid()
    {
        if (this == CLOSE_PROXYTASK)
        {
            MeshLog.e(DEBUG, USE_LOG4J, CLASS, "CLOSE_PROXYTASK shouldn't call getTargetBssid()");
            throw new IllegalStateException("CLOSE_PROXYTASK shouldn't call getTargetBssid()");
        }
        return mTargetBssid;
    }
    
    @Override
    public int getTargetTimeout()
    {
        if (this == CLOSE_PROXYTASK)
        {
            MeshLog.e(DEBUG, USE_LOG4J, CLASS, "CLOSE_PROXYTASK shouldn't call getTargetTimeout()");
            throw new IllegalStateException("CLOSE_PROXYTASK shouldn't call getTargetTimeout()");
        }
        return mTargetTimeout;
    }
    
    @Override
    public byte[] getRequestBytes()
    {
        if (this == CLOSE_PROXYTASK)
        {
            MeshLog.e(DEBUG, USE_LOG4J, CLASS, "CLOSE_PROXYTASK CLOSE_PROXYTASK shouldn't call getRequestBytes()");
            throw new IllegalStateException("CLOSE_PROXYTASK shouldn't call getRequestBytes()");
        }
        return mRequestBuffer;
    }
    
    @Override
    public void setResponseBuffer(byte[] buffer)
    {
        if (this == CLOSE_PROXYTASK)
        {
            MeshLog.e(DEBUG, USE_LOG4J, CLASS, "CLOSE_PROXYTASK shouldn't call setResponseBuffer()");
            throw new IllegalStateException("CLOSE_PROXYTASK shouldn't call setResponseBuffer()");
        }
        mResponseBuffer = buffer;
    }

    @Override
    public byte[] getResponseBuffer()
    {
        if (this == CLOSE_PROXYTASK)
        {
            MeshLog.e(DEBUG, USE_LOG4J, CLASS, "CLOSE_PROXYTASK shouldn't call getResponseBuffer()");
            throw new IllegalStateException("CLOSE_PROXYTASK shouldn't call getResponseBuffer()");
        }
        return mResponseBuffer;
    }

    private byte[] getHttpHeader(int contentLength)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("HTTP/1.1 200 OK\r\n");
        sb.append("Content-Length: " + contentLength + "\r\n\r\n");
        return sb.toString().getBytes();
    }
    
    @Override
    public void replyResponse()
        throws IOException
    {
        if (this == CLOSE_PROXYTASK)
        {
            MeshLog.e(DEBUG, USE_LOG4J, CLASS, "CLOSE_PROXYTASK shouldn't call replyResponse()");
            throw new IllegalStateException("CLOSE_PROXYTASK shouldn't call replyResponse()");
        }
        try
        {
            // add fake HTTP header if response isn't HTTP
            if (mProtoType != M_PROTO_HTTP)
            {
                // return empty HTTP response if mResponseBuffer is null and replyResponse() is called
                int contentLength = mResponseBuffer != null ? mResponseBuffer.length : 0;
                byte[] httpHeader = getHttpHeader(contentLength);
                mSourceSocket.getOutputStream().write(httpHeader);
            }
            if (mResponseBuffer != null)
            {
                mSourceSocket.getOutputStream().write(mResponseBuffer);
            }
            mSourceSocket.getOutputStream().flush();
            mSourceSocket.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
            throw e;
        }
        finally
        {
            mFinished = true;
        }
        MeshLog.i(DEBUG, USE_LOG4J, CLASS, "EspProxyTaskImpl meshBssid: " + mTargetBssid + " replyResponse()");
    }

    @Override
    public void replyClose()
    {
        if (this == CLOSE_PROXYTASK)
        {
            MeshLog.e(DEBUG, USE_LOG4J, CLASS, "CLOSE_PROXYTASK shouldn't call setSourceSocket()");
            throw new IllegalStateException("CLOSE_PROXYTASK shouldn't call replyClose()");
        }
        try
        {
            mSourceSocket.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        mFinished = true;
        MeshLog.i(DEBUG, USE_LOG4J, CLASS, "EspProxyTaskImpl meshBssid: " + mTargetBssid + " replyClose()");
    }

    @Override
    public boolean isFinished()
    {
        if (this == CLOSE_PROXYTASK)
        {
            MeshLog.e(DEBUG, USE_LOG4J, CLASS, "CLOSE_PROXYTASK shouldn't call isFinished()");
            throw new IllegalStateException("CLOSE_PROXYTASK shouldn't call isFinished()");
        }
        return mFinished;
    }

    @Override
    public void setFinished(boolean isFinished)
    {
        if (this == CLOSE_PROXYTASK)
        {
            MeshLog.e(DEBUG, USE_LOG4J, CLASS, "CLOSE_PROXYTASK shouldn't call setFinished()");
            throw new IllegalStateException("CLOSE_PROXYTASK shouldn't call setFinished()");
        }
        mFinished = isFinished;
    }

    @Override
    public void setRequestValid(boolean isRequestValid)
    {
        if (this == CLOSE_PROXYTASK)
        {
            MeshLog.e(DEBUG, USE_LOG4J, CLASS, "CLOSE_PROXYTASK shouldn't call setRequestValid()");
            throw new IllegalStateException("CLOSE_PROXYTASK shouldn't call setRequestValid()");
        }
        mRequestValid = isRequestValid;
    }

    @Override
    public boolean isRequestValid()
    {
        if (this == CLOSE_PROXYTASK)
        {
            MeshLog.e(DEBUG, USE_LOG4J, CLASS, "CLOSE_PROXYTASK shouldn't call isRequestValid()");
            throw new IllegalStateException("CLOSE_PROXYTASK shouldn't call isRequestValid()");
        }
        return mRequestValid;
    }

    @Override
    public void setResponseVaild(boolean isResponseValid)
    {
        if (this == CLOSE_PROXYTASK)
        {
            MeshLog.e(DEBUG, USE_LOG4J, CLASS, "CLOSE_PROXYTASK shouldn't call setResponseVaild()");
            throw new IllegalStateException("CLOSE_PROXYTASK shouldn't call setResponseVaild()");
        }
        mResponseValid = isResponseValid;
    }

    @Override
    public boolean isResponseValid()
    {
        if (this == CLOSE_PROXYTASK)
        {
            MeshLog.e(DEBUG, USE_LOG4J, CLASS, "CLOSE_PROXYTASK shouldn't call isResponseValid()");
            throw new IllegalStateException("CLOSE_PROXYTASK shouldn't call isResponseValid()");
        }
        return mResponseValid;
    }
    
    @Override
    public void updateTimestamp()
    {
        if (this == CLOSE_PROXYTASK)
        {
            MeshLog.e(DEBUG, USE_LOG4J, CLASS, "CLOSE_PROXYTASK shouldn't call updateTimestamp()");
            throw new IllegalStateException("CLOSE_PROXYTASK shouldn't call updateTimestamp()");
        }
        mTimestamp = System.currentTimeMillis();
    }

    @Override
    public boolean isExpired()
    {
        if (this == CLOSE_PROXYTASK)
        {
            MeshLog.e(DEBUG, USE_LOG4J, CLASS, "CLOSE_PROXYTASK shouldn't call isExpired()");
            throw new IllegalStateException("CLOSE_PROXYTASK shouldn't call isExpired()");
        }
        long consume = System.currentTimeMillis() - mTimestamp;
        boolean isExpired = consume > (mTargetTimeout + mTaskTimeout);
        return isExpired;
    }
    
    void setReadOnlyTask(boolean readOnly) {
        mIsReadOnlyTask = readOnly;
    }
    
    @Override
    public boolean isReadOnlyTask()
    {
        if (this == CLOSE_PROXYTASK)
        {
            MeshLog.e(DEBUG, USE_LOG4J, CLASS, "CLOSE_PROXYTASK shouldn't call isReadOnlyTask()");
            throw new IllegalStateException("CLOSE_PROXYTASK shouldn't call isReadOnlyTask()");
        }
        return mIsReadOnlyTask;
    }
    
    void setNeedReplyResponse(boolean replyResponse) {
        mIsReplyRequired = replyResponse;
    }
    
    @Override
    public boolean isReplyRequired()
    {
        if (this == CLOSE_PROXYTASK)
        {
            MeshLog.e(DEBUG, USE_LOG4J, CLASS, "CLOSE_PROXYTASK shouldn't call isReplyRequired()");
            throw new IllegalStateException("CLOSE_PROXYTASK shouldn't call isReplyRequired()");
        }
        return mIsReplyRequired;
    }
    
    void setProtoType(int type) {
        mProtoType = type;
    }
    
    @Override
    public int getProtoType()
    {
        if (this == CLOSE_PROXYTASK)
        {
            MeshLog.e(DEBUG, USE_LOG4J, CLASS, "CLOSE_PROXYTASK shouldn't call getProtoType()");
            throw new IllegalStateException("CLOSE_PROXYTASK shouldn't call getProtoType()");
        }
        return mProtoType;
    }
    
    void setLongSocketSerial(int serial)
    {
        mLongSocketSerial = serial;
    }
    
    @Override
    public int getLongSocketSerial()
    {
        if (this == CLOSE_PROXYTASK)
        {
            MeshLog.e(DEBUG, USE_LOG4J, CLASS, "CLOSE_PROXYTASK shouldn't call getLongSocketSerial()");
            throw new IllegalStateException("CLOSE_PROXYTASK shouldn't call getLongSocketSerial()");
        }
        return mLongSocketSerial;
    }
    
    void setTaskTimeout(int timeout)
    {
        mTaskTimeout = timeout;
    }
    
    @Override
    public int getTaskTimeout()
    {
        if (this == CLOSE_PROXYTASK)
        {
            MeshLog.e(DEBUG, USE_LOG4J, CLASS, "CLOSE_PROXYTASK shouldn't call getTaskTimeout()");
            throw new IllegalStateException("CLOSE_PROXYTASK shouldn't call getTaskTimeout()");
        }
        return mTaskTimeout;
    }
    
    @Override
    public String toString()
    {
        if (this == CLOSE_PROXYTASK)
        {
            return "[ CLOSE_PROXYTASK ]";
        }
        else
        {
            return "[" + "host = " + mTargetInetAddress.getHostAddress() + " | bssid = " + mTargetBssid
                + " | request valid = " + mRequestValid + " | response valid = " + mResponseValid + " | finished = "
                + mFinished + " | mLongSocketSerial = " + mLongSocketSerial + "]";
        }
    }

    @Override
    public List<String> getGroupBssidList()
    {
        return mGroupBssidList;
    }
    
    @Override
    public void setGroupBssidList(List<String> groupBssidList)
    {
        mGroupBssidList = groupBssidList;
    }
    
}
