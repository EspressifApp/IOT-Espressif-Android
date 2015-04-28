package com.espressif.iot.type.device.status;

import com.espressif.iot.type.device.IEspDeviceStatus;

public interface IEspStatusSensor extends IEspDeviceStatus
{
    /**
     * Get the UTC time of the status produced(stamped by Server)
     * 
     * @return the UTC time of the status produced(stamped by Server)
     */
    long getAt();
    
    /**
     * Set the UTC time of the status produced(stamped by Server)
     * 
     * @param at the UTC time of the status produced(stamped by Server)
     */
    void setAt(long at);
    
    /**
     * Get the x value of the status
     * 
     * @return the X value of the status
     */
    double getX();
    
    /**
     * Set the x value of the status
     * 
     * @param x the X value of the status
     */
    void setX(double x);
    
    /**
     * Get the y value of the status
     * 
     * @return the Y value of the status
     */
    double getY();
    
    /**
     * Set the x value of the status
     * 
     * @param y the Y value of the status
     */
    void setY(double y);
    
    /**
     * Get whether the sensor status support X
     * @return whether the sensor status support X
     */
    boolean isXSupported();
    
    /**
     * Get whether the sensor status support Y
     * @return whether the sensor status support Y
     */
    boolean isYSupported();
}
