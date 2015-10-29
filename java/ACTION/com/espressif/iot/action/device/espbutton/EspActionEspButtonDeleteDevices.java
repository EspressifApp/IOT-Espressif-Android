package com.espressif.iot.action.device.espbutton;

import java.util.List;

import com.espressif.iot.command.device.espbutton.EspCommandEspButtonDeleteDevices;
import com.espressif.iot.command.device.espbutton.IEspCommandEspButtonDeleteDevices;
import com.espressif.iot.device.IEspDevice;

public class EspActionEspButtonDeleteDevices implements IEspActionEspButtonDeleteDevices
{
    
    @Override
    public boolean doActionEspButtonDeleteDevices(IEspDevice inetDevice, String buttonMac, List<IEspDevice> delDevices)
    {
        IEspCommandEspButtonDeleteDevices command = new EspCommandEspButtonDeleteDevices();
        return command.doCommandEspButtonDeleteDevices(inetDevice, buttonMac, delDevices);
    }
    
}
