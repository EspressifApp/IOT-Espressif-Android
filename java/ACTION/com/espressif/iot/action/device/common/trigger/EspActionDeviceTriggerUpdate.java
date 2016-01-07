package com.espressif.iot.action.device.common.trigger;

import com.espressif.iot.command.device.common.EspCommandDeviceTriggerInternet;
import com.espressif.iot.command.device.common.IEspCommandDeviceTriggerInternet;
import com.espressif.iot.device.IEspDevice;
import com.espressif.iot.type.device.trigger.EspDeviceTrigger;

public class EspActionDeviceTriggerUpdate implements IEspActionDeviceTriggerUpdate
{

    @Override
    public boolean updateTriggerInternet(IEspDevice device, EspDeviceTrigger trigger)
    {
        IEspCommandDeviceTriggerInternet cmd = new EspCommandDeviceTriggerInternet();
        return cmd.updateTriggerInternet(device, trigger);
    }

}
