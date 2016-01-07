package com.espressif.iot.action.device.common.trigger;

import java.util.List;

import com.espressif.iot.action.IEspActionInternet;
import com.espressif.iot.device.IEspDevice;
import com.espressif.iot.type.device.trigger.EspDeviceTrigger;

public interface IEspActionDeviceTriggerGet extends IEspActionInternet
{
    /**
     * Get triggers list of the device from server
     * 
     * @param device
     * @return null is failed
     */
    public List<EspDeviceTrigger> getTriggersInternet(IEspDevice device);
}
