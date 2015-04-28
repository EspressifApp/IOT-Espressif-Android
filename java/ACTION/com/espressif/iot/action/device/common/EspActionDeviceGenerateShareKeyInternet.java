package com.espressif.iot.action.device.common;

import com.espressif.iot.command.device.common.EspCommandDeviceGenerateShareKeyInternet;
import com.espressif.iot.command.device.common.IEspCommandDeviceGenerateShareKeyInternet;

public class EspActionDeviceGenerateShareKeyInternet implements IEspActionDeviceGenerateShareKeyInternet
{
    
    @Override
    public String doActionDeviceGenerateShareKeyInternet(String ownerDeviceKey)
    {
        IEspCommandDeviceGenerateShareKeyInternet command = new EspCommandDeviceGenerateShareKeyInternet();
        String shareKey = command.doCommandDeviceGenerateShareKey(ownerDeviceKey);
        return shareKey;
    }
    
}
