package com.espressif.iot.action.group;

import com.espressif.iot.command.group.EspCommandGroupRenameInternet;
import com.espressif.iot.command.group.IEspCommandGroupRenameInternet;

public class EspActionGroupRenameInternet implements IEspActionGroupRenameInternet
{
    
    @Override
    public boolean doActionRenameGroupInternet(String userKey, long groupId, String newName)
    {
        IEspCommandGroupRenameInternet command = new EspCommandGroupRenameInternet();
        return command.doCommandRenameGroupInternet(userKey, groupId, newName);
    }
    
}
