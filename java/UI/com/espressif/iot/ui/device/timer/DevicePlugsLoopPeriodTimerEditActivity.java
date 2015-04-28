package com.espressif.iot.ui.device.timer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.espressif.iot.device.IEspDevicePlugs;
import com.espressif.iot.type.device.timer.EspDeviceLoopPeriodTimer;
import com.espressif.iot.util.EspStrings;

import android.os.Bundle;

public class DevicePlugsLoopPeriodTimerEditActivity extends DevicePlugLoopPeriodTimerEditActivity
{
    private String mValue;
    
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        mValue = mIntentBundle.getString(EspStrings.Key.DEVICE_TIMER_PLUGS_VALUE_KEY);
    }
    
    @Override
    protected JSONObject getPostJSON()
    {
        JSONObject json = super.getPostJSON();
        try
        {
            JSONArray array = json.getJSONArray(KEY_TIMERS_ARRAY);
            for (int i = 0; i < array.length(); i++)
            {
                JSONObject actionJSON = array.getJSONObject(i);
                String action = actionJSON.getString(KEY_TIMER_ACTION);
                action += mValue;
                actionJSON.put(KEY_TIMER_ACTION, action);
            }
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
        super.setGotData();
        
        EspDeviceLoopPeriodTimer timer = (EspDeviceLoopPeriodTimer)mTimer;
        String action = timer.getAction();
        action = action.substring(0, action.length() - IEspDevicePlugs.TIMER_TAIL_LENGTH);
        for (int i = 0; i < mActionValues.length; i++)
        {
            if (action.equals(mActionValues[i]))
            {
                mActionSpinner.setSelection(i);
                break;
            }
        }
    }
}
