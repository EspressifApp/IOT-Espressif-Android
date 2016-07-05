package com.espressif.iot.command.device.light;

import com.espressif.iot.command.IEspCommandInternet;
import com.espressif.iot.command.device.IEspCommandLight;
import com.espressif.iot.device.IEspDevice;
import com.espressif.iot.type.device.status.IEspStatusLight;

public interface IEspCommandLightGetStatusInternet extends IEspCommandInternet, IEspCommandLight
{
    static final String URL = "https://iot.espressif.cn/v1/datastreams/light/datapoint/?deliver_to_device=true";
    
    /**
     * get the statusLight to the Light by Internet
     * 
     * @param device
     * @return the status of the Light or null(if executed fail)
     */
    IEspStatusLight doCommandLightGetStatusInternet(IEspDevice device);
}
