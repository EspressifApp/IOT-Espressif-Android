package com.espressif.iot.device;

import com.espressif.iot.type.device.status.IEspStatusRemote;

public interface IEspDeviceRemote extends IEspDevice
{
    /**
     * Get the status of the remote
     * 
     * @return the status @see IEspStatusRemote
     */
    IEspStatusRemote getStatusRemote();
    
    /**
     * Set the status of the remote
     * 
     * @param statusRemote @see IEspStatusRemote
     */
    void setStatusFlammable(IEspStatusRemote statusRemote);
}
