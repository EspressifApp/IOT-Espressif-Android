package com.espressif.iot.command.device.common;

import com.espressif.iot.command.IEspCommandInternet;
import com.espressif.iot.command.device.IEspCommandActivated;

public interface IEspCommandDeviceRenameInternet extends IEspCommandActivated, IEspCommandInternet
{
    static final String URL = "https://iot.espressif.cn/v1/device/" + "?method=PUT";
    /**
     * rename the device on Server
     * @param deviceKey the device's key
     * @param deviceName the device's new name
     * @return whether the command executed suc
     */
    boolean doCommandDeviceRenameInternet(String deviceKey,String deviceName);
}
