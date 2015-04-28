package com.espressif.iot.command.device.plugs;

import com.espressif.iot.command.IEspCommandInternet;
import com.espressif.iot.command.device.IEspCommandPlugs;
import com.espressif.iot.type.device.status.IEspStatusPlugs;

public interface IEspCommandPlugsPostStatusInternet extends IEspCommandInternet, IEspCommandPlugs
{
    boolean doCommandPlugsPostStatusInternet(String deviceKey, IEspStatusPlugs status, String router);
}
