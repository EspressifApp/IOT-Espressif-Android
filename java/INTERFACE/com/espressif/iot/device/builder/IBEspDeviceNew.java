package com.espressif.iot.device.builder;

import com.espressif.iot.device.IEspDeviceNew;
import com.espressif.iot.object.IEspObjectBuilder;
import com.espressif.iot.type.net.WifiCipherType;

public interface IBEspDeviceNew extends IEspObjectBuilder
{
    IEspDeviceNew alloc(String ssid, String bssid, WifiCipherType wifiCipherType,int rssi);
    
    IEspDeviceNew alloc(String ssid, String bssid, WifiCipherType wifiCipherType,int rssi,int state);
}
