package com.espressif.iot.action.device.espbutton;

import java.util.List;

import com.espressif.iot.command.device.espbutton.EspCommandEspButtonActionGet;
import com.espressif.iot.command.device.espbutton.IEspCommandEspButtonActionGet;
import com.espressif.iot.device.IEspDevice;
import com.espressif.iot.type.device.other.EspButtonKeySettings;

public class EspActionEspButtonActionGet implements IEspActionEspButtonActionGet
{

    @Override
    public List<EspButtonKeySettings> doActionEspButtonActionGet(IEspDevice device)
    {
        IEspCommandEspButtonActionGet command = new EspCommandEspButtonActionGet();
        return command.doCommandEspButtonActionGet(device);
    }
    
}
