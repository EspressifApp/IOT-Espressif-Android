package com.espressif.iot.action.device.esptouch;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import com.espressif.iot.command.device.esptouch.EspCommandDeviceEsptouch;
import com.espressif.iot.command.device.esptouch.IEspCommandDeviceEsptouch;
import com.espressif.iot.esptouch.IEsptouchListener;
import com.espressif.iot.esptouch.IEsptouchResult;

public class EspActionDeviceEsptouch implements IEspActionDeviceEsptouch
{
    
    private static final AtomicBoolean IS_ACTION_RUNNING = new AtomicBoolean(false);
    
    private IEspCommandDeviceEsptouch mCommandEsptouch;
    
    private AtomicBoolean mIsDone = new AtomicBoolean(false);
    
    public EspActionDeviceEsptouch()
    {
        mCommandEsptouch = new EspCommandDeviceEsptouch();
    }
    
    @Override
    public List<IEsptouchResult> doActionDeviceEsptouch(int expectTaskResultCount, String apSsid, String apBssid,
        String apPassword, boolean isSsidHidden, int timeoutMillisecond)
    {
        if (IS_ACTION_RUNNING.get())
        {
            // for the esptouch will occupy the fix port, so if you call it more than once at the same time,
            // except the first time, other time will be failed forever. to prevent you from the abnormal situation,
            // we return null
            return null;
            // throw new IllegalArgumentException(
            // "EspActionDeviceEsptouch is running already, it can't be executed before the backgroud task finished");
        }
        IS_ACTION_RUNNING.set(true);
        List<IEsptouchResult> result =
            mCommandEsptouch.doCommandDeviceEsptouch(expectTaskResultCount,
                apSsid,
                apBssid,
                apPassword,
                isSsidHidden,
                timeoutMillisecond);
        IS_ACTION_RUNNING.set(false);
        return result;
    }
    
    @Override
    public List<IEsptouchResult> doActionDeviceEsptouch(int expectTaskResultCount, String apSsid, String apBssid,
        String apPassword, boolean isSsidHidden, int timeoutMillisecond, IEsptouchListener esptouchListener)
    {
        if (IS_ACTION_RUNNING.get())
        {
            // for the esptouch will occupy the fix port, so if you call it more than once at the same time,
            // except the first time, other time will be failed forever. to prevent you from the abnormal situation,
            // we return null
            return null;
            // throw new IllegalArgumentException(
            // "EspActionDeviceEsptouch is running already, it can't be executed before the backgroud task finished");
        }
        IS_ACTION_RUNNING.set(true);
        List<IEsptouchResult> result =
            mCommandEsptouch.doCommandDeviceEsptouch(expectTaskResultCount,
                apSsid,
                apBssid,
                apPassword,
                isSsidHidden,
                timeoutMillisecond,
                esptouchListener);
        IS_ACTION_RUNNING.set(false);
        return result;
    }
    
    @Override
    public List<IEsptouchResult> doActionDeviceEsptouch(int expectTaskResultCount, String apSsid, String apBssid,
        String apPassword, boolean isSsidHidden)
    {
        if (IS_ACTION_RUNNING.get())
        {
            // for the esptouch will occupy the fix port, so if you call it more than once at the same time,
            // except the first time, other time will be failed forever. to prevent you from the abnormal situation,
            // we return null
            return null;
            // throw new IllegalArgumentException(
            // "EspActionDeviceEsptouch is running already, it can't be executed before the backgroud task finished");
        }
        IS_ACTION_RUNNING.set(true);
        List<IEsptouchResult> result =
            mCommandEsptouch.doCommandDeviceEsptouch(expectTaskResultCount, apSsid, apBssid, apPassword, isSsidHidden);
        IS_ACTION_RUNNING.set(false);
        return result;
    }
    
    @Override
    public List<IEsptouchResult> doActionDeviceEsptouch(int expectTaskResultCount, String apSsid, String apBssid,
        String apPassword, boolean isSsidHidden, IEsptouchListener esptouchListener)
    {
        if (IS_ACTION_RUNNING.get())
        {
            // for the esptouch will occupy the fix port, so if you call it more than once at the same time,
            // except the first time, other time will be failed forever. to prevent you from the abnormal situation,
            // we return null
            return null;
            // throw new IllegalArgumentException(
            // "EspActionDeviceEsptouch is running already, it can't be executed before the backgroud task finished");
        }
        IS_ACTION_RUNNING.set(true);
        List<IEsptouchResult> result =
            mCommandEsptouch.doCommandDeviceEsptouch(expectTaskResultCount,
                apSsid,
                apBssid,
                apPassword,
                isSsidHidden,
                esptouchListener);
        IS_ACTION_RUNNING.set(false);
        return result;
    }
    
    @Override
    public boolean isCancelled()
    {
        return mCommandEsptouch.isCancelled();
    }
    
    @Override
    public void cancel()
    {
        mCommandEsptouch.cancel();
    }
    
    @Override
    public boolean isExecuted()
    {
        return IS_ACTION_RUNNING.get();
    }
    
    @Override
    public void done()
    {
        cancel();
        mIsDone.set(true);
    }
    
    @Override
    public boolean isDone()
    {
        return mIsDone.get();
    }
    
}
