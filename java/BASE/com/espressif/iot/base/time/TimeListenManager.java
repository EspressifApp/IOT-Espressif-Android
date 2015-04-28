package com.espressif.iot.base.time;

import com.espressif.iot.base.application.EspApplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

public class TimeListenManager
{
    private Context mContext;
    
    private BroadcastReceiver mReceiver;
    
    /*
     * Singleton lazy initialization start
     */
    private TimeListenManager()
    {
        mContext = EspApplication.sharedInstance();
        mReceiver = new TimeBroadcastReceiver();
    }
    
    private static class InstanceHolder
    {
        static TimeListenManager instance = new TimeListenManager();
    }
    
    public static TimeListenManager getInstance()
    {
        return InstanceHolder.instance;
    }
    
    /*
     * Singleton lazy initialization end
     */
    
    public void registerReceiver()
    {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        filter.addAction(Intent.ACTION_TIME_CHANGED);
        filter.addAction(Intent.ACTION_DATE_CHANGED);
        mContext.registerReceiver(mReceiver, filter);
    }
    
    public void unregisterReceiver()
    {
        mContext.unregisterReceiver(mReceiver);
    }
}
