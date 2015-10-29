package com.espressif.iot.action.device.espbutton;

import java.util.List;

import com.espressif.iot.command.device.espbutton.EspCommandEspButtonGroupDeviceRemove;
import com.espressif.iot.command.device.espbutton.IEspCommandEspButtonGroupDeviceRemove;
import com.espressif.iot.device.IEspDevice;

public class EspActionEspButtonGroupDeviceRemove implements IEspActionEspButtonGroupDeviceRemove
{

    @Override
    public boolean doActionEspButtonGroupRemoveDevice(IEspDevice inetDevice, String buttonMac, long groupId,
        List<IEspDevice> removeDevices)
    {
        IEspCommandEspButtonGroupDeviceRemove command = new EspCommandEspButtonGroupDeviceRemove();
        return command.doCommandEspButtonGroupRemoveDevice(inetDevice, buttonMac, groupId, removeDevices);
    }
    
}
