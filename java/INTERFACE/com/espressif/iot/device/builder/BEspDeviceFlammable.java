package com.espressif.iot.device.builder;

import com.espressif.iot.device.IEspDeviceFlammable;
import com.espressif.iot.model.device.EspDeviceFlammable;

public class BEspDeviceFlammable implements IBEspDeviceFlammable
{
    private BEspDeviceFlammable()
    {
    }
    
    private static class InstanceHolder
    {
        static BEspDeviceFlammable instance = new BEspDeviceFlammable();
    }
    
    public static BEspDeviceFlammable getInstance()
    {
        return InstanceHolder.instance;
    }
    
    @Override
    public IEspDeviceFlammable alloc()
    {
        return new EspDeviceFlammable();
    }
    
}
