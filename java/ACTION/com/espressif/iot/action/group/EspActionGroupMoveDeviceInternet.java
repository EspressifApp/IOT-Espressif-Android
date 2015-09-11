package com.espressif.iot.action.group;

import com.espressif.iot.command.group.EspCommandGroupMoveDeviceInternet;
import com.espressif.iot.command.group.IEspCommandGroupMoveDeviceInternet;

public class EspActionGroupMoveDeviceInternet implements IEspActionGroupMoveDeviceInternet
{
    
    @Override
    public boolean doActionMoveDeviceIntoGroupInternet(String userKey, long deviceId, long groupId, boolean reservePreGroup)
    {
        IEspCommandGroupMoveDeviceInternet command = new EspCommandGroupMoveDeviceInternet();
        return command.doCommandMoveDeviceIntoGroupInternet(userKey, deviceId, groupId, reservePreGroup);
    }
    
}
