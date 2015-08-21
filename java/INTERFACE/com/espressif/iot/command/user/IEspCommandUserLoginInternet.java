package com.espressif.iot.command.user;

import com.espressif.iot.type.user.EspLoginResult;

public interface IEspCommandUserLoginInternet extends IEspCommandUserLogin
{
    /**
     * Login in online
     * 
     * @param userEmail
     * @param userPassword
     * @return the login result @see EspLoginResult
     */
    EspLoginResult doCommandUserLoginInternet(String userEmail, String userPassword);
}
