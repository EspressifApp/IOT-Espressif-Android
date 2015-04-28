package com.espressif.iot.type.device.status;

public interface IEspStatusHumiture extends IEspStatusSensor
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
     * Get the x value of the status(here means temperature)
     * 
     * @return the X value of the status(here means temperature)
     */
    double getX();
    
    /**
     * Set the x value of the status(here means temperature)
     * 
     * @param x the X value of the status(here means temperature)
     */
    void setX(double x);
    
    /**
     * Get the y value of the status(here means humidity)
     * 
     * @return the Y value of the status(here means humidity)
     */
    double getY();
    
    /**
     * Set the x value of the status(here means humidity)
     * 
     * @param y the Y value of the status(here means humidity)
     */
    void setY(double y);
}
