package com.espressif.iot.command.device.light;

import java.net.InetAddress;
import java.util.List;

import com.espressif.iot.command.IEspCommandLocal;
import com.espressif.iot.command.device.IEspCommandLight;
import com.espressif.iot.device.IEspDevice;
import com.espressif.iot.type.device.status.IEspStatusLight;

public interface IEspCommandLightPostStatusLocal extends IEspCommandLocal, IEspCommandLight {
    /**
     * Post the statusLight to the Light by Local
     * 
     * @param device
     * @param statusLight
     * @return
     */
    boolean doCommandLightPostStatusLocal(IEspDevice device, IEspStatusLight statusLight);

    /**
     * Post the statusLight to the Light by Local Instantly(without response)
     * 
     * @param device
     * @param statusLight
     * @param disconnectedCallback
     */
    void doCommandLightPostStatusLocalInstantly(IEspDevice device, IEspStatusLight statusLight,
        Runnable disconnectedCallback);

    /**
     * Post multicast status
     * 
     * @param inetAddress
     * @param statusLight
     * @param bssids
     * @return
     */
    boolean doCommandMulticastPostStatusLocal(InetAddress inetAddress, IEspStatusLight statusLight,
        List<String> bssids);
}
