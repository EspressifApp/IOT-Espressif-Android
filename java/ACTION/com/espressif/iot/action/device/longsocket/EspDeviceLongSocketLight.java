package com.espressif.iot.action.device.longsocket;

import java.net.InetAddress;

import org.apache.log4j.Logger;

import com.espressif.iot.base.net.longsocket.EspLongSocket;
import com.espressif.iot.base.net.longsocket.IEspLongSocket;
import com.espressif.iot.base.net.rest.mesh.EspSocketRequestBaseEntity;
import com.espressif.iot.type.device.status.EspStatusLight;
import com.espressif.iot.type.device.status.IEspStatusLight;

public class EspDeviceLongSocketLight implements IEspDeviceLongSocketLight
{
    /*
     * Singleton lazy initialization start
     */
    private EspDeviceLongSocketLight()
    {
//        mLightParser = EspLightResultParser.getInstance();
        mLightBuilder = EspLightCommandBuilder.getInstance();
        mLastTimestamp = System.currentTimeMillis() - MIN_INTERVAL;
        mLightStatusLast = new EspStatusLight();
    }
    
    private static class InstanceHolder
    {
        static EspDeviceLongSocketLight instance = new EspDeviceLongSocketLight();
    }
    
    public static EspDeviceLongSocketLight getInstance()
    {
        return InstanceHolder.instance;
    }
    
    /*
     * Singleton lazy initialization end
     */
    
    private static final Logger log = Logger.getLogger(EspDeviceLongSocketLight.class);
    
    // the max size of task to be executed
    private static final int MAX_TASK_SIZE = 5;
    // the min interval of task to be added
    private static final long MIN_INTERVAL = 100;
    private EspLongSocket mLongSocket;
//    private IEspLightResultParser mLightParser;
    private IEspLightCommandBuilder mLightBuilder;
    private IEspStatusLight mLightStatusLast;
    private InetAddress mInetAddress;
    private String mDeviceKey;
    private long mLastTimestamp;
    
    private void __close()
    {
        if (mLongSocket != null)
        {
            mLongSocket.close();
        }
    }
    
    @Override
    public void finish()
    {
        log.debug(Thread.currentThread().toString() + "##finish()");
        if (mLongSocket != null)
        {
            mLongSocket.finish();
        }
    }
    
    @Override
    public void close()
    {
        log.debug(Thread.currentThread().toString() + "##close()");
        __close();
    }
    
    @Override
    public boolean connectLightLocal(InetAddress inetAddress, IEspLongSocket.EspLongSocketDisconnected listener)
    {
        log.debug(Thread.currentThread().toString() + "##connectLightLocal()");
        if (mLongSocket != null)
        {
            mLongSocket.close();
        }
        mLongSocket = new EspLongSocket();
        mInetAddress = inetAddress;
        String targetHost = inetAddress.getHostAddress();
        mLongSocket.setTarget(targetHost, IEspLongSocket.DEVICE_PORT, MAX_TASK_SIZE);
        mLongSocket.setEspLongSocketDisconnectedListener(listener);
        return mLongSocket.connect();
    }
    
    @Override
    public boolean connectLightInternet(String deviceKey, IEspLongSocket.EspLongSocketDisconnected listener)
    {
        log.debug(Thread.currentThread().toString() + "##connectLightInternet()");
        if (mLongSocket != null)
        {
            mLongSocket.close();
        }
        mLongSocket = new EspLongSocket();
        mDeviceKey = deviceKey;
        String targetHost = "iot.espressif.cn";
        mLongSocket.setTarget(targetHost, IEspLongSocket.SERVER_PORT, MAX_TASK_SIZE);
        return mLongSocket.connect();
    }

    @Override
    public void addLigthtStatusLocal(IEspStatusLight statusLight)
    {
        if (!statusLight.equals(this.mLightStatusLast))
        {
            long cost = System.currentTimeMillis() - this.mLastTimestamp;
            if (cost > MIN_INTERVAL)
            {
                log.debug(Thread.currentThread().toString() + "##addLigthtStatusLocal()");
                EspSocketRequestBaseEntity request = mLightBuilder.buildLocalPostStatusRequest(mInetAddress, statusLight, null);
                mLongSocket.addRequest(request);
                mLastTimestamp = System.currentTimeMillis();
                // copy current light status to last light status
                mLightStatusLast.setPeriod(statusLight.getPeriod());
                mLightStatusLast.setRed(statusLight.getRed());
                mLightStatusLast.setGreen(statusLight.getGreen());
                mLightStatusLast.setBlue(statusLight.getBlue());
            }
        }
    }

    @Override
    public void addLigthStatusInternet(IEspStatusLight statusLight)
    {
        if (!statusLight.equals(this.mLightStatusLast))
        {
            long cost = System.currentTimeMillis() - this.mLastTimestamp;
            if (cost > MIN_INTERVAL)
            {
                log.debug(Thread.currentThread().toString() + "##addLigthStatusInternet()");
                EspSocketRequestBaseEntity request = mLightBuilder.buildInternetPostStatusRequest(mDeviceKey, statusLight, null);
                mLongSocket.addRequest(request);
                mLastTimestamp = System.currentTimeMillis();
                // copy current light status to last light status
                mLightStatusLast.setPeriod(statusLight.getPeriod());
                mLightStatusLast.setRed(statusLight.getRed());
                mLightStatusLast.setGreen(statusLight.getGreen());
                mLightStatusLast.setBlue(statusLight.getBlue());
            }
        }
    }
    
}
