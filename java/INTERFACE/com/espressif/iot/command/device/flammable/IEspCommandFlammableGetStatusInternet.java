package com.espressif.iot.command.device.flammable;

import com.espressif.iot.command.IEspCommandInternet;
import com.espressif.iot.command.device.IEspCommandFlammable;
import com.espressif.iot.type.device.status.IEspStatusFlammable;

public interface IEspCommandFlammableGetStatusInternet extends IEspCommandInternet, IEspCommandFlammable
{
    static final String URL = "https://iot.espressif.cn/v1/datastreams/flammable_gas/datapoints/";
    
    /**
     * get the statusFlammable to the Flammable by Internet
     * 
     * @param deviceKey the device key
     * @return the status of the Flammable or null(if executed fail)
     */
    IEspStatusFlammable doCommandFlammableGetStatusInternet(String deviceKey);
}
