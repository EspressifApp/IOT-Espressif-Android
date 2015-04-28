package com.espressif.iot.action.device.common;

import com.espressif.iot.action.IEspActionDevice;
import com.espressif.iot.action.IEspActionInternet;

public interface IEspActionDeviceGenerateShareKeyInternet extends IEspActionInternet, IEspActionDevice
{
    /**
     * Get share key from Server
     * 
     * @param ownerDeviceKey
     * @return the share key of the device
     */
    String doActionDeviceGenerateShareKeyInternet(String ownerDeviceKey);
}
