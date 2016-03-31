package com.espressif.iot.ui.device.timer;

import com.espressif.iot.device.IEspDevicePlugs;
import com.espressif.iot.type.device.timer.EspDeviceLoopPeriodTimer;
import com.espressif.iot.util.EspStrings;

import android.os.Bundle;

public class DevicePlugsLoopPeriodTimerEditActivity extends DeviceTimerEditLoopPeriodActivityAbs
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
    protected String getTimerAction(EspDeviceLoopPeriodTimer timer)
    {
        String action = timer.getAction();
        action = action.substring(0, action.length() - IEspDevicePlugs.TIMER_TAIL_LENGTH);
        return action;
    }
}
