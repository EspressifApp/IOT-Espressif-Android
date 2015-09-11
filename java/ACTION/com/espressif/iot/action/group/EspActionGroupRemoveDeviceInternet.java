package com.espressif.iot.action.group;

import com.espressif.iot.command.group.EspCommandGroupRemoveDeviceInternet;
import com.espressif.iot.command.group.IEspCommandGroupRemoveDeviceInternet;

public class EspActionGroupRemoveDeviceInternet implements IEspActionGroupRemoveDeviceInternet
{
    
    @Override
    public boolean doActionRemoveDevicefromGroupInternet(String userKey, long deviceId, long groupId)
    {
        IEspCommandGroupRemoveDeviceInternet command = new EspCommandGroupRemoveDeviceInternet();
        return command.doCommandRemoveDevicefromGroupInternet(userKey, deviceId, groupId);
    }
    
}
