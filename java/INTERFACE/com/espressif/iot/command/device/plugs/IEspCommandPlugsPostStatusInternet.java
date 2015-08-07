package com.espressif.iot.command.device.plugs;

import com.espressif.iot.command.IEspCommandInternet;
import com.espressif.iot.command.device.IEspCommandPlugs;
import com.espressif.iot.type.device.status.IEspStatusPlugs;

public interface IEspCommandPlugsPostStatusInternet extends IEspCommandInternet, IEspCommandPlugs
{
    /**
     * post the statusPlugs to the Plugs by Internet
     * 
     * @param deviceKey the device's key
     * @param statusPlug the status of Plugs
     * @return whether the command executed suc
     */
    boolean doCommandPlugsPostStatusInternet(String deviceKey, IEspStatusPlugs status);
}
