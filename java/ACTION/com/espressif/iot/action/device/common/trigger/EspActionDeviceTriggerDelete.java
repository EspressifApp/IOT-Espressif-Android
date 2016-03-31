package com.espressif.iot.action.device.common.trigger;

import com.espressif.iot.command.device.common.EspCommandDeviceTriggerInternet;
import com.espressif.iot.command.device.common.IEspCommandDeviceTriggerInternet;
import com.espressif.iot.device.IEspDevice;

public class EspActionDeviceTriggerDelete implements IEspActionDeviceTriggerDelete {

    @Override
    public boolean deleteTriggerInternet(IEspDevice device, long triggerId) {
        IEspCommandDeviceTriggerInternet cmd = new EspCommandDeviceTriggerInternet();
        return cmd.deleteTriggerInternet(device, triggerId);
    }

}
