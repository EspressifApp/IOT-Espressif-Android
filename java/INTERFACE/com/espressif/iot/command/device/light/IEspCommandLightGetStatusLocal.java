package com.espressif.iot.command.device.light;

import com.espressif.iot.command.IEspCommandLocal;
import com.espressif.iot.command.device.IEspCommandLight;
import com.espressif.iot.device.IEspDevice;
import com.espressif.iot.type.device.status.IEspStatusLight;

public interface IEspCommandLightGetStatusLocal extends IEspCommandLocal, IEspCommandLight
{
    /**
     * get the statusLight to the Light by Local
     * 
     * @param device
     * @return the status of the Light or null(if executed fail)
     */
    IEspStatusLight doCommandLightGetStatusLocal(IEspDevice device);
}
