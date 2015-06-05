package com.espressif.iot.device;

import com.espressif.iot.type.device.status.IEspStatusLight;

public interface IEspDeviceLight extends IEspDevice
{
    public static final int PERIOD_MIN = 1000;
    public static final int PERIOD_MAX = 10000;
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
