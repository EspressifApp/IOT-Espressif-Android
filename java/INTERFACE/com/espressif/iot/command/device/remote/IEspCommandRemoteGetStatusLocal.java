package com.espressif.iot.command.device.remote;

import java.net.InetAddress;

import com.espressif.iot.command.IEspCommandLocal;
import com.espressif.iot.command.device.IEspCommandRemote;
import com.espressif.iot.type.device.status.IEspStatusRemote;

public interface IEspCommandRemoteGetStatusLocal extends IEspCommandLocal, IEspCommandRemote
{
    /**
     * @deprecated Use {@link #doCommandRemoteGetStatusLocal(InetAddress, String, String)} instead of it,
     * and the deviceBssid=null when you call the method
     * 
     * get the statusRemote to the Remote by Local
     * 
     * @param inetAddress the Remote's ip address
     * @return the status of the Remote or null(if executed fail)
     */
    IEspStatusRemote doCommandRemoteGetStatusLocal(InetAddress inetAddress);

    /**
     * get the statusRemote to the Remote by Local
     * 
     * @param inetAddress the Remote's ip address
     * @param deviceBssid the Remote's bssid
     * @param isMeshDevice whether the Remote is mesh device
     * @return the status of the Remote or null(if executed fail)
     */
    IEspStatusRemote doCommandRemoteGetStatusLocal(InetAddress inetAddress, String deviceBssid, boolean isMeshDevice);

}
