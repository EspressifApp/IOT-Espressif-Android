package com.espressif.iot.command.device;

/**
 * IEspCommandLight indicate that the command is belong to light
 * 
 * @author afunx
 * 
 */
public interface IEspCommandLight extends IEspCommandDevice
{
    public static final String PROTOCOL_NEW_VERSION = "b1.3.0t45772(o)";

    public static final String KEY_STATUS = "status";
    public static final String KEY_PERIOD = "period";
    public static final String KEY_RED = "red";
    public static final String KEY_GREEN = "green";
    public static final String KEY_BLUE = "blue";
    public static final String KEY_WHITE = "white";
    public static final String KEY_COLOR = "color";
    
    static final String Period = KEY_PERIOD;
    static final String Rgb = "rgb";
    static final String Red = KEY_RED;
    static final String Green = KEY_GREEN;
    static final String Blue = KEY_BLUE;
    static final String CWhite = "cwhite";
    static final String WWhite = "wwhite";

    static final String Switches = "switches";
    static final String Mac = "mac";
    static final String VoltageMV = "voltagemv";
    static final String StatusOK = "OK";
}
