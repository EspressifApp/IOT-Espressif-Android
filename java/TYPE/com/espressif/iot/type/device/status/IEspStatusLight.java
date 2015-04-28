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
     * Get frequency value of the light
     * 
     * @return the frequency value of the light
     */
    int getFreq();
    
    /**
     * Set frequency value of the light
     * 
     * @param freq the frequency value of the light
     */
    void setFreq(int freq);
}
