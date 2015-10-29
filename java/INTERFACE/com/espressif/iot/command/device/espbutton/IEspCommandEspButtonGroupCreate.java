package com.espressif.iot.command.device.espbutton;

import com.espressif.iot.command.IEspCommandLocal;
import com.espressif.iot.device.IEspDevice;

public interface IEspCommandEspButtonGroupCreate extends IEspCommandEspButton, IEspCommandLocal
{
    public long doCommandEspButtonCreateGroup(IEspDevice inetDevice, String buttonMac);
}
