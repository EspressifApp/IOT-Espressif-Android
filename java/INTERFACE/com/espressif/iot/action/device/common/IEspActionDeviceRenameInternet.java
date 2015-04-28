package com.espressif.iot.action.device.common;

import com.espressif.iot.action.IEspActionDevice;
import com.espressif.iot.action.IEspActionInternet;

public interface IEspActionDeviceRenameInternet extends IEspActionInternet, IEspActionDevice
{
    /**
     * rename the device on Server
     * 
     * @param deviceKey the device's key
     * @param deviceName the device's new name
     * @return whether the command executed suc
     */
    boolean doActionDeviceRenameInternet(String deviceKey, String deviceName);
}
