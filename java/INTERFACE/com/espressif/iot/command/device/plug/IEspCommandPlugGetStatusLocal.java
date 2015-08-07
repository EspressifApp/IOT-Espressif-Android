package com.espressif.iot.command.device.plug;

import java.net.InetAddress;

import com.espressif.iot.command.IEspCommandLocal;
import com.espressif.iot.command.device.IEspCommandPlug;
import com.espressif.iot.type.device.status.IEspStatusPlug;

public interface IEspCommandPlugGetStatusLocal extends IEspCommandLocal, IEspCommandPlug
{
    /**
     * @deprecated Use {@link #doCommandPlugGetStatusLocal(InetAddress, String, String)} instead of it,
     * and the deviceBssid=null when you call the method
     * 
     * get the statusPlug to the Plug by Local
     * @param inetAddress the Plug's ip address
     * @return the status of the Plug or null(if executed fail)
     */
    IEspStatusPlug doCommandPlugGetStatusLocal(InetAddress inetAddress);
    
    /**
     * get the statusPlug to the Plug by Local
     * 
     * @param inetAddress the Plug's ip address
     * @param deviceBssid the Plug's bssid
     * @param isMeshDevice whether the Plug is mesh device
     * @return the status of the Plug or null(if executed fail)
     */
    IEspStatusPlug doCommandPlugGetStatusLocal(InetAddress inetAddress, String deviceBssid, boolean isMeshDevice);

}
