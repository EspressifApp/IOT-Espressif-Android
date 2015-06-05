package com.espressif.iot.command.device.New;

import com.espressif.iot.command.IEspCommandLocal;
import com.espressif.iot.command.device.IEspCommandNew;
import com.espressif.iot.device.IEspDeviceNew;
import com.espressif.iot.type.device.DeviceInfo;

public interface IEspCommandDeviceNewGetInfoLocal extends IEspCommandNew, IEspCommandLocal
{
    /**
     * Get the DeviceInfo of the local device
     * 
     * @param device
     * @return the DeviceInfo of the device
     */
    DeviceInfo doCommandDeviceNewGetInfoLocal(IEspDeviceNew device);
}
