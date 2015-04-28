package com.espressif.iot.action.device.common;

import com.espressif.iot.action.IEspActionInternet;
import com.espressif.iot.action.device.IEspActionActivated;
import com.espressif.iot.device.IEspDevice;

public interface IEspActionDeviceGetStatusInternet extends IEspActionActivated, IEspActionInternet
{
    /**
     * get the current status of device via internet)
     * 
     * @param device the device
     * @return whether the get action is suc
     */
    boolean doActionDeviceGetStatusInternet(final IEspDevice device);
}
