package com.espressif.iot.action.device.common.timer;

import com.espressif.iot.command.device.common.EspCommandDeviceTimerInternet;
import com.espressif.iot.command.device.common.IEspCommandDeviceTimerInternet;
import com.espressif.iot.device.IEspDevice;

public class EspActionDeviceTimerDeleteInternet implements IEspActionDeviceTimerDeleteInternet
{
    
    @Override
    public boolean doActionDeviceTimerDeleteInternet(IEspDevice device, long timerId)
    {
        IEspCommandDeviceTimerInternet command = new EspCommandDeviceTimerInternet(device);
        return command.doCommandDeviceTimerDelete(timerId);
    }
    
}
