package com.espressif.iot.ui.device.timer;

import com.espressif.iot.type.device.timer.EspDeviceLoopWeekTimer;

public class DevicePlugLoopInWeekTimerEditActivity extends DeviceTimerEditLoopInWeekActivityAbs
{
    
    @Override
    protected String getEditAction()
    {
        return mActionValues[mActionSpinner.getSelectedItemPosition()];
    }
    
    @Override
    protected String getTimerAction(EspDeviceLoopWeekTimer timer)
    {
        return timer.getTimeAction().get(0).getAction();
    }
    
}
