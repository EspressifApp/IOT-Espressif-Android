package com.espressif.iot.ui.device.timer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TimePicker;

import com.espressif.iot.R;
import com.espressif.iot.type.device.timer.EspDeviceFixedTimeTimer;
import com.espressif.iot.type.device.timer.EspDeviceTimer;

public abstract class DeviceTimerEditFixedTimerActivityAbs extends DeviceTimerEditActivityAbs
{
    private DatePicker mDatePicker;
    
    private TimePicker mTimePicker;
    
    private Spinner mActionSpinner;
    
    private ArrayAdapter<String> mActionsAdapter;
    
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.device_timer_plug_fixed_time_edit);
        
        mDatePicker = (DatePicker)findViewById(R.id.date_picker);
        mTimePicker = (TimePicker)findViewById(R.id.time_picker);
        mTimePicker.setIs24HourView(true);
        
        mActionSpinner = (Spinner)findViewById(R.id.timer_action);
        mActionsAdapter =
            new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, mActionTitles);
        mActionSpinner.setAdapter(mActionsAdapter);
        mActionSpinner.setSelection(0);
        
        if (mTimer != null)
        {
            setGotData();
        }
    }
    
    @Override
    protected JSONObject getPostJSON()
    {
        JSONObject json = new JSONObject();
        try
        {
            JSONObject timerJSON = new JSONObject();
            
            if (mTimer != null)
            {
                timerJSON.put(KEY_TIMER_ID, mTimer.getId());
            }
            
            timerJSON.put(KEY_TIMER_TYPE, EspDeviceTimer.TIMER_TYPE_FIXEDTIME);
            
            JSONObject timeActionJson = new JSONObject();
            
            String time =
                mDatePicker.getYear() + matchingTimeStr(mDatePicker.getMonth() + 1)
                    + matchingTimeStr(mDatePicker.getDayOfMonth()) + matchingTimeStr(mTimePicker.getCurrentHour()) + ""
                    + matchingTimeStr(mTimePicker.getCurrentMinute()) + "" + "00";
            timeActionJson.put(KEY_TIMER_TIME, time);
            
            String action = getEditAction();
            timeActionJson.put(KEY_TIMER_ACTION, action);
            
            JSONArray timerActionArray = new JSONArray();
            timerActionArray.put(timeActionJson);
            
            timerJSON.put(KEY_TIMER_TIME_ACTION, timerActionArray);
            
            JSONArray timersArray = new JSONArray();
            timersArray.put(timerJSON);
            
            json.put(KEY_TIMERS_ARRAY, timersArray);
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        return json;
    }
    
    @Override
    protected void setGotData()
    {
        // the timeStr is yyyyMMddhhmmss
        EspDeviceFixedTimeTimer timer = (EspDeviceFixedTimeTimer)mTimer;
        String timeStr = timer.getTimeAction().get(0).getTime();
        int year = Integer.parseInt(timeStr.substring(0, 4));
        int month = Integer.parseInt(timeStr.substring(4, 6));
        int day = Integer.parseInt(timeStr.substring(6, 8));
        int hour = Integer.parseInt(timeStr.substring(8, 10));
        int minute = Integer.parseInt(timeStr.substring(10, 12));
        mDatePicker.init(year, month - 1, day, null);
        mTimePicker.setCurrentHour(hour);
        mTimePicker.setCurrentMinute(minute);
        
        String action = getTimerAction(timer);
        for (int i = 0; i < mActionValues.length; i++)
        {
            if (action.equals(mActionValues[i]))
            {
                mActionSpinner.setSelection(i);
                break;
            }
        }
    }

    protected Spinner getActionSpinner() {
        return mActionSpinner;
    }

    /**
     * 
     * @return the new action need post
     */
    abstract protected String getEditAction();
    
    /**
     * 
     * @return the saved timer action
     */
    abstract protected String getTimerAction(EspDeviceFixedTimeTimer timer);
}
