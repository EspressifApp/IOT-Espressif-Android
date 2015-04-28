package com.espressif.iot.device;

import com.espressif.iot.type.device.status.IEspStatusFlammable;

public interface IEspDeviceFlammable extends IEspDevice
{
    /**
     * Get the status of the flammable device
     * 
     * @return the status @see IEspStatusFlammable
     */
    IEspStatusFlammable getStatusFlammable();
    
    /**
     * Set the status of the flammable device
     * 
     * @param statusFlammable @see IEspStatusFlammable
     */
    void setStatusFlammable(IEspStatusFlammable statusFlammable);
}
