package com.espressif.iot.action.device.common;

import com.espressif.iot.command.device.common.EspCommandDeviceRenameInternet;
import com.espressif.iot.command.device.common.IEspCommandDeviceRenameInternet;

public class EspActionDeviceRenameInternet implements IEspActionDeviceRenameInternet
{
    
    @Override
    public boolean doActionDeviceRenameInternet(String deviceKey, String deviceName)
    {
        IEspCommandDeviceRenameInternet command = new EspCommandDeviceRenameInternet();
        return command.doCommandDeviceRenameInternet(deviceKey, deviceName);
    }
    
}
