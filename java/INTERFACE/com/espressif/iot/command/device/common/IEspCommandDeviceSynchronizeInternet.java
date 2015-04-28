package com.espressif.iot.command.device.common;

import java.util.List;

import com.espressif.iot.command.IEspCommandInternet;
import com.espressif.iot.command.device.IEspCommandActivated;
import com.espressif.iot.device.IEspDevice;

public interface IEspCommandDeviceSynchronizeInternet extends IEspCommandActivated, IEspCommandInternet
{
    final String URL = "https://iot.espressif.cn/v1/user/devices/?list_by_group=true&query_devices_mesh=true";
    /**
     * synchronize the user's device from the Server
     * @return the device list of the user
     */
    List<IEspDevice> doCommandDeviceSynchronizeInternet(String userKey);
}
