package com.espressif.iot.action.device.common.trigger;

import com.espressif.iot.action.IEspActionInternet;
import com.espressif.iot.device.IEspDevice;

public interface IEspActionDeviceTriggerDelete extends IEspActionInternet {

    /**
     * Delete trigger on server
     * 
     * @param device
     * @param triggerId
     * @return
     */
    public boolean deleteTriggerInternet(IEspDevice device, long triggerId);
}
