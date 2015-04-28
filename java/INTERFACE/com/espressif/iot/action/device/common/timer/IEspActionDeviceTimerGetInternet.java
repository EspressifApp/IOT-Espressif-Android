package com.espressif.iot.action.device.common.timer;

import com.espressif.iot.action.IEspActionDevice;
import com.espressif.iot.action.IEspActionInternet;
import com.espressif.iot.device.IEspDevice;

public interface IEspActionDeviceTimerGetInternet extends IEspActionDevice, IEspActionInternet
{
    /**
     * Get timers from server
     * 
     * @param device
     * @return get timers suc or failed
     */
    boolean doActionDeviceTimerGet(IEspDevice device);
}
