package com.espressif.iot.command.user;

import com.espressif.iot.command.IEspCommandDB;
import com.espressif.iot.command.IEspCommandUser;
import com.espressif.iot.user.IEspUser;

public interface IEspCommandUserLoginDB extends IEspCommandUser, IEspCommandDB
{
    /**
     * login automatically by local db (if the user set auto login last time, it will suc)
     * 
     * @return the user
     */
    IEspUser doCommandUserLoginDB();
}
