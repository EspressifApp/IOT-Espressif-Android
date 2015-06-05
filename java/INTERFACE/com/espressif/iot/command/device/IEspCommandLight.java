package com.espressif.iot.command.device;

/**
 * IEspCommandLight indicate that the command is belong to light
 * 
 * @author afunx
 * 
 */
public interface IEspCommandLight extends IEspCommandDevice
{
    static final String Period = "period";
    
    static final String Rgb = "rgb";
    
    static final String Red = "red";
    
    static final String Green = "green";
    
    static final String Blue = "blue";
    
    static final String CWhite = "cwhite";
    
    static final String WWhite = "wwhite";
}
