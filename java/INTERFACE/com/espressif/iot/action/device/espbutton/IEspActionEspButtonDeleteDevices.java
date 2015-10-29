package com.espressif.iot.action.device.espbutton;

import java.util.List;

import com.espressif.iot.action.IEspActionLocal;
import com.espressif.iot.device.IEspDevice;

public interface IEspActionEspButtonDeleteDevices extends IEspActionLocal
{
    public boolean doActionEspButtonDeleteDevices(IEspDevice inetDevice, String buttonMac, List<IEspDevice> delDevices);
}
