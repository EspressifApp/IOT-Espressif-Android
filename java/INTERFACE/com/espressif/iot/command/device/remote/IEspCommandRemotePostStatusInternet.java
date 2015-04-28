package com.espressif.iot.command.device.remote;

import com.espressif.iot.command.IEspCommandInternet;
import com.espressif.iot.command.device.IEspCommandRemote;
import com.espressif.iot.type.device.status.IEspStatusRemote;

public interface IEspCommandRemotePostStatusInternet extends IEspCommandInternet, IEspCommandRemote
{
    final String URL = "https://iot.espressif.cn/v1/datastreams/remote/datapoint/?deliver_to_device=true";
    
    /**
     * post the statusRemote to the Remote by Internet
     * 
     * @param deviceKey the device's key
     * @param statusRemote the status of Remote
     * @return whether the command executed suc
     */
    boolean doCommandRemotePostStatusInternet(String deviceKey, IEspStatusRemote statusRemote);
    
    boolean doCommandRemotePostStatusInternet(String deviceKey, IEspStatusRemote statusRemote, String router);
}
