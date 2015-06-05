package com.espressif.iot.command.device.plug;

import com.espressif.iot.command.IEspCommandInternet;
import com.espressif.iot.command.device.IEspCommandPlug;
import com.espressif.iot.type.device.status.IEspStatusPlug;

public interface IEspCommandPlugGetStatusInternet extends IEspCommandInternet, IEspCommandPlug
{
    static final String URL = "https://iot.espressif.cn/v1/datastreams/plug-status/datapoint/?deliver_to_device=true";
    
    /**
     * get the statusPlug to the Plug by Internet
     * 
     * @param deviceKey the device key
     * @return the status of the Plug or null(if executed fail)
     */
    IEspStatusPlug doCommandPlugGetStatusInternet(String deviceKey);
}
