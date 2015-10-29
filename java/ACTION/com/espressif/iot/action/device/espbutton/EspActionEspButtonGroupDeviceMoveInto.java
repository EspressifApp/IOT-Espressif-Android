package com.espressif.iot.action.device.espbutton;

import java.util.List;

import com.espressif.iot.command.device.espbutton.EspCommandEspButtonGroupDeviceMoveInto;
import com.espressif.iot.command.device.espbutton.IEspCommandEspButtonGroupDeviceMoveInto;
import com.espressif.iot.device.IEspDevice;

public class EspActionEspButtonGroupDeviceMoveInto implements IEspActionEspButtonGroupDeviceMoveInto
{

    @Override
    public boolean doActionEspButtonGroupMoveIntoDevices(IEspDevice inetDevice, String buttonMac, long groupId,
        List<IEspDevice> moveDevices)
    {
        IEspCommandEspButtonGroupDeviceMoveInto command = new EspCommandEspButtonGroupDeviceMoveInto();
        return command.doCommandEspButtonGroupMoveIntoDevices(inetDevice, buttonMac, groupId, moveDevices);
    }
    
}
