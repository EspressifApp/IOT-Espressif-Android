package com.espressif.iot.command.device.humiture;

import com.espressif.iot.command.IEspCommandInternet;
import com.espressif.iot.command.device.IEspCommandHumiture;
import com.espressif.iot.type.device.status.IEspStatusHumiture;

public interface IEspCommandHumitureGetStatusInternet extends IEspCommandInternet, IEspCommandHumiture
{
    static final String URL = "https://iot.espressif.cn/v1/datastreams/tem_hum/datapoints/";
    
    /**
     * get the statusHumiture to the Humiture by Internet
     * 
     * @param deviceKey the device key
     * @return the status of the Humiture or null(if executed fail)
     */
    IEspStatusHumiture doCommandHumitureGetStatusInternet(String deviceKey);
}
