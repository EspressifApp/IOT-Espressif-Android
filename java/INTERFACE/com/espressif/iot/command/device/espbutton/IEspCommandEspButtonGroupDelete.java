package com.espressif.iot.command.device.espbutton;

import com.espressif.iot.command.IEspCommandLocal;
import com.espressif.iot.device.IEspDevice;

public interface IEspCommandEspButtonGroupDelete extends IEspCommandEspButton, IEspCommandLocal
{
    public boolean doCommandEspButtonDeleteGroup(IEspDevice inetDevice, String buttonMac, long[] delGroupIds);
}
