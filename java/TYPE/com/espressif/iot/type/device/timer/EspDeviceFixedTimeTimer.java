package com.espressif.iot.type.device.timer;

import java.util.ArrayList;
import java.util.List;


public class EspDeviceFixedTimeTimer extends EspDeviceTimer
{
    private ArrayList<EspDeviceTimeAction> mTimeActionsList;
    
    public EspDeviceFixedTimeTimer(long id, String type)
    {
        super(id, type);
        mTimeActionsList = new ArrayList<EspDeviceTimeAction>();
    }
    
    public ArrayList<EspDeviceTimeAction> getTimeAction()
    {
        return mTimeActionsList;
    }
    
    public void addTimeAction(EspDeviceTimeAction timeAction)
    {
        mTimeActionsList.add(timeAction);
    }
    
    @Override
    public String toString()
    {
        String result = "";
        for (EspDeviceTimeAction dta : mTimeActionsList)
        {
            result = dta.toString() + " ";
        }
        return super.toString() + result;
    }

    @Override
    public List<EspDeviceTimeAction> getTimerActions()
    {
        List<EspDeviceTimeAction> actions = new ArrayList<EspDeviceTimeAction>();
        actions.addAll(mTimeActionsList);
        return actions;
    }
}
