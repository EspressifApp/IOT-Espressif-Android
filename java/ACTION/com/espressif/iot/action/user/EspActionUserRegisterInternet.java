package com.espressif.iot.action.user;

import org.apache.log4j.Logger;

import com.espressif.iot.command.user.EspCommandUserRegisterInternet;
import com.espressif.iot.command.user.IEspCommandUserRegisterInternet;
import com.espressif.iot.type.user.EspRegisterResult;

public class EspActionUserRegisterInternet implements IEspActionUserRegisterInternet
{
    
    private final static Logger log = Logger.getLogger(EspActionUserRegisterInternet.class);
    
    @Override
    public EspRegisterResult doActionUserRegisterInternet(String userName, String userEmail, String userPassword)
    {
        IEspCommandUserRegisterInternet command = new EspCommandUserRegisterInternet();
        EspRegisterResult result = command.doCommandUserRegisterInternet(userName, userEmail, userPassword);
        log.debug(Thread.currentThread().toString() + "##doActionUserRegisterInternet(userName=[" + userName
            + "],userEmail=[" + userEmail + "],userPassword=[" + userPassword + "]): " + result);
        return result;
    }
    
}
