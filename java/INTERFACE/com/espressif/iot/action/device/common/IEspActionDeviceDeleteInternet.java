package com.espressif.iot.action.device.common;

import com.espressif.iot.action.IEspActionDevice;
import com.espressif.iot.action.IEspActionInternet;

public interface IEspActionDeviceDeleteInternet extends IEspActionInternet, IEspActionDevice
{
    /**
     * delete the device on Server
     * 
     * @param deviceKey the device's key
     * @return whether the command executed suc
     */
    boolean doActionDeviceDeleteInternet(String deviceKey);
}
