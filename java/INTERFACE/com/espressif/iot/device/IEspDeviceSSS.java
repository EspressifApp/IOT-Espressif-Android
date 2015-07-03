package com.espressif.iot.device;

import com.espressif.iot.type.device.IEspDeviceStatus;
import com.espressif.iot.type.net.IOTAddress;

public interface IEspDeviceSSS extends IEspDeviceRoot
{
    /**
     * set the iotAddress of the device
     * @param iotAddress the deivce's new iotAddress
     */
    void setIOTAddress(IOTAddress iotAddress);
    
    /**
     * get the device's iotAddress
     * @return iotAddress
     */
    IOTAddress getIOTAddress();
    
    /**
     * get the status of the device
     * @return the status of the device
     */
    IEspDeviceStatus getDeviceStatus();
    
    /**
     * create an device remained to be activated
     * @param random40 the random40 key
     * @return the IEspDeviceConfigure with state Configuring
     */
    IEspDeviceConfigure createConfiguringDevice(String random40);
}
