package com.espressif.iot.device;

import com.espressif.iot.type.device.status.IEspStatusPlug;

public interface IEspDevicePlug extends IEspDevice
{
    /**
     * Get the status of the plug
     * 
     * @return the status @see IEspStatusPlug
     */
    IEspStatusPlug getStatusPlug();
    
    /**
     * Set the status of the plug
     * 
     * @param statusPlug @see IEspStatusPlug
     */
    void setStatusPlug(IEspStatusPlug statusPlug);
}
