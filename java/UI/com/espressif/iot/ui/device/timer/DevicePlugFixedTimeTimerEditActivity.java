package com.espressif.iot.ui.device.timer;

import com.espressif.iot.type.device.timer.EspDeviceFixedTimeTimer;

public class DevicePlugFixedTimeTimerEditActivity extends DeviceTimerEditFixedTimerActivityAbs
{
    @Override
    protected String getEditAction()
    {
        return mActionValues[getActionSpinner().getSelectedItemPosition()];
    }

    @Override
    protected String getTimerAction(EspDeviceFixedTimeTimer timer)
    {
        return timer.getTimeAction().get(0).getAction();
    }
}
