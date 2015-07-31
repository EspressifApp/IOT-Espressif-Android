package com.espressif.iot.command.user;

import com.espressif.iot.command.IEspCommandInternet;
import com.espressif.iot.command.IEspCommandUser;
import com.espressif.iot.type.user.EspLoginResult;

public interface IEspCommandUserLoginInternet extends IEspCommandUser, IEspCommandInternet
{
    static final String URL = "https://iot.espressif.cn/v1/user/login/";
    
    /**
     * Login in online
     * 
     * @param userEmail
     * @param userPassword
     * @return the login result @see EspLoginResult
     */
    EspLoginResult doCommandUserLoginInternet(String userEmail, String userPassword);
}
