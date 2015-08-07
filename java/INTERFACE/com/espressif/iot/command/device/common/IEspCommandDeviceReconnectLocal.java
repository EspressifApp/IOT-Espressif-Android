package com.espressif.iot.command.device.common;

import com.espressif.iot.command.IEspCommandLocal;
import com.espressif.iot.command.device.IEspCommandActivated;
import com.espressif.iot.type.net.WifiCipherType;

public interface IEspCommandDeviceReconnectLocal extends IEspCommandActivated, IEspCommandLocal
{
    /**
     * Make device reconnect to another AP, only mesh device support the function.
     * Except mesh device, the device will shut down softap,
     * the mesh device won't shut down.
     * Except mesh device, the device require connecting AP accessible Internet,
     * the mesh device could connect to mesh device and could change its connection target after configure
     * 
     * @param deviceBssid the device's bssid
     * @param apSsid the AP's ssid
     * @param type the wifi cipher type
     * @param apPassword the password of the AP, if WifiCipherType isn't OPEN
     * @return whether the command executed suc
     */
    boolean doCommandReconnectLocal(final String deviceBssid, final String apSsid, final WifiCipherType type,
        final String... apPassword);
}
