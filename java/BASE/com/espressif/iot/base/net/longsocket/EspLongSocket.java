package com.espressif.iot.base.net.longsocket;

import java.io.IOException;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.espressif.iot.base.api.EspBaseApiUtil;
import com.espressif.iot.base.net.rest.mesh.EspSocketClient;
import com.espressif.iot.base.net.rest.mesh.EspSocketRequestBaseEntity;
import com.espressif.iot.base.net.rest.mesh.EspSocketResponseBaseEntity;

public class EspLongSocket implements IEspLongSocket
{
    private static final Logger log = Logger.getLogger(EspLongSocket.class);
    
    private static final long INTERVAL = 25;
    
    private volatile boolean mIsClosed = false;
    
    private volatile boolean mIsFinished = false;
    
    private volatile boolean mIsLastTaskSendFinished = false;
    
    private volatile boolean mIsLastTaskRecFinished = false;
    
    private String mTargetHost = null;
    
    private int mTargetPort = -1;
    
    private int mMaxTaskSize = Integer.MAX_VALUE;
    
    private EspSocketClient mClient;
    
    private EspLongSocketDisconnected mListener;
    
    private LinkedBlockingDeque<EspLongSocketRequest> mTaskDeque;
    
    private LinkedBlockingDeque<JSONObject> mResultDeque;
    
    private Runnable mProducerTask;
    
    private Runnable mConsumerTask;
    
    class EspLongSocketRequest
    {
        private EspSocketRequestBaseEntity mRequestEntity;
        
        private String mBssid;
        
        private boolean mIsMeshDevice;
        
        EspLongSocketRequest()
        {
        }
        
        EspLongSocketRequest(EspSocketRequestBaseEntity requestEntity, String bssid, boolean isMeshDevice)
        {
            this.mRequestEntity = requestEntity;
            this.mBssid = bssid;
            this.mIsMeshDevice = isMeshDevice;
        }
        
        EspSocketRequestBaseEntity getRequest()
        {
            return mRequestEntity;
        }
        
        String getBssid()
        {
            return mBssid;
        }
        
        boolean isMeshDevice()
        {
            return mIsMeshDevice;
        }
    }
    
    public EspLongSocket()
    {
        this.mClient = new EspSocketClient();
        this.mTaskDeque = new LinkedBlockingDeque<EspLongSocketRequest>();
        this.mResultDeque = new LinkedBlockingDeque<JSONObject>();
        this.mProducerTask = new ProducerTask();
        this.mConsumerTask = new ConsumerTask();
        new Thread(mProducerTask).start();
        new Thread(mConsumerTask).start();
    }
    
    private class ProducerTask implements Runnable
    {
        
        static final int SEND_RETRY_TIME = 2;
        
        // get the next task to be done
        private EspLongSocketRequest getNextTask()
        {
            // when the EspLongSocket is finished, just return the last task
            if (mIsFinished)
            {
                return mTaskDeque.peekLast();
            }
            
            int size = mTaskDeque.size();
            if (size <= mMaxTaskSize)
            {
                EspLongSocketRequest result = null;
                try
                {
                    result = mTaskDeque.pollFirst(INTERVAL, TimeUnit.MILLISECONDS);
                }
                catch (InterruptedException e)
                {
                    return null;
                }
                return result;
            }
            // if the size is to large, remove some redundant task
            else
            {
                int redundant = size / mMaxTaskSize;
                for (int count = 0; count < redundant; count++)
                {
                    mTaskDeque.removeFirst();
                }
                return mTaskDeque.removeFirst();
            }
        }
        
