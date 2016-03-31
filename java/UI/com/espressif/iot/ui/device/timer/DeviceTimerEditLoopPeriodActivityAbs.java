package com.espressif.iot.ui.device.timer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.espressif.iot.R;
import com.espressif.iot.type.device.timer.EspDeviceLoopPeriodTimer;
import com.espressif.iot.type.device.timer.EspDeviceTimer;

public abstract class DeviceTimerEditLoopPeriodActivityAbs extends DeviceTimerEditActivityAbs
{
    private EditText mTimeEdit;
    
    private Spinner mTimeUnitSpinner;
    
    private ArrayAdapter<String> mTimeUnitAdapter;
    
    private String[] mTimeUnits;
    
    private String[] mTimeUintValues;
    
    private Spinner mActionSpinner;
    
    private ArrayAdapter<String> mActionsAdapter;
    
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.device_timer_plug_loop_period_edit);
        
        mTimeEdit = (EditText)findViewById(R.id.loop_period_time_edit);
        
        mTimeUnitSpinner = (Spinner)findViewById(R.id.loop_period_unit_spinner);
        mTimeUnits = getResources().getStringArray(R.array.esp_device_timer_period_units);
        mTimeUintValues = getResources().getStringArray(R.array.esp_device_timer_period_unit_values);
        mTimeUnitAdapter =
            new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, mTimeUnits);
        mTimeUnitSpinner.setAdapter(mTimeUnitAdapter);
        mTimeUnitSpinner.setSelection(0);
        
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
            
            timerJSON.put(KEY_TIMER_TYPE, EspDeviceTimer.TIMER_TYPE_LOOP_PERIOD);
            
            String peroid = mTimeUintValues[mTimeUnitSpinner.getSelectedItemPosition()];
            timerJSON.put(KEY_TIMER_PEROID, peroid);
            
            if (TextUtils.isEmpty(mTimeEdit.getText()))
            {
                Toast.makeText(this, R.string.esp_device_timer_enter_time_message, Toast.LENGTH_LONG).show();
                return null;
            }
            long time = Long.parseLong(mTimeEdit.getText().toString());
            timerJSON.put(KEY_TIMER_TIME, time);
            
            String action = mActionValues[mActionSpinner.getSelectedItemPosition()];
            timerJSON.put(KEY_TIMER_ACTION, action);
            
            JSONArray timersArray = new JSONArray();
            timersArray.put(timerJSON);
            
            json.put(KEY_TIMERS_ARRAY, timersArray);
        }
        catch (JSONException e)
        {
            e.printStackTrace();
            return null;
        }
        return json;
    }
    
    @Override
    protected void setGotData()
    {
        EspDeviceLoopPeriodTimer timer = (EspDeviceLoopPeriodTimer)mTimer;
        String period = timer.getPeriod();
        for (int i = 0; i < mTimeUintValues.length; i++)
        {
            if (period.equals(mTimeUintValues[i]))
            {
                mTimeUnitSpinner.setSelection(i);
                break;
            }
        }
        
        int time = timer.getTime();
        mTimeEdit.setText(time + "");
        
        String action = timer.getAction();
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
    abstract protected String getTimerAction(EspDeviceLoopPeriodTimer timer);
}
