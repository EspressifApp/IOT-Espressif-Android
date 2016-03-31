package com.espressif.iot.ui.device.timer;

import com.espressif.iot.type.device.timer.EspDeviceLoopPeriodTimer;

public class DevicePlugLoopPeriodTimerEditActivity extends DeviceTimerEditLoopPeriodActivityAbs
{
    @Override
    protected String getEditAction()
    {
        return mActionValues[getActionSpinner().getSelectedItemPosition()];
    }
    
    @Override
    protected String getTimerAction(EspDeviceLoopPeriodTimer timer)
    {
        return timer.getAction();
    }
}
