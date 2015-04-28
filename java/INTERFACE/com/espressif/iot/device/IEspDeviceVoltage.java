package com.espressif.iot.device;

import com.espressif.iot.type.device.status.IEspStatusVoltage;

public interface IEspDeviceVoltage extends IEspDevice
{
    /**
     * Get the status of the voltage device
     * 
     * @return the status @see IEspStatusVoltage
     */
    IEspStatusVoltage getStatusVoltage();
    
    /**
     * Set the status of the voltage device
     * 
     * @param statusVoltage @see IEspStatusVoltage
     */
    void setStatusVoltage(IEspStatusVoltage statusVoltage);
}
