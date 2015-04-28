package com.espressif.iot.device.builder;

import com.espressif.iot.device.IEspDeviceLight;
import com.espressif.iot.model.device.EspDeviceLight;

public class BEspDeviceLight implements IBEspDeviceLight
{
    private BEspDeviceLight()
    {
    }
    
    private static class InstanceHolder
    {
        static BEspDeviceLight instance = new BEspDeviceLight();
    }
    
    public static BEspDeviceLight getInstance()
    {
        return InstanceHolder.instance;
    }
    
    @Override
    public IEspDeviceLight alloc()
    {
        return new EspDeviceLight();
    }
    
}
