package com.espressif.iot.action.device.common;

import java.util.List;

import com.espressif.iot.action.IEspActionLocal;
import com.espressif.iot.action.device.IEspActionActivated;
import com.espressif.iot.action.device.IEspActionUnactivated;
import com.espressif.iot.device.IEspDevice;
import com.espressif.iot.type.net.WifiCipherType;

public interface IEspActionDeviceReconnectLocal extends IEspActionActivated, IEspActionUnactivated, IEspActionLocal
{
    /**
     * Make device reconnect to another AP, only mesh device support the function.
     * Except mesh device, the device will shut down softap,
     * the mesh device won't shut down.
     * Except mesh device, the device require connecting AP accessible Internet,
     * the mesh device could connect to mesh device and could change its connection target after configure.
     * After the action the currentDeviceList will update their routers and their device states.
     * But the current device won't change it device state directly to avoid transform device state invalid.
     * 
     * @param router the router
     * @param deviceBssid the device's bssid
     * @param apSsid the AP's ssid
     * @param type the wifi cipher type
     * @param apPassword the password of the AP, if WifiCipherType isn't OPEN
     * @return whether the action executed suc
     */
    boolean doActionDeviceReconnectLocal(final List<IEspDevice> currentDeviceList, final String router,
        final String deviceBssid, final String apSsid, final WifiCipherType type, final String... apPassword);
}
