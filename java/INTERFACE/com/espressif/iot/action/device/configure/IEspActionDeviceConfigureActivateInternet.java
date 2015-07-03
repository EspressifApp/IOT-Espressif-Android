package com.espressif.iot.action.device.configure;

import com.espressif.iot.action.IEspActionInternet;
import com.espressif.iot.action.device.IEspActionConfigure;
import com.espressif.iot.device.IEspDevice;

public interface IEspActionDeviceConfigureActivateInternet extends IEspActionConfigure, IEspActionInternet
{
    /**
     * activate device in the Internet
     * 
     * @param userId the user's id
     * @param userKey the user's key
     * @param randomToken the random token
     * @return IEspDevice the device
     */
    IEspDevice doActionDeviceConfigureActivateInternet(long userId, String userKey, String randomToken);
    
}
