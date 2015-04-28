package com.espressif.iot.device.builder;

import com.espressif.iot.device.IEspDeviceVoltage;
import com.espressif.iot.model.device.EspDeviceVoltage;

public class BEspDeviceVoltage implements IBEspDeviceVoltage
{
    private BEspDeviceVoltage()
    {
    }
    
    private static class InstanceHolder
    {
        static BEspDeviceVoltage instance = new BEspDeviceVoltage();
    }
    
    public static BEspDeviceVoltage getInstance()
    {
        return InstanceHolder.instance;
    }
    
    @Override
    public IEspDeviceVoltage alloc()
    {
        return new EspDeviceVoltage();
    }
    
}
