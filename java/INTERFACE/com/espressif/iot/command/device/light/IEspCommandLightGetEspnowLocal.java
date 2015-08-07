package com.espressif.iot.command.device.light;

import java.net.InetAddress;
import java.util.List;

import com.espressif.iot.command.IEspCommandLocal;
import com.espressif.iot.command.device.IEspCommandLight;
import com.espressif.iot.type.device.status.IEspStatusEspnow;

public interface IEspCommandLightGetEspnowLocal extends IEspCommandLight, IEspCommandLocal
{
    /**
     * Get battery status of the Espnow which control the light
     * 
     * @param inetAddress the Light's ip address
     * @param deviceBssid the Light's bssid
     * @param isMeshDevice whether the Light is mesh device
     * @return battery status of the Espnow which control the light
     */
    List<IEspStatusEspnow> doCommandLightGetEspnowLocal(InetAddress inetAddress, String deviceBssid,
        boolean isMeshDevice);
}
