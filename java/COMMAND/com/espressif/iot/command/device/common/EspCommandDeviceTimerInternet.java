package com.espressif.iot.command.device.common;

import java.util.List;

import org.apache.http.HttpStatus;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.espressif.iot.base.api.EspBaseApiUtil;
import com.espressif.iot.device.IEspDevice;
import com.espressif.iot.type.device.timer.EspDeviceFixedTimeTimer;
import com.espressif.iot.type.device.timer.EspDeviceLoopPeriodTimer;
import com.espressif.iot.type.device.timer.EspDeviceLoopWeekTimer;
import com.espressif.iot.type.device.timer.EspDeviceTimeAction;
import com.espressif.iot.type.device.timer.EspDeviceTimer;
import com.espressif.iot.type.device.timer.EspDeviceTimerJSONKey;
import com.espressif.iot.type.net.HeaderPair;

public class EspCommandDeviceTimerInternet implements IEspCommandDeviceTimerInternet, EspDeviceTimerJSONKey
{
    private final Logger log = Logger.getLogger(EspCommandDeviceTimerInternet.class);
    
    private final IEspDevice mDevice;
    
    private final List<EspDeviceTimer> mTimerList;
    
    public EspCommandDeviceTimerInternet(IEspDevice device)
    {
        mDevice = device;
        mTimerList = device.getTimerList();
    }
    
    @Override
    public boolean doCommandDeviceTimerPost(JSONObject timerJSON)
    {
        String url;
        try
        {
            // if timerJSON contain KEY_TIMER_ID, post edit command
            timerJSON.getJSONArray(KEY_TIMERS_ARRAY).getJSONObject(0).getLong(KEY_TIMER_ID);
            url = URL_EDIT;
        }
        catch (JSONException e)
        {
            // timerJSON doesn't contain KEY_TIMER_ID, post new command
            url = URL_NEW;
        }
        log.debug("Device timer post JSON = " + timerJSON.toString());
        log.debug("Device timer post url = " + url);
        
        HeaderPair header = generateHeader(mDevice.getKey());
        JSONObject result = EspBaseApiUtil.Post(url, timerJSON, header);
        
        if (result == null)
        {
            return false;
        }
        
        try
        {
            int status = result.getInt(Status);
            if (status == HttpStatus.SC_OK)
            {
                return true;
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        return false;
    }
    
    @Override
    public boolean doCommandDeviceTimerGet()
    {
        return getTimers();
    }
    
    @Override
    public boolean doCommandDeviceTimerDelete(long timerId)
    {
        JSONObject json = new JSONObject();
        JSONArray timersArray = new JSONArray();
        JSONObject idJSON = new JSONObject();
        try
        {
            idJSON.put(KEY_TIMER_ID, timerId);
            timersArray.put(idJSON);
            json.put(KEY_TIMERS_ARRAY, timersArray);
        }
        catch (JSONException e)
        {
            e.printStackTrace();
            return false;
        }
        log.debug("Device timer delete JSON = " + json.toString());
        
        HeaderPair header = generateHeader(mDevice.getKey());
        JSONObject result = EspBaseApiUtil.Post(URL_DELETE, json, header);
        
        if (result == null)
        {
            return false;
        }
        
        try
        {
            int status = result.getInt(Status);
            if (status == HttpStatus.SC_OK)
            {
                // Delete the timer from timer list
                for (EspDeviceTimer timer : mTimerList)
                {
                    if (timer.getId() == timerId)
                    {
                        mTimerList.remove(timer);
                        return true;
                    }
                }
                return true;
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        
        return false;
    }
    
    private boolean getTimers()
    {
        HeaderPair header = generateHeader(mDevice.getKey());
        JSONObject result = EspBaseApiUtil.Get(URL_GET, header);
        
        if (result == null)
        {
            return false;
        }
        
        try
        {
            int status = result.getInt(Status);
            if (status != HttpStatus.SC_OK)
            {
                return false;
            }
            
            mTimerList.clear();
            
            JSONArray timersArray = result.getJSONArray(KEY_TIMERS_ARRAY);
            if (timersArray != null)
            {
                for (int i = 0; i < timersArray.length(); i++)
                {
                    JSONObject json = timersArray.getJSONObject(i);
                    
                    try
                    {
                        long id = json.getLong(KEY_TIMER_ID);
                        String type = json.getString(KEY_TIMER_TYPE);
                        
                        if (type.equals(EspDeviceTimer.TIMER_TYPE_FIXEDTIME))
                        {
                            getFixedTimeTimer(json, id, type);
                        }
                        else if (type.equals(EspDeviceTimer.TIMER_TYPE_LOOP_PERIOD))
                        {
                            getLoopPeriodTimer(json, id, type);
                        }
                        else if (type.equals(EspDeviceTimer.TIMER_TYPE_LOOP_IN_WEEK))
                        {
                            getLoopInWeekTimer(json, id, type);
                        }
                    }
                    catch (JSONException e)
                    {
                        e.printStackTrace();
                    }
                }
                return true;
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        return false;
    }
    
    private void getFixedTimeTimer(JSONObject json, long id, String type)
        throws JSONException
    {
        EspDeviceFixedTimeTimer fixedTimer = new EspDeviceFixedTimeTimer(id, type);
        
        JSONArray actionArray = json.getJSONArray(KEY_TIMER_TIME_ACTION);
        for (int i = 0; i < actionArray.length(); i++)
        {
            JSONObject jsonAction = actionArray.getJSONObject(i);
            String time = jsonAction.getString(KEY_TIMER_TIME);
            String action = jsonAction.getString(KEY_TIMER_ACTION);
            EspDeviceTimeAction timeAction = new EspDeviceTimeAction(time, action);
            fixedTimer.addTimeAction(timeAction);
            mTimerList.add(fixedTimer);
        }
    }
    
    private void getLoopPeriodTimer(JSONObject json, long id, String type)
        throws JSONException
    {
        EspDeviceLoopPeriodTimer loopPeriodTimer = new EspDeviceLoopPeriodTimer(id, type);
        String period = json.getString(KEY_TIMER_PEROID);
        int time = json.getInt(KEY_TIMER_TIME);
        String action = json.getString(KEY_TIMER_ACTION);
        loopPeriodTimer.setPeriod(period);
        loopPeriodTimer.setTime(time);
        loopPeriodTimer.setAction(action);
        mTimerList.add(loopPeriodTimer);
    }
    
    private void getLoopInWeekTimer(JSONObject json, long id, String type)
        throws JSONException
    {
        EspDeviceLoopWeekTimer loopWeekTimer = new EspDeviceLoopWeekTimer(id, type);
        JSONArray weekDaysArray = json.getJSONArray(KEY_TIMER_WEEKDAYS);
        for (int i = 0; i < weekDaysArray.length(); i++)
        {
            loopWeekTimer.addWeekDay(weekDaysArray.getInt(i));
        }
        JSONArray actionArray = json.getJSONArray(KEY_TIMER_TIME_ACTION);
        for (int i = 0; i < actionArray.length(); i++)
        {
            JSONObject jsonAction = actionArray.getJSONObject(i);
            String time = jsonAction.getString(KEY_TIMER_TIME);
            String action = jsonAction.getString(KEY_TIMER_ACTION);
            EspDeviceTimeAction timeAction = new EspDeviceTimeAction(time, action);
            loopWeekTimer.addTimeAction(timeAction);
            mTimerList.add(loopWeekTimer);
        }
    }
    
    private HeaderPair generateHeader(String value)
    {
        return new HeaderPair(Authorization, "token " + value);
    }
}
