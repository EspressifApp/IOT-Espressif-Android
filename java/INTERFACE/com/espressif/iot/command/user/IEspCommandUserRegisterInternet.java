package com.espressif.iot.command.user;

import com.espressif.iot.command.IEspCommandInternet;
import com.espressif.iot.command.IEspCommandUser;
import com.espressif.iot.type.user.EspRegisterResult;

public interface IEspCommandUserRegisterInternet extends IEspCommandUser, IEspCommandInternet
{
    final String URL = "https://iot.espressif.cn/v1/user/join/";
    
    /**
     * Register a new account
     * 
     * @param userName the account's user name
     * @param userEmail the account's user email
     * @param userPassword the account's user password, at least 6 words
     * @return the register result @see EspRegisterResult
     */
    EspRegisterResult doCommandUserRegisterInternet(String userName, String userEmail, String userPassword);
}
