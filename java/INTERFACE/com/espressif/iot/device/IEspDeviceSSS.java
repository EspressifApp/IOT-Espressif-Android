package com.espressif.iot.device;

import com.espressif.iot.type.device.IEspDeviceStatus;
import com.espressif.iot.type.net.IOTAddress;

public interface IEspDeviceSSS extends IEspDeviceRoot
{
    void setIOTAddress(IOTAddress iotAddress);
    
    IOTAddress getIOTAddress();
    
    IEspDeviceStatus getDeviceStatus();
}
