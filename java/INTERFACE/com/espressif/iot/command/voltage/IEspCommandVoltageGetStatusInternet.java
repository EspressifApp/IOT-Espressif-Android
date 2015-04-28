package com.espressif.iot.command.voltage;

import com.espressif.iot.command.IEspCommandInternet;
import com.espressif.iot.command.device.IEspCommandFlammable;
import com.espressif.iot.type.device.status.IEspStatusVoltage;

public interface IEspCommandVoltageGetStatusInternet extends IEspCommandInternet, IEspCommandFlammable
{
    final String URL = "https://iot.espressif.cn/v1/datastreams/supply-voltage/datapoints/";
    
    /**
     * get the statusVoltage to the Voltage by Internet
     * 
     * @param deviceKey the device key
     * @return the status of the Voltage or null(if executed fail)
     */
    IEspStatusVoltage doCommandVoltageGetStatusInternet(String deviceKey);
}
