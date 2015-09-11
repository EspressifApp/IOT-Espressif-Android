package com.espressif.iot.action.device.New;

import com.espressif.iot.action.IEspActionLocal;
import com.espressif.iot.action.device.IEspActionNew;
import com.espressif.iot.device.IEspDeviceNew;
import com.espressif.iot.type.net.IOTAddress;

public interface IEspActionDeviceNewGetInfoLocal extends IEspActionNew, IEspActionLocal
{
    /**
     * Get the sta-device information
     * @param device
     * @return the IOTAddress of the device
     */
    IOTAddress doActionNewGetInfoLocal(IEspDeviceNew device);
}