        // do the next task
        private boolean doNextTask()
        {
            EspLongSocketRequest task = getNextTask();
            // if there's no more task
            if (task == null)
            {
                return false;
            }
            boolean complete = false;
            for (int retry = 0; !complete && retry < SEND_RETRY_TIME; retry++)
            {
                while(!_isReconnectFinished)
                {
                    try
                    {
                        Thread.sleep(INTERVAL);
                    }
                    catch (InterruptedException e)
                    {
                        e.printStackTrace();
                    }
                }
                try
                {
                    // not mesh device
                    if (!task.isMeshDevice())
                    {
                        mClient.writeRequest(task.getRequest().toString());
                    }
                    // mesh device
                    else
                    {
                        JSONObject json = null;
                        try
                        {
                            json = new JSONObject(task.getRequest().getContent());
                        }
                        catch (JSONException e)
                        {
                            e.printStackTrace();
                        }
                        boolean checkIsDeviceAvailable = false;
                        boolean closeClientImmdeiately = false;
                        int targetPort = 8000;
                        int connectTimeout = 10000;
                        int connectRetry = 3;
                        boolean isResultRead = false;
                        int soTimeout = 3000;
                        // for the moment method "EspBaseApiUtil.PostForJson(...)" parameter deviceBssid isn't used
                        // ,so we use null to replace it
                        String deviceBssid = task.getBssid();
                        EspBaseApiUtil.PostForJson(mClient,
                            task.getRequest().getOriginUri(),
                            deviceBssid,
                            json,
                            checkIsDeviceAvailable,
                            closeClientImmdeiately,
                            targetPort,
                            connectTimeout,
                            connectRetry,
                            isResultRead,
                            soTimeout);
                    }
                    complete = true;
                    log.debug("ProducerTask:: doNextTask() nextTask is completed");
                }
                catch (IOException e)
                {
                    log.warn("ProducerTask:: doNextTask() send task fail, close the old client and try reconnect");
                    try
                    {
                        mClient.close();
                    }
                    catch (IOException ignore)
                    {
                    }
                    if (!__reconnect())
                    {
                        log.warn(Thread.currentThread().toString()
                            + "ProducerTask:: doNextTask() send socket fail and reconnect fail");
                        break;
                    }
                }
            }
            return complete;
        }
        
        @Override
        public void run()
        {
            while (!mIsClosed)
            {
                doNextTask();
                if (mIsFinished)
                {
                    log.info("ProducerTask:: the EspLongSocket is finished");
                    break;
                }
            }
            if (!mIsClosed && !mTaskDeque.isEmpty())
            {
                log.info("ProducerTask:: do the last task");
                doNextTask();
                mIsLastTaskSendFinished = true;
            }
        }
    }
    
    private class ConsumerTask implements Runnable
    {
        
        private boolean doNextTask()
        {
            EspSocketResponseBaseEntity response = mClient.readResponseEntity();
            
            if (response == null)
            {
                log.debug("ConsumerTask:: doNextTask() nextTask is null, just nap");
                try
                {
                    Thread.sleep(INTERVAL);
                }
                catch (InterruptedException ignore)
                {
                }
                return false;
            }
            
            else
            {
                log.debug("ConsumerTask:: doNextTask() read one response");
                String jsonStr = response.getContentBodyStr();
                // TODO
                // it shouldn't happen here, but device return bad format
                if (jsonStr == null)
                {
                    log.warn("ConsumerTask:: doNextTask() read bad format");
                    return false;
                }
                try
                {
                    JSONObject json = new JSONObject(jsonStr);
                    log.debug("ConsumerTask:: doNextTask() add one result into result deque");
                    mResultDeque.addLast(json);
                    return true;
                }
                catch (JSONException e)
                {
                    log.warn("ConsumerTask:: doNextTask() bad JSON format");
                }
                return false;
            }
        }
        
        @Override
        public void run()
        {
            while (!mIsClosed)
            {
                doNextTask();
                if (mIsFinished)
                {
                    log.info("ConsumerTask:: the EspLongSocket is finished");
                    break;
                }
            }
            // wait the last request suc
            while (!mIsLastTaskSendFinished && !mIsClosed)
            {
                try
                {
                    Thread.sleep(INTERVAL);
                }
                catch (InterruptedException ignore)
                {
                }
            }
            // receive the result as more as possible
            boolean exeSuc = false;
            do
            {
                exeSuc = doNextTask();
            } while (exeSuc);
            mIsLastTaskRecFinished = true;
            // close the socket when all of the task are finished
            __close();
        }
        
    }
    
    @Override
    protected void finalize() throws Throwable {
        __close();
        super.finalize();
    }
    
