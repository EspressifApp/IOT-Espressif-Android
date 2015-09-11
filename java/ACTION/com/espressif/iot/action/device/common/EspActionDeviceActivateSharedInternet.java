package com.espressif.iot.action.device.common;

import com.espressif.iot.command.device.New.EspCommandDeviceNewActivateInternet;
import com.espressif.iot.command.device.New.IEspCommandDeviceNewActivateInternet;
import com.espressif.iot.device.IEspDevice;

public class EspActionDeviceActivateSharedInternet implements IEspActionDeviceActivateSharedInternet
{
    
    @Override
    public IEspDevice doActionDeviceActivateSharedInternet(long userId, String userKey, String sharedDeviceKey)
    {
        IEspCommandDeviceNewActivateInternet command = new EspCommandDeviceNewActivateInternet();
        IEspDevice device = command.doCommandNewActivateInternet(userId, userKey, sharedDeviceKey);
        return device;
    }
    
}
