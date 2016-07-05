package com.espressif.iot.action.longsocket2;

import java.net.InetAddress;
import java.util.concurrent.LinkedBlockingDeque;

import org.apache.log4j.Logger;

import com.espressif.iot.command.device.light.EspCommandLightPostStatusInternet;
import com.espressif.iot.command.device.light.EspCommandLightPostStatusLocal;
import com.espressif.iot.command.device.light.IEspCommandLightPostStatusInternet;
import com.espressif.iot.command.device.light.IEspCommandLightPostStatusLocal;
import com.espressif.iot.device.IEspDevice;
import com.espressif.iot.type.device.IEspDeviceState;
import com.espressif.iot.type.device.IEspDeviceStatus;
import com.espressif.iot.type.device.status.IEspStatusLight;

public class EspActionLongSocket implements IEspActionLongSocket
{
    private static class EspLongSocketRequest
    {
        private EspLongSocketRequest(final IEspDevice device, final IEspDeviceStatus status,
            final Runnable disconnectedCallback, long timestamp) {
            this.device = device;
            this.status = status;
            this.state = device.getDeviceState();
            this.disconnectedCallback = disconnectedCallback;
            this.timestamp = timestamp;
        }
        
        static EspLongSocketRequest createInstance(final IEspDevice device, final IEspDeviceStatus status,
            Runnable disconnectedCallback, long timestamp)
        {
            EspLongSocketRequest instance =
                new EspLongSocketRequest(device, status,
                    disconnectedCallback, timestamp);
            return instance;
        }
        
        void execute()
        {
            log.debug("EspLongSocketRequest execute()");
            // at present, only light support EspLongSocket
            if (status instanceof IEspStatusLight)
            {
                IEspStatusLight statusLight = (IEspStatusLight)status;
                // device is local(local is prior to internet)
                if (state.isStateLocal())
                {
                    IEspCommandLightPostStatusLocal command = new EspCommandLightPostStatusLocal();
                    command.doCommandLightPostStatusLocalInstantly(device, statusLight, disconnectedCallback);
                }
                // device is internet
                else
                {
                    IEspCommandLightPostStatusInternet command = new EspCommandLightPostStatusInternet();
                    boolean isSuc = command.doCommandLightPostStatusInternet(device, statusLight);
                    if (!isSuc)
                    {
                        disconnectedCallback.run();
                    }
                }
            }
        }
        final IEspDevice device;
        
        final IEspDeviceStatus status;
        
        final IEspDeviceState state;
        
        final Runnable disconnectedCallback;
        
        final long timestamp;
    }
    
    private static final Logger log = Logger.getLogger(EspActionLongSocket.class);
    
    private EspActionLongSocket()
    {
        mTaskDeque = new LinkedBlockingDeque<EspLongSocketRequest>();
    }
    
    public static EspActionLongSocket createInstance()
    {
        EspActionLongSocket instance = new EspActionLongSocket();
        return instance;
    }
    
    private final LinkedBlockingDeque<EspLongSocketRequest> mTaskDeque;
    
    private volatile boolean mIsExecuted = false;
    
    private final long MIN_INTERVAL = 100;
    
    private volatile EspLongSocketRequest mLastTask;
    
    private volatile Thread mBackgroundThread;
    
    // background thread to execute the post command instantly
    private final Runnable mBackgroudTask = new Runnable()
    {
        @Override
        public void run()
        {
            while (true)
            {
                // execute the last request and clear the mTaskDeque
                try
                {
                    EspLongSocketRequest task = mTaskDeque.takeLast();
                    mTaskDeque.clear();
                    if (mIsExecuted)
                    {
                        task.execute();
                    }
                }
                catch (InterruptedException e)
                {
                    return;
                }
            }
        }
    };
    
    // check whether the request is valid
    private boolean isValid(String deviceKey, boolean isMeshDevice, InetAddress inetAddress, String bssid,
        IEspDeviceStatus status, IEspDeviceState state, Runnable disconnectedCallback)
    {
        if (status == null || state == null)
        {
            return false;
        }
        if (state.isStateLocal())
        {
            if (isMeshDevice && bssid == null)
            {
                return false;
            }
            if (inetAddress == null)
            {
                return false;
            }
            else
            {
                return true;
            }
        }
        else if (state.isStateInternet())
        {
            if (deviceKey == null)
            {
                return false;
            }
            else
            {
                return true;
            }
        }
        else
        {
            return false;
        }
    }
    
    // check whether it is necessary to add the request into task queue
    private boolean isNecessary(boolean isMeshDevice, InetAddress inetAddress, String bssid, IEspDeviceStatus status,
        IEspDeviceState state, Runnable disconnectedCallback, long timestamp)
    {
        if (mLastTask == null)
        {
            return true;
        }
        if (timestamp - mLastTask.timestamp <= MIN_INTERVAL)
        {
            log.debug("isNecessary() timestamp isn't big enough, return false");
            return false;
        }
        if (mLastTask.status.equals(status))
        {
            log.debug("isNecessary() status is equal, return false");
            return false;
        }
        return true;
    }
    
    @Override
    public void addStatus(IEspDevice device, IEspDeviceStatus status, final Runnable disconnectedCallback)
    {
        String deviceKey = device.getKey();
        String bssid = device.getBssid();
        boolean isMeshDevice = device.getIsMeshDevice();
        InetAddress inetAddress = device.getInetAddress();
        IEspDeviceState state = device.getDeviceState();
        
        // check whether parameters are valid
        boolean isValid = isValid(deviceKey, isMeshDevice, inetAddress, bssid, status, state, disconnectedCallback);
        if (!isValid)
        {
            throw new IllegalArgumentException("addStatus() parameters are invalid");
        }
        long timestamp = System.currentTimeMillis();
        // check whether it is necessary to add status into task queue
        boolean isNecessary =
            isNecessary(isMeshDevice, inetAddress, bssid, status, state, disconnectedCallback, timestamp);
        log.debug("addStatus() isNecessary: " + isNecessary);
        // add status into task queue
        if (isNecessary)
        {
            Runnable disconnectedCallback2 = new Runnable()
            {
                @Override
                public void run()
                {
                    stop();
                    if (disconnectedCallback != null)
                    {
                        disconnectedCallback.run();
                    }
                }
            };
            EspLongSocketRequest request =
                EspLongSocketRequest.createInstance(device, status, disconnectedCallback2, timestamp);
            mLastTask = request;
            mTaskDeque.addLast(request);
        }
    }
    
    @Override
    public void start()
    {
        log.info("EspActionLongSocket start()");
        mIsExecuted = true;
        mBackgroundThread = new Thread(mBackgroudTask);
        mBackgroundThread.start();
    }
    
    @Override
    public void stop()
    {
        log.info("EspActionLongSocket stop()");
        mIsExecuted = false;
        if (mBackgroundThread != null)
        {
            mBackgroundThread.interrupt();
        }
        mBackgroundThread = null;
    }
    
}
