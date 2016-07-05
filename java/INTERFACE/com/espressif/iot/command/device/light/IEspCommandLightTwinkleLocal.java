package com.espressif.iot.command.device.light;

import com.espressif.iot.command.IEspCommandLocal;
import com.espressif.iot.command.device.IEspCommandLight;
import com.espressif.iot.device.IEspDevice;

public interface IEspCommandLightTwinkleLocal extends IEspCommandLocal, IEspCommandLight {
    public static final String URL_PARAM_ON = "/twinkle_on";
    public static final String URL_PARAM_OFF = "/twinkle_off";

    public static final String KEY_IP = "ip";
    public static final String KEY_PORT = "port";
    public static final String KEY_ID = "id";
    
    public boolean doCommandPostTwinkleOn(IEspDevice device, String appIP, int appPort, String appId);

    public boolean doCommandPostTwinkleOff(IEspDevice device, String appIP, int appPort, String appId);
}
