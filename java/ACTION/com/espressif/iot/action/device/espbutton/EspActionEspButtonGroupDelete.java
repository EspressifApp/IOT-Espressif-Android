package com.espressif.iot.action.device.espbutton;

import com.espressif.iot.command.device.espbutton.EspCommandEspButtonGroupDelete;
import com.espressif.iot.command.device.espbutton.IEspCommandEspButtonGroupDelete;
import com.espressif.iot.device.IEspDevice;

public class EspActionEspButtonGroupDelete implements IEspActionEspButtonGroupDelete
{

    @Override
    public boolean doActionEspButtonDeleteGroup(IEspDevice inetDevice, String buttonMac, long[] delGroupIds)
    {
        IEspCommandEspButtonGroupDelete command = new EspCommandEspButtonGroupDelete();
        return command.doCommandEspButtonDeleteGroup(inetDevice, buttonMac, delGroupIds);
    }
    
}
