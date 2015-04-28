package com.espressif.iot.command.device.common;

import com.espressif.iot.base.api.EspBaseApiUtil;

public class EspCommandDeviceSleepRebootLocal implements IEspCommandDeviceSleepRebootLocal
{
    
    @Override
    public void doCommandDeviceSleepLocal()
    {
        EspBaseApiUtil.Post(URL_SLEEP, null);
    }
    
    @Override
    public void doCommandDeviceRebootLocal()
    {
        EspBaseApiUtil.Post(URL_REBOOT, null);
    }
    
}
