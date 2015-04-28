package com.espressif.iot.command.user;

import org.apache.log4j.Logger;

import com.espressif.iot.user.IEspUser;
import com.espressif.iot.user.builder.BEspUser;

public class EspCommandUserLoginDB implements IEspCommandUserLoginDB
{
    private final static Logger log = Logger.getLogger(EspCommandUserLoginDB.class);
    
    @Override
    public IEspUser doCommandUserLoginDB()
    {
        IEspUser result = BEspUser.getBuilder().loadUser();
        log.debug(Thread.currentThread().toString() + "##doCommandUserLoginDB(): " + result);
        return result;
    }
    
}
