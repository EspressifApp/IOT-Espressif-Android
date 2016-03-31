package com.espressif.iot.ui.device.timer;

import com.espressif.iot.device.IEspDevicePlugs;
import com.espressif.iot.type.device.timer.EspDeviceLoopWeekTimer;
import com.espressif.iot.util.EspStrings;

import android.os.Bundle;

public class DevicePlugsLoopInWeekTimerEditActivity extends DeviceTimerEditLoopInWeekActivityAbs
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
    protected String getTimerAction(EspDeviceLoopWeekTimer timer)
    {
        String action = timer.getTimeAction().get(0).getAction();
        action = action.substring(0, action.length() - IEspDevicePlugs.TIMER_TAIL_LENGTH);
        return action;
    }
}
