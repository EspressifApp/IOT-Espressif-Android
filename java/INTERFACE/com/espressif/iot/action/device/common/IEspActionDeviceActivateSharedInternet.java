package com.espressif.iot.action.device.common;

import com.espressif.iot.action.IEspActionDevice;
import com.espressif.iot.action.IEspActionInternet;

public interface IEspActionDeviceActivateSharedInternet extends IEspActionInternet, IEspActionDevice
{
    /**
     * Get the shared device
     * 
     * @param userId the user id
     * @param userKey the user need get the device
     * @param sharedDeviceKey the shared device key
     * @return whether activate shared device success
     */
    boolean doActionDeviceActivateSharedInternet(long userId, String userKey, String sharedDeviceKey);
}
