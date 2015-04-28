package com.espressif.iot.command.device.light;

import java.net.InetAddress;

import com.espressif.iot.command.IEspCommandLocal;
import com.espressif.iot.command.device.IEspCommandLight;
import com.espressif.iot.type.device.status.IEspStatusLight;

public interface IEspCommandLightGetStatusLocal extends IEspCommandLocal, IEspCommandLight
{
    /**
     * @deprecated Use {@link #doCommandLightGetStatusLocal(InetAddress, String, String)} instead of it,
     * and the deviceBssid=null and router=null when you call the method
     * 
     * get the statusLight to the Light by Local
     * 
     * @param inetAddress the Light's ip address
     * @return the status of the Light or null(if executed fail)
     */
    IEspStatusLight doCommandLightGetStatusLocal(InetAddress inetAddress);
    
    /**
     * get the statusLight to the Light by Local
     * 
     * @param inetAddress the Light's ip address
     * @param deviceBssid the Light's bssid
     * @param router the Light's router
     * @return
     */
    IEspStatusLight doCommandLightGetStatusLocal(InetAddress inetAddress, String deviceBssid, String router);
}
