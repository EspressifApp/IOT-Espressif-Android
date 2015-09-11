package com.espressif.iot.device.array;

import java.util.List;

import com.espressif.iot.device.IEspDevice;

public interface IEspDeviceArray extends IEspDevice
{
    void addDevice(IEspDevice device);
    
    void removeDevice(IEspDevice device);
    
    List<IEspDevice> getDeviceList();
}
