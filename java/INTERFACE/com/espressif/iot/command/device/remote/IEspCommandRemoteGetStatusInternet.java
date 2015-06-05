package com.espressif.iot.command.device.remote;

import com.espressif.iot.command.IEspCommandInternet;
import com.espressif.iot.command.device.IEspCommandRemote;
import com.espressif.iot.type.device.status.IEspStatusRemote;

public interface IEspCommandRemoteGetStatusInternet extends IEspCommandInternet, IEspCommandRemote
{
    /**
     * get the statusRemote to the Remote by Internet
     * 
     * @param deviceKey the device key
     * @return the status of the Remote or null(if executed fail)
     */
    IEspStatusRemote doCommandRemoteGetStatusInternet(String deviceKey);
}
