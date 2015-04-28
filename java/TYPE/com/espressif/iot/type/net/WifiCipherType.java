package com.espressif.iot.type.net;

import android.net.wifi.ScanResult;

public enum WifiCipherType
{
    /**
     * WEP
     */
    WIFICIPHER_WEP,
    /**
     * WPA or WPA2
     */
    WIFICIPHER_WPA,
    /**
     * OPEN means the wifi don't have password
     */
    WIFICIPHER_OPEN,
    /**
     * Invalid wifi cipher type, it shouldn't happen
     */
    WIFICIPHER_INVALID;
    
    public static WifiCipherType getWifiCipherType(ScanResult scanResult)
    {
        String capa = scanResult.capabilities;
        if (capa.contains("PSK"))
        {
            return WIFICIPHER_WPA;
        }
        else if (capa.contains("WEP"))
        {
            return WIFICIPHER_WEP;
        }
        else
        {
            return WIFICIPHER_OPEN;
        }
    }
}
