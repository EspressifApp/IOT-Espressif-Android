package com.espressif.iot.command.user;

import org.apache.log4j.Logger;

import com.espressif.iot.user.IEspUser;
import com.espressif.iot.user.builder.BEspUser;

public class EspCommandUserLoginGuest implements IEspCommandUserLoginGuest
{
    private final static Logger log = Logger.getLogger(EspCommandUserLoginGuest.class);
    
    @Override
    public IEspUser doCommandUserLoginGuest()
    {
        IEspUser result = BEspUser.getBuilder().loadUser();
        log.debug(Thread.currentThread().toString() + "##doCommandUserLoginGuest(): " + result);
        return result;
    }
}
