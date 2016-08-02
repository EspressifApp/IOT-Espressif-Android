package com.espressif.iot.action.user;

import com.espressif.iot.action.IEspActionDB;
import com.espressif.iot.action.IEspActionUser;
import com.espressif.iot.user.IEspUser;

public interface IEspActionUserLoginGuest extends IEspActionUser, IEspActionDB
{
    /**
     * auto login according to local db by guest
     * 
     * @return @see IEspUser
     */
    IEspUser doActionUserLoginGuest();
}
