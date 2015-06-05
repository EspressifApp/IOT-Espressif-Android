package com.espressif.iot.type.device.status;

import com.espressif.iot.type.device.IEspDeviceStatus;

public interface IEspStatusLight extends IEspDeviceStatus
{
    /**
     * Get red value of the light
     * 
     * @return red value of the light
     */
    int getRed();
    
    /**
     * Set red value of the light
     * 
     * @param red the red value of the light
     */
    void setRed(int red);
    
    /**
     * Get green value of the light
     * 
     * @return green value of the light
     */
    int getGreen();
    
    /**
     * Set green value of the light
     * 
     * @param green the green value of the light
     */
    void setGreen(int green);
    
    /**
     * Get blue value of the light
     * 
     * @return blue value of the light
     */
    int getBlue();
    
    /**
     * Set blue value of the light
     * 
     * @param blue the blue value of the light
     */
    void setBlue(int blue);
    
    /**
     * Get period value of the light
     * 
     * @return the period value of the light
     */
    int getPeriod();
    
    /**
     * Set period value of the light
     * 
     * @param period the period value of the light
     */
    void setPeriod(int period);
    
    /**
     * Get the cold white value of the light
     * 
     * @return the cold white value of the light
     */
    int getCWhite();
    
    /**
     * Set the cold white value of the light
     * 
     * @param cWhite the cold while value
     */
    void setCWhite(int cWhite);
    
    /**
     * Get the warm white value of the light
     * 
     * @return the warm white value of the light
     */
    int getWWhite();
    
    /**
     * Set the warm white value of the light
     * @param wWhite the warm value of the light
     */
    void setWWhite(int wWhite);
}
