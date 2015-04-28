package com.espressif.iot.action.user;

import com.espressif.iot.action.IEspActionDB;
import com.espressif.iot.action.IEspActionUser;
import com.espressif.iot.user.IEspUser;

public interface IEspActionUserLoginDB extends IEspActionUser, IEspActionDB
{
    /**
     * auto login according to local db
     * 
     * @return @see IEspUser
     */
    IEspUser doActionUserLoginDB();
}
