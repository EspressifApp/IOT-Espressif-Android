package com.espressif.iot.action.device.common.trigger;

import com.espressif.iot.action.IEspActionInternet;
import com.espressif.iot.device.IEspDevice;
import com.espressif.iot.type.device.trigger.EspDeviceTrigger;

public interface IEspActionDeviceTriggerUpdate extends IEspActionInternet
{
    /**
     * Update trigger on server
     * 
     * @param device
     * @param trigger must contain it's id
     * @return true is successful, false is failed
     */
    public boolean updateTriggerInternet(IEspDevice device, EspDeviceTrigger trigger);
}
