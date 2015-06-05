package com.espressif.iot.command.voltage;

import com.espressif.iot.command.IEspCommandInternet;
import com.espressif.iot.command.device.IEspCommandVoltage;
import com.espressif.iot.type.device.status.IEspStatusVoltage;

public interface IEspCommandVoltageGetStatusInternet extends IEspCommandInternet, IEspCommandVoltage
{
    /**
     * get the statusVoltage to the Voltage by Internet
     * 
     * @param deviceKey the device key
     * @return the status of the Voltage or null(if executed fail)
     */
    IEspStatusVoltage doCommandVoltageGetStatusInternet(String deviceKey);
}
