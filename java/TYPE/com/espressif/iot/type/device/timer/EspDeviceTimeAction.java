package com.espressif.iot.type.device.timer;

public class EspDeviceTimeAction
{
    private String mTime;
    
    private String mAction;
    
    public EspDeviceTimeAction(String time, String action)
    {
        mTime = time;
        mAction = action;
    }
    
    public void setTime(String time)
    {
        mTime = time;
    }
    
    public String getTime()
    {
        return mTime;
    }
    
    public void setAction(String action)
    {
        mAction = action;
    }
    
    public String getAction()
    {
        return mAction;
    }
    
    @Override
    public String toString()
    {
        return "time=" + mTime + "  action=" + mAction;
    }
}
