package com.espressif.iot.command.device.plugs;

import com.espressif.iot.command.IEspCommandInternet;
import com.espressif.iot.command.device.IEspCommandPlugs;
import com.espressif.iot.type.device.status.IEspStatusPlugs;

public interface IEspCommandPlugsGetStatusInternet extends IEspCommandInternet, IEspCommandPlugs
{
    IEspStatusPlugs doCommandPlugsGetStatusInternet(String deviceKey);
}
