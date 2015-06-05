package com.espressif.iot.command.device.flammable;

import java.util.List;

import com.espressif.iot.command.IEspCommandInternet;
import com.espressif.iot.command.device.IEspCommandFlammable;
import com.espressif.iot.type.device.status.IEspStatusFlammable;

public interface IEspCommandFlammableGetStatusListInternet extends IEspCommandInternet, IEspCommandFlammable
{
    static final String URL = "https://iot.espressif.cn/v1/datastreams/flammable_gas/datapoints/";
    
    /**
     * get the statusFlammable list to the Flammable by Internet
     * 
     * @param deviceKey the device key
     * @param startTimestamp the start of UTC timestamp
     * @param endTimestamp the end of UTC timestamp
     * @return the list of EspStatusFlammable
     */
    List<IEspStatusFlammable> doCommandFlammableGetStatusListInternet(String deviceKey, long startTimestamp,
        long endTimestamp);
}
