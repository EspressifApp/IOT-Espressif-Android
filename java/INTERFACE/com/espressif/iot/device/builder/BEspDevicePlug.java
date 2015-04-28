package com.espressif.iot.device.builder;

import com.espressif.iot.device.IEspDevicePlug;
import com.espressif.iot.model.device.EspDevicePlug;

public class BEspDevicePlug implements IBEspDevicePlug
{
    /*
     * Singleton lazy initialization start
     */
    private BEspDevicePlug()
    {
    }
    
    private static class InstanceHolder
    {
        static BEspDevicePlug instance = new BEspDevicePlug();
    }
    
    public static BEspDevicePlug getInstance()
    {
        return InstanceHolder.instance;
    }
    
    /*
     * Singleton lazy initialization end
     */
    
    @Override
    public IEspDevicePlug alloc()
    {
        IEspDevicePlug device = new EspDevicePlug();
        return device;
    }
}
