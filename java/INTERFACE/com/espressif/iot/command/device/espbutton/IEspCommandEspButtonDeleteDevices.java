package com.espressif.iot.command.device.espbutton;

import java.util.List;

import com.espressif.iot.command.IEspCommandLocal;
import com.espressif.iot.device.IEspDevice;

public interface IEspCommandEspButtonDeleteDevices extends IEspCommandEspButton, IEspCommandLocal
{
    boolean doCommandEspButtonDeleteDevices(IEspDevice inetDevice, String buttonMac, List<IEspDevice> delDevices);
}
