package com.espressif.iot.action.device.common.timer;

import org.json.JSONObject;

import com.espressif.iot.command.device.common.EspCommandDeviceTimerInternet;
import com.espressif.iot.command.device.common.IEspCommandDeviceTimerInternet;
import com.espressif.iot.device.IEspDevice;

public class EspActionDeviceTimerPostInternet implements IEspActionDeviceTimerPostInternet
{
    
    @Override
    public boolean doActionDeviceTimerPostInternet(IEspDevice device, JSONObject timerJSON)
    {
        IEspCommandDeviceTimerInternet command = new EspCommandDeviceTimerInternet(device);
        return command.doCommandDeviceTimerPost(timerJSON);
    }
    
}
