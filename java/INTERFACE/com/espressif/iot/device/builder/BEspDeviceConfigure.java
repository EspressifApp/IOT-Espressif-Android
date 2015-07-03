package com.espressif.iot.device.builder;

import com.espressif.iot.device.IEspDeviceConfigure;
import com.espressif.iot.model.device.EspDeviceConfigure;

public class BEspDeviceConfigure implements IBEspDeviceConfigure
{
    /*
     * Singleton lazy initialization start
     */
    private BEspDeviceConfigure()
    {
    }
    
    private static class InstanceHolder
    {
        static BEspDeviceConfigure instance = new BEspDeviceConfigure();
    }
    
    public static BEspDeviceConfigure getInstance()
    {
        return InstanceHolder.instance;
    }
    
    @Override
    public IEspDeviceConfigure alloc(String bssid, String randomToken)
    {
        IEspDeviceConfigure device = new EspDeviceConfigure(bssid, randomToken);
        return device;
    }
    
    /*
     * Singleton lazy initialization end
     */
    
}
