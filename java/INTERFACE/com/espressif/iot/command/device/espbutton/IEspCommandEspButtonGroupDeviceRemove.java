package com.espressif.iot.command.device.espbutton;

import java.util.List;

import com.espressif.iot.command.IEspCommandLocal;
import com.espressif.iot.device.IEspDevice;

public interface IEspCommandEspButtonGroupDeviceRemove extends IEspCommandEspButton, IEspCommandLocal
{
    public boolean doCommandEspButtonGroupRemoveDevice(IEspDevice inetDevice, String buttonMac, long groupId,
        List<IEspDevice> removeDevices);
}
