package com.espressif.iot.command.device;

/**
 * IEspCommandSensor indicate that the command is belong to sensor,
 * sensor is an abstract device, its children are concrete devices
 * 
 * @author afunx
 *
 */
public interface IEspCommandSensor extends IEspCommandDevice
{
    static final int PAGE_NUMBER = 1000;
    
    static final String At = "at";
}
