package com.espressif.iot.command.device.plugs;

import java.net.InetAddress;

import com.espressif.iot.command.IEspCommandLocal;
import com.espressif.iot.command.device.IEspCommandPlugs;
import com.espressif.iot.type.device.status.IEspStatusPlugs;

public interface IEspCommandPlugsPostStatusLocal extends IEspCommandLocal, IEspCommandPlugs
{
    /**
     * post the statusPlugs to the Plugs by Local
     * 
     * @param inetAddress the Plugs's ip address
     * @param statusPlugs the status of Plugs
     * @param deviceBssid the Plugs's bssid
     * @param isMeshDevice whether the Plugs is mesh device
     * @return whether the command executed suc
     */
    boolean doCommandPlugsPostStatusLocal(InetAddress inetAddress, IEspStatusPlugs status, String deviceBssid,
        boolean isMeshDevice);
}
