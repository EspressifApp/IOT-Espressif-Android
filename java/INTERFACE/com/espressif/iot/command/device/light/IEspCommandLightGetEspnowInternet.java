package com.espressif.iot.command.device.light;

import java.util.List;

import com.espressif.iot.command.IEspCommandInternet;
import com.espressif.iot.command.device.IEspCommandLight;
import com.espressif.iot.type.device.status.IEspStatusEspnow;

public interface IEspCommandLightGetEspnowInternet extends IEspCommandInternet, IEspCommandLight
{
    static final String URL = "https://iot.espressif.cn/v1/device/rpc/?deliver_to_device=true&action=get_switches";
    
    List<IEspStatusEspnow> doCommandLightGetEspnowInternet(String deviceKey);
}
