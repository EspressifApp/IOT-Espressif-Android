package com.espressif.iot.command.device.remote;

import com.espressif.iot.command.IEspCommandInternet;
import com.espressif.iot.command.device.IEspCommandRemote;
import com.espressif.iot.type.device.status.IEspStatusRemote;

public interface IEspCommandRemotePostStatusInternet extends IEspCommandInternet, IEspCommandRemote
{
    /**
     * post the statusRemote to the Remote by Internet
     * 
     * @param deviceKey the device's key
     * @param statusRemote the status of Remote
     * @return whether the command executed suc
     */
    boolean doCommandRemotePostStatusInternet(String deviceKey, IEspStatusRemote statusRemote);
}
