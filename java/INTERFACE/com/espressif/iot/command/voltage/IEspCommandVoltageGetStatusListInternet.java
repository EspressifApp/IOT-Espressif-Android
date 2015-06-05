package com.espressif.iot.command.voltage;

import java.util.List;

import com.espressif.iot.command.IEspCommandInternet;
import com.espressif.iot.command.device.IEspCommandVoltage;
import com.espressif.iot.type.device.status.IEspStatusVoltage;

public interface IEspCommandVoltageGetStatusListInternet extends IEspCommandInternet, IEspCommandVoltage
{
    /**
     * get the statusVoltage list to the Voltage by Internet
     * 
     * @param deviceKey the device key
     * @param startTimestamp the start of UTC timestamp
     * @param endTimestamp the end of UTC timestamp
     * @return the list of EspStatusVoltage
     */
    List<IEspStatusVoltage> doCommandVoltageGetStatusListInternet(String deviceKey, long startTimestamp,
        long endTimestamp);
}
