package com.espressif.iot.device;

import java.util.List;

import com.espressif.iot.adt.tree.IEspDeviceTreeElement;
import com.espressif.iot.type.device.IEspDeviceStatus;
import com.espressif.iot.type.net.IOTAddress;

public interface IEspDeviceSSS extends IEspDevice
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
    
    /**
     * Get all tree element of the Sta device
     * @return
     */
    List<IEspDeviceTreeElement> getDeviceTreeElementList();
}
