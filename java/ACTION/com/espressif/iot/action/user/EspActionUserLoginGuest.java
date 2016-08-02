package com.espressif.iot.action.user;

import org.apache.log4j.Logger;

import com.espressif.iot.command.user.EspCommandUserLoginGuest;
import com.espressif.iot.command.user.IEspCommandUserLoginGuest;
import com.espressif.iot.user.IEspUser;

public class EspActionUserLoginGuest implements IEspActionUserLoginGuest
{
    private final static Logger log = Logger.getLogger(EspActionUserLoginGuest.class);
    
    @Override
    public IEspUser doActionUserLoginGuest()
    {
        IEspCommandUserLoginGuest command = new EspCommandUserLoginGuest();
        IEspUser result = command.doCommandUserLoginGuest();
        log.debug(Thread.currentThread().toString() + "##doActionUserLoginDB(): " + result);
        return result;
    }
}
