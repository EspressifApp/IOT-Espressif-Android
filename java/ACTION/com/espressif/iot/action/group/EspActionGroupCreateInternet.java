package com.espressif.iot.action.group;

import com.espressif.iot.command.group.EspCommandGroupCreateInternet;
import com.espressif.iot.command.group.IEspCommandGroupCreateInternet;

public class EspActionGroupCreateInternet implements IEspActionGroupCreateInternet
{
    
    @Override
    public long doActionCreateGroupInternet(String userKey, String groupName)
    {
        IEspCommandGroupCreateInternet command = new EspCommandGroupCreateInternet();
        return command.doCommandCreateGroupInternet(userKey, groupName);
    }
    
}
