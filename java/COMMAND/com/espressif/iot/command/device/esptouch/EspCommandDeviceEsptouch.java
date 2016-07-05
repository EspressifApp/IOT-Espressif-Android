package com.espressif.iot.command.device.esptouch;

import java.util.List;

import android.content.Context;

import com.espressif.iot.base.application.EspApplication;
import com.espressif.iot.esptouch.EsptouchTask;
import com.espressif.iot.esptouch.IEsptouchListener;
import com.espressif.iot.esptouch.IEsptouchResult;
import com.espressif.iot.esptouch.IEsptouchTask;

public class EspCommandDeviceEsptouch implements IEspCommandDeviceEsptouch
{
    // without the lock, if the user tap confirm and cancel quickly enough,
    // the bug will arise. the reason is follows:
    // 0. task is starting created, but not finished
    // 1. the task is cancel for the task hasn't been created, it do nothing
    // 2. task is created
    // 3. Oops, the task should be cancelled, but it is running
    private final Object mLock = new Object();
    
    private IEsptouchTask mEsptouchTask;
    
    private volatile boolean mIsCancelled;
    
    @Override
    public List<IEsptouchResult> doCommandDeviceEsptouch(int expectTaskResultCount, String apSsid, String apBssid,
        String apPassword, boolean isSsidHidden, int timeoutMillisecond)
    {
        return doCommandDeviceEsptouch(expectTaskResultCount,
            apSsid,
            apBssid,
            apPassword,
            isSsidHidden,
            timeoutMillisecond,
            null);
    }
    
    @Override
    public List<IEsptouchResult> doCommandDeviceEsptouch(int expectTaskResultCount, String apSsid, String apBssid,
        String apPassword, boolean isSsidHidden, int timeoutMillisecond, IEsptouchListener esptouchListener)
    {
        synchronized (mLock)
        {
            if (mIsCancelled)
            {
                return null;
            }
            Context context = EspApplication.sharedInstance().getContext();
            mEsptouchTask = new EsptouchTask(apSsid, apBssid, apPassword, isSsidHidden, timeoutMillisecond, context);
            mEsptouchTask.setEsptouchListener(esptouchListener);
        }
        return mEsptouchTask.executeForResults(expectTaskResultCount);
    }
    
    @Override
    public List<IEsptouchResult> doCommandDeviceEsptouch(int expectTaskResultCount, String apSsid, String apBssid,
        String apPassword, boolean isSsidHidden)
    {
        return doCommandDeviceEsptouch(expectTaskResultCount, apSsid, apBssid, apPassword, isSsidHidden, null);
    }
    
    @Override
    public List<IEsptouchResult> doCommandDeviceEsptouch(int expectTaskResultCount, String apSsid, String apBssid,
        String apPassword, boolean isSsidHidden, IEsptouchListener esptouchListener)
    {
        synchronized (mLock)
        {
            if (mIsCancelled)
            {
                return null;
            }
            Context context = EspApplication.sharedInstance().getContext();
            mEsptouchTask = new EsptouchTask(apSsid, apBssid, apPassword, isSsidHidden, context);
            mEsptouchTask.setEsptouchListener(esptouchListener);
        }
        return mEsptouchTask.executeForResults(expectTaskResultCount);
    }
    
    @Override
    public boolean isCancelled()
    {
        synchronized (mLock)
        {
            return mIsCancelled;
        }
    }
    
    @Override
    public void cancel()
    {
        synchronized (mLock)
        {
            if (mEsptouchTask != null)
            {
                mEsptouchTask.interrupt();
            }
            mIsCancelled = true;
        }
    }
}
