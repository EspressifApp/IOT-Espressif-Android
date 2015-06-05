package com.espressif.iot.command.device.common;

import java.util.List;

import com.espressif.iot.command.IEspCommandInternet;
import com.espressif.iot.command.device.IEspCommandActivated;
import com.espressif.iot.device.IEspDevice;

public interface IEspCommandDeviceSynchronizeInternet extends IEspCommandActivated, IEspCommandInternet
{
    static final String URL = "https://iot.espressif.cn/v1/user/devices/?list_by_group=true&query_devices_mesh=true";
    static final String Last_Active = "last_active";
    static final String Devices = "devices";
    static final String Is_Owner_Key = "is_owner_key";
    static final String Device_Groups = "deviceGroups";
    /**
     * synchronize the user's device from the Server
     * @return the device list of the user
     */
    List<IEspDevice> doCommandDeviceSynchronizeInternet(String userKey);
}
