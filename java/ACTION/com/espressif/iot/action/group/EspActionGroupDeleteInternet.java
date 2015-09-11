package com.espressif.iot.action.group;

import com.espressif.iot.command.group.EspCommandGroupDeleteInternet;
import com.espressif.iot.command.group.IEspCommandGroupDeleteInternet;

public class EspActionGroupDeleteInternet implements IEspActionGroupDeleteInternet
{
    
    @Override
    public boolean doActionDeleteGroupInternet(String userKey, long groupId)
    {
        IEspCommandGroupDeleteInternet command = new EspCommandGroupDeleteInternet();
        return command.doCommandDeleteGroupInternet(userKey, groupId);
    }
    
}
