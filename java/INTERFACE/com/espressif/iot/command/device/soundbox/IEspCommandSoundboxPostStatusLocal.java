package com.espressif.iot.command.device.soundbox;

import java.net.InetAddress;

import com.espressif.iot.command.IEspCommandLocal;
import com.espressif.iot.command.device.IEspCommandSoundbox;
import com.espressif.iot.type.device.status.IEspStatusSoundbox;

public interface IEspCommandSoundboxPostStatusLocal extends IEspCommandLocal, IEspCommandSoundbox {

    /**
     * Post new status to the soundbox
     * 
     * @param inetAddress
     * @param status
     * @param deviceBssid
     * @param isMesh
     * @return
     */
    public boolean doCommandPlugsPostStatusLocal(InetAddress inetAddress, IEspStatusSoundbox status, String deviceBssid,
        boolean isMesh);
}
