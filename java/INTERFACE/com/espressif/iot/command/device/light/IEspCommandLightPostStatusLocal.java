package com.espressif.iot.command.device.light;

import java.net.InetAddress;

import com.espressif.iot.command.IEspCommandLocal;
import com.espressif.iot.command.device.IEspCommandLight;
import com.espressif.iot.type.device.status.IEspStatusLight;

public interface IEspCommandLightPostStatusLocal extends IEspCommandLocal, IEspCommandLight
{
    /**
     * @deprecated Use {@link #doCommandLightPostStatusLocal(InetAddress, IEspStatusLight, String, String)} instead of it,
     * and the deviceBssid=null and router=null when you call the method
     * 
     * post the statusPlug to the Light by Local
     * 
     * @param inetAddress the Light's ip address
     * @param statusLight the status of Light
     * @return whether the command executed suc
     */
    boolean doCommandLightPostStatusLocal(InetAddress inetAddress, IEspStatusLight statusLight);
    
    /**
     * post the statusPlug to the Light by Local
     * 
     * @param inetAddress the Light's ip address
     * @param statusLight the status of Light
     * @param deviceBssid the Light's bssid
     * @param router the Light's router
     * @return whether the command executed suc
     */
    boolean doCommandLightPostStatusLocal(InetAddress inetAddress, IEspStatusLight statusLight, String deviceBssid,
        String router);
    
    /**
     * post the statusPlug to the Light by Local
     * 
     * @param inetAddress the Light's ip address
     * @param statusLight the status of Light
     * @param deviceBssid the Light's bssid
     * @param isMeshDevice whether the Light is mesh device
     * @return whether the command executed suc
     */
    boolean doCommandLightPostStatusLocal(InetAddress inetAddress, IEspStatusLight statusLight, String deviceBssid,
        boolean isMeshDevice);
}
