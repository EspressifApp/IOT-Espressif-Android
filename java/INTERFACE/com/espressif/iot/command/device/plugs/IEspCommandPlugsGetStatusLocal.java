package com.espressif.iot.command.device.plugs;

import java.net.InetAddress;

import com.espressif.iot.command.IEspCommandLocal;
import com.espressif.iot.command.device.IEspCommandPlugs;
import com.espressif.iot.type.device.status.IEspStatusPlugs;

public interface IEspCommandPlugsGetStatusLocal extends IEspCommandLocal, IEspCommandPlugs
{
    IEspStatusPlugs doCommandPlugsGetStatusLocal(InetAddress inetAddress, String deviceBssid, String router);
}
