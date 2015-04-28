package com.espressif.iot.device.builder;

import com.espressif.iot.device.IEspDeviceRemote;
import com.espressif.iot.model.device.EspDeviceRemote;

public class BEspDeviceRemote implements IBEspDeviceRemote
{
    /*
     * Singleton lazy initialization start
     */
    private BEspDeviceRemote()
    {
    }
    
    private static class InstanceHolder
    {
        static BEspDeviceRemote instance = new BEspDeviceRemote();
    }
    
    public static BEspDeviceRemote getInstance()
    {
        return InstanceHolder.instance;
    }
    
    /*
     * Singleton lazy initialization end
     */
    
    @Override
    public IEspDeviceRemote alloc()
    {
        return new EspDeviceRemote();
    }
    
}
