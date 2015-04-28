package com.espressif.iot.device;

import com.espressif.iot.type.device.status.IEspStatusLight;

public interface IEspDeviceLight extends IEspDevice
{
    public static final int FREQ_MIN = 100;
    public static final int FREQ_MAX = 500;
    public static final int RGB_MAX = 254;
    
    /**
     * Get the status of the light
     * 
     * @return the status @see IEspStatusLight
     */
    IEspStatusLight getStatusLight();
    
    /**
     * Set the status of the light
     * 
     * @param statusLight @see IEspStatusLight
     */
    void setStatusLight(IEspStatusLight statusLight);
}
