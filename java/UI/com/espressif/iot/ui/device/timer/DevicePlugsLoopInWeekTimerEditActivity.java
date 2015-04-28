package com.espressif.iot.ui.device.timer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.espressif.iot.device.IEspDevicePlugs;
import com.espressif.iot.type.device.timer.EspDeviceLoopWeekTimer;
import com.espressif.iot.util.EspStrings;

import android.os.Bundle;

public class DevicePlugsLoopInWeekTimerEditActivity extends DevicePlugLoopInWeekTimerEditActivity
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
                JSONObject jsonInArray = array.getJSONObject(i);
                JSONArray actionArray = jsonInArray.getJSONArray(KEY_TIMER_TIME_ACTION);
                for (int j = 0; j < actionArray.length(); j++)
                {
                    JSONObject actionJSON = actionArray.getJSONObject(j);
                    String action = actionJSON.getString(KEY_TIMER_ACTION);
                    action += mValue;
                    actionJSON.put(KEY_TIMER_ACTION, action);
                }
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
        
        EspDeviceLoopWeekTimer timer = (EspDeviceLoopWeekTimer)mTimer;
        String action = timer.getTimeAction().get(0).getAction();
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
