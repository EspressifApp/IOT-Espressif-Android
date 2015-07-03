package com.espressif.iot.action.user;

import com.espressif.iot.command.user.EspCommandFindAccountInternet;
import com.espressif.iot.command.user.IEspCommandFindAccountInternet;

public class EspActionFindAccountInternet implements IEspActionFindAccountnternet
{

    @Override
    public boolean doActionFindUsernametInternet(String userName)
    {
        IEspCommandFindAccountInternet command = new EspCommandFindAccountInternet();
        return command.doCommandFindUsernametInternet(userName);
    }

    @Override
    public boolean doActionFindEmailInternet(String email)
    {
        IEspCommandFindAccountInternet command = new EspCommandFindAccountInternet();
        return command.doCommandFindEmailInternet(email);
    }
    
}
