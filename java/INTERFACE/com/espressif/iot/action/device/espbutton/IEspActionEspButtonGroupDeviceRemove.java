package com.espressif.iot.action.device.espbutton;

import java.util.List;

import com.espressif.iot.action.IEspActionLocal;
import com.espressif.iot.device.IEspDevice;

public interface IEspActionEspButtonGroupDeviceRemove extends IEspActionLocal
{
    public boolean doActionEspButtonGroupRemoveDevice(IEspDevice inetDevice, String buttonMac, long groupId,
        List<IEspDevice> removeDevices);
}
