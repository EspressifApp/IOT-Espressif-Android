package com.espressif.iot.device.builder;

import com.espressif.iot.device.IEspDeviceHumiture;
import com.espressif.iot.model.device.EspDeviceHumiture;

public class BEspDeviceHumiture implements IBEspDeviceHumiture
{
    private BEspDeviceHumiture()
    {
    }
    
    private static class InstanceHolder
    {
        static BEspDeviceHumiture instance = new BEspDeviceHumiture();
    }
    
    public static BEspDeviceHumiture getInstance()
    {
        return InstanceHolder.instance;
    }
    
    @Override
    public IEspDeviceHumiture alloc()
    {
        return new EspDeviceHumiture();
    }
    
}
