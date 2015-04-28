package com.espressif.iot.command.device.remote;

import java.net.InetAddress;

import com.espressif.iot.command.IEspCommandLocal;
import com.espressif.iot.command.device.IEspCommandRemote;
import com.espressif.iot.type.device.status.IEspStatusRemote;

public interface IEspCommandRemotePostStatusLocal extends IEspCommandLocal, IEspCommandRemote
{
    /**
     * @deprecated Use {@link } instead of it,
     * and the deviceBssid=null and router=null when you call the method
     * 
     * post the statusPlug to the Remote by Local
     * 
     * @param inetAddress the Remote's ip address
     * @param statusRemote the status of Remote
     * @return whether the command executed suc
     */
    boolean doCommandRemotePostStatusLocal(InetAddress inetAddress, IEspStatusRemote statusRemote);
    
    /**
     * post the statusPlug to the Remote by Local
     * 
     * @param inetAddress the Remote's ip address
     * @param statusRemote the status of Remote
     * @param deviceBssid the Remote's bssid
     * @param router the Remote's router
     * @return whether the command executed suc
     */
    boolean doCommandRemotePostStatusLocal(InetAddress inetAddress, IEspStatusRemote statusRemote, String deviceBssid,
        String router);
}
