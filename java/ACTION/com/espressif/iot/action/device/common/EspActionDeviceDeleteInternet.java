package com.espressif.iot.action.device.common;

import com.espressif.iot.command.device.common.EspCommandDeviceDeleteInternet;
import com.espressif.iot.command.device.common.IEspCommandDeviceDeleteInternet;

public class EspActionDeviceDeleteInternet implements IEspActionDeviceDeleteInternet
{
    
    @Override
    public boolean doActionDeviceDeleteInternet(String deviceKey)
    {
        IEspCommandDeviceDeleteInternet command = new EspCommandDeviceDeleteInternet();
        return command.doCommandDeviceDeleteInternet(deviceKey);
    }
    
}
