package com.espressif.iot.action.device.espbutton;

import com.espressif.iot.command.device.espbutton.EspCommandEspButtonActionSet;
import com.espressif.iot.command.device.espbutton.IEspCommandEspButtonActionSet;
import com.espressif.iot.device.IEspDevice;
import com.espressif.iot.type.device.other.EspButtonKeySettings;

public class EspActionEspButtonActionSet implements IEspActionEspButtonActionSet
{

    @Override
    public boolean doActionEspButtonActionSet(IEspDevice device, EspButtonKeySettings settings)
    {
        IEspCommandEspButtonActionSet command = new EspCommandEspButtonActionSet();
        return command.doCommandEspButtonActionSet(device, settings);
    }
    
}
