package com.espressif.iot.command.device.remote;

import java.net.InetAddress;

import com.espressif.iot.command.IEspCommandInternet;
import com.espressif.iot.command.device.IEspCommandRemote;
import com.espressif.iot.type.device.status.IEspStatusRemote;

public interface IEspCommandRemotePostStatusInternet extends IEspCommandInternet, IEspCommandRemote
{
    /**
     * @deprecated Use {@link #doCommandRemotePostStatusInternet(InetAddress, IEspStatusRemote, String)} instead of it,
     * post the statusRemote to the Remote by Internet
     * 
     * @param deviceKey the device's key
     * @param statusRemote the status of Remote
     * @return whether the command executed suc
     */
    boolean doCommandRemotePostStatusInternet(String deviceKey, IEspStatusRemote statusRemote);
    
    /**
     * post the statusRemote to the Remote by Internet
     * 
     * @param deviceKey the device's key
     * @param statusRemote the status of Remote
     * @param router the Romte's router
     * @return whether the command executed suc
     */
    boolean doCommandRemotePostStatusInternet(String deviceKey, IEspStatusRemote statusRemote, String router);
}
