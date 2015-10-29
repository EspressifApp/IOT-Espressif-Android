package com.espressif.iot.command.device.espbutton;

import java.util.List;

import com.espressif.iot.command.IEspCommandLocal;
import com.espressif.iot.device.IEspDevice;

public interface IEspCommandEspButtonConfigure extends IEspCommandLocal, IEspCommandEspButton
{
    public static final int RESULT_SUC = 1;
    public static final int RESULT_FAILED = 2;
    public static final int RESULT_OVER = 3;
    
    public static final String KEY_PATH = "path";
    public static final String PATH_BROADCAST = "/device/button/configure";
    public static final String PATH_PING = "/device/ping";
    public static final String PATH_PAIR_REQUEST = "/device/button/pair/request";
    public static final String PATH_PAIR_RESULT = "/device/button/pair/result";
    
    /**
     * Configure EspButton
     * 
     * @param devices
     * @param newTempKey
     * @param newMacAddress
     * @param isBroadcast
     * @param permitAllRequest
     * @param listener
     * @param oldMacAddress
     * @return result
     */
    int doCommandEspButtonConfigure(List<IEspDevice> devices, String newTempKey, String newMacAddress,
        boolean isBroadcast, boolean permitAllRequest, IEspButtonConfigureListener listener, String... oldMacAddress);
}
