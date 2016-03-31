package com.espressif.iot.ui.device.timer;

import com.espressif.iot.device.IEspDevicePlugs;
import com.espressif.iot.type.device.timer.EspDeviceFixedTimeTimer;
import com.espressif.iot.util.EspStrings;

import android.os.Bundle;

public class DevicePlugsFixedTimeTimerEditActivity extends DeviceTimerEditFixedTimerActivityAbs
{
    private String mValue;
    
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        mValue = mIntentBundle.getString(EspStrings.Key.DEVICE_TIMER_PLUGS_VALUE_KEY);
    }
    
    @Override
    protected String getEditAction()
    {
        return mActionValues[getActionSpinner().getSelectedItemPosition()] + mValue;
    }

    @Override
    protected String getTimerAction(EspDeviceFixedTimeTimer timer)
    {
        String action = timer.getTimeAction().get(0).getAction();
        action = action.substring(0, action.length() - IEspDevicePlugs.TIMER_TAIL_LENGTH);
        return action;
    }
}
