package com.espressif.iot.command.device.common;

import com.espressif.iot.command.IEspCommandInternet;
import com.espressif.iot.command.device.IEspCommandActivated;

public interface IEspCommandDeviceGenerateShareKeyInternet extends IEspCommandActivated, IEspCommandInternet
{
    static final String URL = "https://iot.espressif.cn/v1/key/share/";
    
    /**
     * Get the share key from server
     * @param ownerDeviceKey
     * @return the share key of the device
     */
    String doCommandDeviceGenerateShareKey(String ownerDeviceKey);
}
