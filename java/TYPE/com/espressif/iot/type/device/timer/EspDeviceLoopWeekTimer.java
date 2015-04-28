package com.espressif.iot.type.device.timer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;


public class EspDeviceLoopWeekTimer extends EspDeviceTimer
{
    private HashSet<Integer> mWeekDays;
    
    private ArrayList<EspDeviceTimeAction> mTimeActionsList;
    
    public EspDeviceLoopWeekTimer(long id, String type)
    {
        super(id, type);
        mWeekDays = new HashSet<Integer>();
        mTimeActionsList = new ArrayList<EspDeviceTimeAction>();
    }
    
    public void addWeekDay(int whichDay)
    {
        mWeekDays.add(whichDay);
    }
    
    public HashSet<Integer> getWeekDays()
    {
        return mWeekDays;
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
        result += "weeks=[";
        for (Integer i : mWeekDays)
        {
            result += (i + " ");
        }
        result += ']';
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
