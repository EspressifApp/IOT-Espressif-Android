package com.espressif.iot.type.device.status;

public interface IEspStatusFlammable extends IEspStatusSensor
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
     * Get the x value of the status(here means gas pressure)
     * 
     * @return the X value of the status(here means gas pressure)
     */
    double getX();
    
    /**
     * Set the x value of the status(here means gas pressure)
     * 
     * @param x the X value of the status(here means gas pressure)
     */
    void setX(double x);
}
