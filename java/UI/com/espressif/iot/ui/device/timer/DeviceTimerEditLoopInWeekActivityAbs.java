package com.espressif.iot.ui.device.timer;

import java.util.ArrayList;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TimePicker;

import com.espressif.iot.R;
import com.espressif.iot.type.device.timer.EspDeviceLoopWeekTimer;
import com.espressif.iot.type.device.timer.EspDeviceTimer;

public abstract class DeviceTimerEditLoopInWeekActivityAbs extends DeviceTimerEditActivityAbs
{
    private final int mWeekCheckBoxIds[] = new int[] {R.id.week_sunday, R.id.week_monday, R.id.week_tuesday,
        R.id.week_wednesday, R.id.week_thursday, R.id.week_friday, R.id.week_saturday};
    
    private ArrayList<CheckBox> mWeekCheckBoxList;
    
    private TimePicker mTimePicker;
    
    private Spinner mActionSpinner;
    
    private ArrayAdapter<String> mActionsAdapter;
    
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.device_timer_plug_loop_in_week_edit);
        
        mWeekCheckBoxList = new ArrayList<CheckBox>();
        for (int i = 0; i < mWeekCheckBoxIds.length; i++)
        {
            CheckBox weekCB = (CheckBox)findViewById(mWeekCheckBoxIds[i]);
            mWeekCheckBoxList.add(weekCB);
        }
        
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
            
            timerJSON.put(KEY_TIMER_TYPE, EspDeviceTimer.TIMER_TYPE_LOOP_IN_WEEK);
            
            JSONArray weekdaysArray = new JSONArray();
            for (int i = 0; i < mWeekCheckBoxList.size(); i++)
            {
                if (mWeekCheckBoxList.get(i).isChecked())
                {
                    weekdaysArray.put(i);
                }
            }
            timerJSON.put(KEY_TIMER_WEEKDAYS, weekdaysArray);
            
            JSONObject timeActionJson = new JSONObject();
            String time =
                matchingTimeStr(mTimePicker.getCurrentHour()) + "" + matchingTimeStr(mTimePicker.getCurrentMinute())
                    + "" + "00";
            timeActionJson.put(KEY_TIMER_TIME, time);
            
            String action = getEditAction();
            timeActionJson.put(KEY_TIMER_ACTION, action);
            
            JSONArray timeActionsArray = new JSONArray();
            timeActionsArray.put(timeActionJson);
            timerJSON.put(KEY_TIMER_TIME_ACTION, timeActionsArray);
            
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
        // timeStr is hhmmss
        EspDeviceLoopWeekTimer timer = (EspDeviceLoopWeekTimer)mTimer;
        String timeStr = timer.getTimeAction().get(0).getTime();
        int hour = Integer.parseInt(timeStr.substring(0, 2));
        int minute = Integer.parseInt(timeStr.substring(2, 4));
        mTimePicker.setCurrentHour(hour);
        mTimePicker.setCurrentMinute(minute);
        
        Set<Integer> weekdays = timer.getWeekDays();
        for (Integer i : weekdays)
        {
            mWeekCheckBoxList.get(i).setChecked(true);
        }
        
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
    abstract protected String getTimerAction(EspDeviceLoopWeekTimer timer);
}