    @Override
    public void setEspLongSocketDisconnectedListener(EspLongSocketDisconnected listener)
    {
        this.mListener = listener;
    }
    
    private volatile boolean _isReconnectFinished = false;
    
    // when the client is broken up, it will try to reconnect several times
    private synchronized boolean __reconnect()
    {
        _isReconnectFinished = false;
        // check state valid
        if (mTargetHost == null || mTargetPort == -1)
        {
            throw new IllegalArgumentException("mTargetHost or mTargetPort hasn't been initiated");
        }
        if (mIsClosed)
        {
            log.warn(Thread.currentThread().toString() + "##__reconnect(): the socket is closed already");
            _isReconnectFinished = true;
            return false;
        }
        
        boolean isReconnectSuc = mClient.connect(mTargetHost, mTargetPort, CONNECT_TIMEOUT);
        log.debug(Thread.currentThread().toString() + "##__reconnect(): " + isReconnectSuc);
        // try to connect to the target and its port in SO_CONNECT_RETRY times
        for (int retryTime = 0; !mIsFinished && !mIsClosed && !isReconnectSuc && retryTime < SO_CONNECT_RETRY-1; retryTime++)
        {
            try
            {
                Thread.sleep(CONNECT_FAIL_SLEEP_TIEMOUT);
            }
            catch (InterruptedException e)
            {
                _isReconnectFinished = true;
                return false;
            }
            // close the old one and create new Client
            try
            {
                log.debug(Thread.currentThread().toString() + "##__reconnect(): close the old client");
                mClient.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            mClient = new EspSocketClient();
            isReconnectSuc = mClient.connect(mTargetHost, mTargetPort, CONNECT_TIMEOUT);
            log.debug(Thread.currentThread().toString() + "##__reconnect(): " + isReconnectSuc);
        }
        // connect fail
        if (!isReconnectSuc)
        {
            if (this.mListener != null && !this.mIsFinished && !this.mIsClosed)
            {
                log.debug("mListener is activated for socket is disconnected");
                this.mListener.onEspLongSocketDisconnected();
            }
            __close();
        }
        _isReconnectFinished = true;
        return isReconnectSuc;
    }
    
    private synchronized void __close()
    {
        if (!this.mIsClosed)
        {
            log.debug(Thread.currentThread().toString() + "##__close()");
            try
            {
                this.mClient.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        this.mIsClosed = true;
    }
    
    private void __finish()
    {
        log.debug(Thread.currentThread().toString() + "##__finish()");
        this.mIsFinished = true;
    }
    
    @Override
    public void setTarget(String targetHost, int targetPort, int maxTaskSize)
    {
        this.mTargetHost = targetHost;
        this.mTargetPort = targetPort;
        this.mMaxTaskSize = maxTaskSize;
    }
    
    @Override
    public boolean connect()
    {
        boolean isConnectSuc = __reconnect();
        log.debug(Thread.currentThread().toString() + "##connect(): " + isConnectSuc);
        return isConnectSuc;
    }
    
    @Override
    public void addRequest(EspSocketRequestBaseEntity request, String bssid, boolean isMeshDevice)
    {
        if (mIsClosed || mIsFinished)
        {
            log.warn(Thread.currentThread().toString()
                + "##addRequest(): EspLongSocket task is closed or finished already");
            return;
        }
        this.mTaskDeque.addLast(new EspLongSocketRequest(request, bssid, isMeshDevice));
    }
    
    @Override
    public void close()
    {
        __close();
    }
    
    @Override
    public void finish()
    {
        __finish();
    }
    
    @Override
    public JSONObject getLastResponse()
    {
        if (!this.mIsFinished)
        {
            return mResultDeque.peekLast();
        }
        else
        {
            // wait the last task finished
            while (!mIsClosed && !mIsLastTaskRecFinished)
            {
                try
                {
                    Thread.sleep(INTERVAL);
                }
                catch (InterruptedException e)
                {
                    return null;
                }
            }
            JSONObject jsonResult = mResultDeque.peekLast();
            return jsonResult;
        }
    }
    
}
