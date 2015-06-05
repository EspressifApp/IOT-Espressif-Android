package com.espressif.iot.command.device.plug;

import java.net.InetAddress;

import com.espressif.iot.command.IEspCommandInternet;
import com.espressif.iot.command.device.IEspCommandPlug;
import com.espressif.iot.type.device.status.IEspStatusPlug;

public interface IEspCommandPlugPostStatusInternet extends IEspCommandInternet, IEspCommandPlug
{
    static final String URL = "https://iot.espressif.cn/v1/datastreams/plug-status/datapoint/?deliver_to_device=true";
    
    /**
     * @deprecated Use {@link #doCommandPlugPostStatusInternet(InetAddress, IEspStatusPlug, String)} instead of it,
     * post the statusPlug to the Plug by Internet
     * 
     * @param deviceKey the device's key
     * @param statusPlug the status of Plug
     * @return whether the command executed suc
     */
    boolean doCommandPlugPostStatusInternet(String deviceKey, IEspStatusPlug statusPlug);
    
    /**
     * 
     * @param deviceKey the device's key
     * @param statusPlug the status of plug
     * @param router the Plug's router
     * @return whether the command executed suc
     */
    boolean doCommandPlugPostStatusInternet(String deviceKey, IEspStatusPlug statusPlug, String router);
}
