package com.espressif.iot.type.device.timer;

import java.util.ArrayList;
import java.util.List;

public class EspDeviceLoopPeriodTimer extends EspDeviceTimer
{
    /**
     * The unit of #mTime
     */
    private String mPeriod;
    
    private int mTime;
    
    private String mAction;
    
    public EspDeviceLoopPeriodTimer(long id, String type)
    {
        super(id, type);
    }
    
    public String getPeriod()
    {
        return mPeriod;
    }
    
    public void setPeriod(String period)
    {
        mPeriod = period;
    }
    
    public int getTime()
    {
        return mTime;
    }
    
    public void setTime(int time)
    {
        mTime = time;
    }
    
    public String getAction()
    {
        return mAction;
    }
    
    public void setAction(String action)
    {
        mAction = action;
    }
    
    @Override
    public String toString()
    {
        return super.toString() + "period=" + mPeriod + " time=" + mTime + " aciton=" + mAction;
    }
    
    @Override
    public List<EspDeviceTimeAction> getTimerActions()
    {
        List<EspDeviceTimeAction> actions = new ArrayList<EspDeviceTimeAction>();
        EspDeviceTimeAction action = new EspDeviceTimeAction(mPeriod + mTime, mAction);
        actions.add(action);
        return actions;
    }
}
