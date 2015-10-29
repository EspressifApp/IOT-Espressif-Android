package com.espressif.iot.command.device.espbutton;

import java.util.List;

import com.espressif.iot.command.IEspCommandLocal;
import com.espressif.iot.device.IEspDevice;

public interface IEspCommandEspButtonGetDevices extends IEspCommandLocal, IEspCommandEspButton
{
    /**
     * Get EspButton's all devices
     * 
     * @param inetDevice
     * @param buttonMac
     * @return list of devices' BSSID
     */
    List<String> doCommandEspButtonGetDevices(IEspDevice inetDevice, String buttonMac);
}
