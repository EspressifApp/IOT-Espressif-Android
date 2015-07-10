package com.espressif.iot.type.device.status;

import com.espressif.iot.type.device.IEspDeviceStatus;

public interface IEspStatusEspnow extends IEspDeviceStatus
{
    public final static int LOW_BATTERY_VOLTAGEMV = 2560;
    
    /**
     * Set the mac address of the Espnow
     * 
     * @param mac
     */
    public void setMac(String mac);
    
    /**
     * 
     * @return the mac address of the Espnow
     */
    public String getMac();
    
    /**
     * Set the voltage of the Espnow
     * 
     * @param voltage
     */
    public void setVoltage(int voltage);
    
    /**
     * 
     * @return get the voltage of the Espnow
     */
    public int getVoltage();
    
    /**
     * 
     * @return whether the Espnow is low battery
     */
    public boolean isLowBattery();
}
