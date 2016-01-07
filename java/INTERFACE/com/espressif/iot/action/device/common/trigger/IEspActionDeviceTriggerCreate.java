package com.espressif.iot.action.device.common.trigger;

import com.espressif.iot.action.IEspActionInternet;
import com.espressif.iot.device.IEspDevice;
import com.espressif.iot.type.device.trigger.EspDeviceTrigger;

public interface IEspActionDeviceTriggerCreate extends IEspActionInternet
{
    /**
     * Create a new trigger on server
     * 
     * @param device
     * @param trigger
     * @return the id of created trigger, -1 is failed
     */
    public long createTriggerInternet(IEspDevice device, EspDeviceTrigger trigger);
}
