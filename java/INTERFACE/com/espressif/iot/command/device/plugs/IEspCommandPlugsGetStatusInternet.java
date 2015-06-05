package com.espressif.iot.command.device.plugs;

import com.espressif.iot.command.IEspCommandInternet;
import com.espressif.iot.command.device.IEspCommandPlugs;
import com.espressif.iot.type.device.status.IEspStatusPlugs;

public interface IEspCommandPlugsGetStatusInternet extends IEspCommandInternet, IEspCommandPlugs
{
    /**
     * get the statusPlugs to the Plugs by Internet
     * 
     * @param deviceKey the device key
     * @return the status of the Plugs or null(if executed fail)
     */
    IEspStatusPlugs doCommandPlugsGetStatusInternet(String deviceKey);
}
