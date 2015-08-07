package com.espressif.iot.command.device.plugs;

import java.net.InetAddress;

import com.espressif.iot.command.IEspCommandLocal;
import com.espressif.iot.command.device.IEspCommandPlugs;
import com.espressif.iot.type.device.status.IEspStatusPlugs;

public interface IEspCommandPlugsGetStatusLocal extends IEspCommandLocal, IEspCommandPlugs
{
    /**
     * get the statusPlugs to the Plugs by Local
     * 
     * @param inetAddress the Plugs's ip address
     * @param deviceBssid the Plugs's bssid
     * @param isMeshDevice whether the Plugs is mesh device
     * @return the status of the Plugs or null(if executed fail)
     */
    IEspStatusPlugs doCommandPlugsGetStatusLocal(InetAddress inetAddress, String deviceBssid, boolean isMeshDevice);
}
