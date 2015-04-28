package com.espressif.iot.command.device.plugs;

import java.net.InetAddress;

import com.espressif.iot.command.IEspCommandLocal;
import com.espressif.iot.command.device.IEspCommandPlugs;
import com.espressif.iot.type.device.status.IEspStatusPlugs;

public interface IEspCommandPlugsPostStatusLocal extends IEspCommandLocal, IEspCommandPlugs
{
    boolean doCommandPlugsPostStatusLocal(InetAddress inetAddress, IEspStatusPlugs status, String deviceBssid,
        String router);
}
