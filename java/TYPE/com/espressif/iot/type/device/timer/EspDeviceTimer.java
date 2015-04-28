package com.espressif.iot.type.device.timer;

import java.util.List;

public abstract class EspDeviceTimer
{
    public static final String TIMER_TYPE_FIXEDTIME = "FIXEDTIME";
    
    public static final String TIMER_TYPE_LOOP_PERIOD = "LOOP_PERIOD";
    
    public static final String TIMER_TYPE_LOOP_IN_WEEK = "LOOP_IN_WEEK";
    
    private long mId;
    
    private String mTypeStr;
    
    public EspDeviceTimer(long id, String type)
    {
        mId = id;
        mTypeStr = type;
    }
    
    public long getId()
    {
        return mId;
    }
    
    public String getTimerType()
    {
        return mTypeStr;
    }
    
    public abstract List<EspDeviceTimeAction> getTimerActions();
    
    @Override
    public String toString()
    {
        return "id=" + mId + " type=" + mTypeStr + " ";
    }
}
