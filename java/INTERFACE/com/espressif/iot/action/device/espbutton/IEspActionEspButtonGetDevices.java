package com.espressif.iot.action.device.espbutton;

import java.util.List;

import com.espressif.iot.action.IEspActionLocal;
import com.espressif.iot.device.IEspDevice;

public interface IEspActionEspButtonGetDevices extends IEspActionLocal
{
    List<IEspDevice> doAcitonEspButtonGetDevices(IEspDevice inetDevice, String buttonMac);
}
