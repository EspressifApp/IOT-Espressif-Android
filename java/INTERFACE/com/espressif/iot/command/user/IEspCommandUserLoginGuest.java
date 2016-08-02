package com.espressif.iot.command.user;

import com.espressif.iot.user.IEspUser;

public interface IEspCommandUserLoginGuest extends IEspCommandUserLogin
{
    /**
     * login by guest
     * 
     * @return the user of guest
     */
    IEspUser doCommandUserLoginGuest();
}
