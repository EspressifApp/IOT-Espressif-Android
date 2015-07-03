package com.espressif.iot.command.device.New;

import com.espressif.iot.command.IEspCommandInternet;
import com.espressif.iot.command.device.IEspCommandActivated;
import com.espressif.iot.command.device.IEspCommandNew;
import com.espressif.iot.device.IEspDevice;

public interface IEspCommandDeviceNewActivateInternet extends IEspCommandNew, IEspCommandActivated, IEspCommandInternet
{
    static final String URL = "https://iot.espressif.cn/v1/key/authorize/?query_devices_mesh=true";
    
    /**
     * Activate the new device online
     * 
     * @param userId
     * @param userKey
     * @param randomToken
     * @return the new activated device
     */
    IEspDevice doCommandNewActivateInternet(long userId, String userKey, String randomToken);
}
