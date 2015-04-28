package com.espressif.iot.device.builder;

import com.espressif.iot.device.IEspDevicePlugs;
import com.espressif.iot.model.device.EspDevicePlugs;

public class BEspDevicePlugs implements IBEspDevicePlugs
{
    private BEspDevicePlugs()
    {
    }
    
    private static class InstanceHolder
    {
        static BEspDevicePlugs instance = new BEspDevicePlugs();
    }
    
    public static BEspDevicePlugs getInstance()
    {
        return InstanceHolder.instance;
    }
    
    @Override
    public IEspDevicePlugs alloc()
    {
        return new EspDevicePlugs();
    }
    
}
