package com.espressif.iot.action.device.configure;

import com.espressif.iot.command.device.New.EspCommandDeviceNewActivateInternet;
import com.espressif.iot.command.device.New.IEspCommandDeviceNewActivateInternet;
import com.espressif.iot.device.IEspDevice;

public class EspActionDeviceConfigureActivateInternet implements IEspActionDeviceConfigureActivateInternet
{
    
    @Override
    public IEspDevice doActionDeviceConfigureActivateInternet(long userId, String userKey, String randomToken)
    {
        // for the historical reason, we use EspCommandDeviceNewActivateInternet to do action belong to
        // the configure device
        IEspCommandDeviceNewActivateInternet command = new EspCommandDeviceNewActivateInternet();
        return command.doCommandNewActivateInternet(userId, userKey, randomToken);
    }
    
}
