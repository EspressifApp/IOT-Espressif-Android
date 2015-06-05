package com.espressif.iot.command.device.humiture;

import java.util.List;

import com.espressif.iot.command.IEspCommandInternet;
import com.espressif.iot.command.device.IEspCommandHumiture;
import com.espressif.iot.type.device.status.IEspStatusHumiture;

public interface IEspCommandHumitureGetStatusListInternet extends IEspCommandInternet, IEspCommandHumiture
{
    static final String URL = "https://iot.espressif.cn/v1/datastreams/tem_hum/datapoints/";
    
    /**
     * get the statusHumiture list to the Humiture by Internet
     * 
     * @param deviceKey the device key
     * @param startTimestamp the start of UTC timestamp
     * @param endTimestamp the end of UTC timestamp
     * @return the list of EspStatusHumiture
     */
    List<IEspStatusHumiture> doCommandHumitureGetStatusListInternet(String deviceKey, long startTimestamp,
        long endTimestamp);
}
