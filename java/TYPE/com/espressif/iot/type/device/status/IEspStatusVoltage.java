package com.espressif.iot.type.device.status;

public interface IEspStatusVoltage extends IEspStatusSensor
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
     * Get the x value of the status(here means voltage)
     * 
     * @return the X value of the status(here means voltage)
     */
    double getX();
    
    /**
     * Set the x value of the status(here means voltage)
     * 
     * @param x the X value of the status(here means voltage)
     */
    void setX(double x);
}
