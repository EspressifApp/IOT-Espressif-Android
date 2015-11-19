package com.espressif.iot.command.device.espbutton;

import com.espressif.iot.command.device.IEspCommandDevice;

public interface IEspCommandEspButton extends IEspCommandDevice
{
    public static final String KEY_BUTTON_NEW = "button_new";
    public static final String KEY_TEMP_KEY = "temp_key";
    public static final String KEY_BUTTON_MAC = "button_mac";
    public static final String KEY_REPLACE = "replace";
    public static final String KEY_BUTTON_REMOVE = "button_remove";
    public static final String KEY_MAC_LEN = "mac_len";
    public static final String KEY_MAC = "mac";
    public static final String KEY_DEVICE_MAC = "device_mac";
    public static final String KEY_RESULT = "result";
    public static final String KEY_GROUPS = "groups";
    public static final String KEY_GROUP_ID = "group_id";
    
    public static final String BUTTON_KEY_REPLACE = "XX";
    public static final String KEY_SHORT_PRESS = "tapXX";
    public static final String KEY_SHROT_PRESS_ARG = "argXXtap";
    public static final String KEY_LONG_PRESS = "pressXX";
    public static final String KEY_LONG_PRESS_ARG = "argXXpress";
}
