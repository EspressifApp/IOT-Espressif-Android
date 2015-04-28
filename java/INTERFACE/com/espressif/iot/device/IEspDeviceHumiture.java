package com.espressif.iot.device;

import com.espressif.iot.type.device.status.IEspStatusHumiture;

public interface IEspDeviceHumiture extends IEspDevice
{
    /**
     * Get the status of the humiture
     * 
     * @return the the status @see IEspStatusHumiture
     */
    IEspStatusHumiture getStatusHumiture();
    
    /**
     * Set the status of the humiture
     * 
     * @param status @see IEspStatusHumiture
     */
    void setStatusHumiture(IEspStatusHumiture status);
}
