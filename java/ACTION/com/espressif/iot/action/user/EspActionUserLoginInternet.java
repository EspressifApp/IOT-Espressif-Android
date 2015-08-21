package com.espressif.iot.action.user;

import org.apache.log4j.Logger;

import com.espressif.iot.command.user.EspCommandUserLoginInternet;
import com.espressif.iot.command.user.IEspCommandUserLoginInternet;
import com.espressif.iot.type.user.EspLoginResult;
import com.espressif.iot.user.IEspUser;
import com.espressif.iot.user.builder.BEspUser;

public class EspActionUserLoginInternet implements IEspActionUserLoginInternet
{
    private final static Logger log = Logger.getLogger(EspActionUserLoginInternet.class);
    
    @Override
    public EspLoginResult doActionUserLoginInternet(String userEmail, String userPassword)
    {
        IEspCommandUserLoginInternet command = new EspCommandUserLoginInternet();
        EspLoginResult loginResult = command.doCommandUserLoginInternet(userEmail, userPassword);
        if (loginResult == EspLoginResult.SUC)
        {
            IEspUser user = BEspUser.getBuilder().getInstance();
            user.saveUserInfoInDB();
        }
        log.debug(Thread.currentThread().toString() + "##doActionUserLoginInternet(userEmail=[" + userEmail
            + "],userPassword=[" + userPassword + "]): " + loginResult);
        return loginResult;
    }
    
}
