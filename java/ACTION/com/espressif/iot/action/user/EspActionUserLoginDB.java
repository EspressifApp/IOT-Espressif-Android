package com.espressif.iot.action.user;

import org.apache.log4j.Logger;

import com.espressif.iot.command.user.EspCommandUserLoginDB;
import com.espressif.iot.command.user.IEspCommandUserLoginDB;
import com.espressif.iot.user.IEspUser;

public class EspActionUserLoginDB implements IEspActionUserLoginDB
{
    private final static Logger log = Logger.getLogger(EspActionUserLoginDB.class);
    
    @Override
    public IEspUser doActionUserLoginDB()
    {
        IEspCommandUserLoginDB command = new EspCommandUserLoginDB();
        IEspUser result = command.doCommandUserLoginDB();
        log.debug(Thread.currentThread().toString() + "##doActionUserLoginDB(): " + result);
        return result;
    }
    
}
