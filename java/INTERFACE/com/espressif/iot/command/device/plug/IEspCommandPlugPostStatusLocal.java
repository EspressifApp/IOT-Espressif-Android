package com.espressif.iot.command.device.plug;

import java.net.InetAddress;

import com.espressif.iot.command.IEspCommandLocal;
import com.espressif.iot.command.device.IEspCommandPlug;
import com.espressif.iot.type.device.status.IEspStatusPlug;

public interface IEspCommandPlugPostStatusLocal extends IEspCommandLocal, IEspCommandPlug
{
    /**
     * @deprecated Use {@link #doCommandPlugPostStatusLocal(InetAddress, IEspStatusPlug, String, String)} instead of it,
     * and the deviceBssid=null and router=null when you call the method
     * 
     * post the statusPlug to the Plug by Local
     * @param inetAddress the Plug's ip address
     * @param statusPlug the status of Plug
     * @return whether the command executed suc
     */
    boolean doCommandPlugPostStatusLocal(InetAddress inetAddress,IEspStatusPlug statusPlug);
    
    /**
     * post the statusPlug to the Plug by Local
     * 
     * @param inetAddress the Plug's ip address
     * @param statusPlug the status of Plug
     * @param deviceBssid the Plug's bssid
     * @param router the Plug's router
     * @return whether the command executed suc
     */
    boolean doCommandPlugPostStatusLocal(InetAddress inetAddress, IEspStatusPlug statusPlug, String deviceBssid,
        String router);
    
    /**
     * post the statusPlug to the Plug by Local
     * 
     * @param inetAddress the Plug's ip address
     * @param statusPlug the status of Plug
     * @param deviceBssid the Plug's bssid
     * @param isMeshDevice whether the Plug is mesh device
     * @return whether the command executed suc
     */
    boolean doCommandPlugPostStatusLocal(InetAddress inetAddress, IEspStatusPlug statusPlug, String deviceBssid,
        boolean isMeshDevice);
}
