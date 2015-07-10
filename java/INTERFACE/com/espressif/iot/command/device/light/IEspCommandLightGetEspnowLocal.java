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
     * @param inetAddress
     * @param deviceBssid
     * @param router
     * @return
     */
    List<IEspStatusEspnow> doCommandLightGetEspnowLocal(InetAddress inetAddress, String deviceBssid, String router);
}
