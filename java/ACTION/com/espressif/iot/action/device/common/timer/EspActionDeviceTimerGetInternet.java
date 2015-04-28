package com.espressif.iot.action.device.common.timer;

import com.espressif.iot.command.device.common.EspCommandDeviceTimerInternet;
import com.espressif.iot.command.device.common.IEspCommandDeviceTimerInternet;
import com.espressif.iot.device.IEspDevice;

public class EspActionDeviceTimerGetInternet implements IEspActionDeviceTimerGetInternet
{
    
    @Override
    public boolean doActionDeviceTimerGet(IEspDevice device)
    {
        IEspCommandDeviceTimerInternet command = new EspCommandDeviceTimerInternet(device);
        return command.doCommandDeviceTimerGet();
    }
    
}
