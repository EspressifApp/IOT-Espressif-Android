package com.espressif.iot.command.device.plug;

import java.util.List;

import com.espressif.iot.command.IEspCommandInternet;
import com.espressif.iot.command.device.IEspCommandPlug;
import com.espressif.iot.type.device.status.IEspStatusPlug;

public interface IEspCommandPlugPostStatusInternet extends IEspCommandInternet, IEspCommandPlug
{
    static final String URL = "https://iot.espressif.cn/v1/datastreams/plug-status/datapoint/?deliver_to_device=true";
    
    /**
     * post the statusPlug to the Plug by Internet
     * 
     * @param deviceKey the device's key
     * @param statusPlug the status of Plug
     * @return whether the command executed suc
     */
    boolean doCommandPlugPostStatusInternet(String deviceKey, IEspStatusPlug statusPlug);
    
    /**
     * post multicast internet
     * 
     * @param deviceKey
     * @param statusPlug
     * @param bssids
     * @return
     */
    boolean doCommandMulticastPostStatusInternet(String deviceKey, IEspStatusPlug statusPlug, List<String> bssids);
}
