package com.espressif.iot.ui.configure;

import com.espressif.iot.type.net.WifiCipherType;

/**
 * The information of target configure Wifi AP
 * @author xuxiangjun
 *
 */
public class ApInfo
{
    final String bssid;
    
    final String ssid;
    
    final String password;
    
    final WifiCipherType type;
    
    public ApInfo(String apBssid, String apSsid, String apPassword, WifiCipherType wifiType)
    {
        bssid = apBssid;
        ssid = apSsid;
        password = apPassword;
        type = wifiType;
    }
    
    public String getBssid()
    {
        return bssid;
    }
    
    public String getSsid()
    {
        return ssid;
    }
}
