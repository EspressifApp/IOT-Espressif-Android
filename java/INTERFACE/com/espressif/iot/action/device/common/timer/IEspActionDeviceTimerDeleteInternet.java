package com.espressif.iot.action.device.common.timer;

import com.espressif.iot.action.IEspActionDevice;
import com.espressif.iot.action.IEspActionInternet;
import com.espressif.iot.device.IEspDevice;

public interface IEspActionDeviceTimerDeleteInternet extends IEspActionDevice, IEspActionInternet
{
    /**
     * Delete the timer
     * 
     * @param device
     * @param timerId
     * @return delete the timer suc or failed
     */
    boolean doActionDeviceTimerDeleteInternet(IEspDevice device, long timerId);
}
