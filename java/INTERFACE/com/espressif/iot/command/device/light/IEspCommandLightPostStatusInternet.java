package com.espressif.iot.command.device.light;

import com.espressif.iot.command.IEspCommandInternet;
import com.espressif.iot.command.device.IEspCommandLight;
import com.espressif.iot.type.device.status.IEspStatusLight;

public interface IEspCommandLightPostStatusInternet extends IEspCommandInternet, IEspCommandLight
{
    static final String URL = "https://iot.espressif.cn/v1/datastreams/light/datapoint/?deliver_to_device=true";
    
    /**
     * post the statusLight to the Light by Internet
     * 
     * @param deviceKey the device's key
     * @param statusLight the status of Light
     * @return whether the command executed suc
     */
    boolean doCommandLightPostStatusInternet(String deviceKey, IEspStatusLight statusLight);
}
