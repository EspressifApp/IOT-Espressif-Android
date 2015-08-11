package com.espressif.iot.command.user;

import com.espressif.iot.type.user.EspLoginResult;

public interface IEspCommandUserLoginPhoneInternet extends IEspCommandUserLogin
{
    /**
     * Login with phone number
     * 
     * @param phoneNumber
     * @param userPassword
     * @return
     */
    EspLoginResult doCommandUserLoginPhone(String phoneNumber, String userPassword);
}
