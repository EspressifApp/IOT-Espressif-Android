package com.espressif.iot.command.device.espbutton;

import java.util.List;

import com.espressif.iot.command.IEspCommandLocal;
import com.espressif.iot.device.IEspDevice;

public interface IEspCommandEspButtonGroupDeviceMoveInto extends IEspCommandEspButton, IEspCommandLocal
{
    public boolean doCommandEspButtonGroupMoveIntoDevices(IEspDevice inetDevice, String buttonMac, long groupId,
        List<IEspDevice> moveDevices);
}
