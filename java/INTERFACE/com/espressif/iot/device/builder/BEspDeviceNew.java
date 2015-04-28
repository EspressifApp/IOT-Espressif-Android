package com.espressif.iot.device.builder;

import com.espressif.iot.device.IEspDeviceNew;
import com.espressif.iot.model.device.EspDeviceNew;
import com.espressif.iot.type.net.WifiCipherType;

public class BEspDeviceNew implements IBEspDeviceNew
{
    /*
     * Singleton lazy initialization start
     */
    private BEspDeviceNew()
    {
    }
    
    private static class InstanceHolder
    {
        static BEspDeviceNew instance = new BEspDeviceNew();
    }
    
    public static BEspDeviceNew getInstance()
    {
        return InstanceHolder.instance;
    }
    
    /*
     * Singleton lazy initialization end
     */
    
    @Override
    public IEspDeviceNew alloc(String ssid, String bssid, WifiCipherType wifiCipherType, int rssi)
    {
        IEspDeviceNew device = new EspDeviceNew(ssid, bssid, wifiCipherType, rssi);
        return device;
    }
    
    @Override
    public IEspDeviceNew alloc(String ssid, String bssid, WifiCipherType wifiCipherType, int rssi, int state)
    {
        IEspDeviceNew device = new EspDeviceNew(ssid, bssid, wifiCipherType, rssi, state);
        return device;
    }
    
}
