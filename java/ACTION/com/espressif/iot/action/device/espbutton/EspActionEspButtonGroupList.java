package com.espressif.iot.action.device.espbutton;

import java.util.List;

import com.espressif.iot.command.device.espbutton.EspCommandEspButtonGroupList;
import com.espressif.iot.command.device.espbutton.IEspCommandEspButtonGroupList;
import com.espressif.iot.device.IEspDevice;
import com.espressif.iot.espbutton.IEspButtonGroup;

public class EspActionEspButtonGroupList implements IEspActionEspButtonGroupList
{

    @Override
    public List<IEspButtonGroup> doActionEspButtonListGroup(IEspDevice inetDevice, String buttonMac)
    {
        IEspCommandEspButtonGroupList command = new EspCommandEspButtonGroupList();
        return command.doCommandEspButtonListGroup(inetDevice, buttonMac);
    }
    
}
