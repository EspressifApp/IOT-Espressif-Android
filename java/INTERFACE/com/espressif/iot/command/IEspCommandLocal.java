package com.espressif.iot.command;


/**
 * IEspCommandLocal indicate that the action is related to local
 * 
 * @author afunx
 * 
 */
public interface IEspCommandLocal extends IEspCommand
{
    static final String Status = "status";
    
    static final String Response = "Response";
    
    static final String Freq = "freq";
    
    static final String Rgb = "rgb";
    
    static final String Red = "red";
    
    static final String Green = "green";
    
    static final String Blue = "blue";
    
    static final String Remote = "remote";
    
    static final String Addr = "addr";
    
    static final String Cmd = "cmd";
    
    static final String Rep = "rep";
}
