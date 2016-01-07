package com.espressif.iot.action.device.common.trigger;

import java.util.List;

import com.espressif.iot.command.device.common.EspCommandDeviceTriggerInternet;
import com.espressif.iot.command.device.common.IEspCommandDeviceTriggerInternet;
import com.espressif.iot.device.IEspDevice;
import com.espressif.iot.type.device.trigger.EspDeviceTrigger;

public class EspActionDeviceTriggerGet implements IEspActionDeviceTriggerGet
{

    @Override
    public List<EspDeviceTrigger> getTriggersInternet(IEspDevice device)
    {
        IEspCommandDeviceTriggerInternet cmd = new EspCommandDeviceTriggerInternet();
        return cmd.getTriggersInternet(device);
    }

}
