package com.espressif.iot.type.device.status;

import com.espressif.iot.type.device.IEspDeviceStatus;

public interface IEspStatusPlug extends IEspDeviceStatus
{
    /**
     * Check whether the plug is on
     * 
     * @return whether the plug is on
     */
    boolean isOn();
    
    /**
     * Set whether the plug is on
     * 
     * @param isOn whether the plug is on
     */
    void setIsOn(boolean isOn);
}
