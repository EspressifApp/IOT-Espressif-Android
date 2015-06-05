package com.espressif.iot.command.device;

/**
 * IEspCommandVoltage indicate that the command is belong to voltage
 * 
 * @author afunx
 * 
 */
public interface IEspCommandVoltage extends IEspCommandSensor
{
    static final String URL = "https://iot.espressif.cn/v1/datastreams/supply-voltage/datapoints/";
}
