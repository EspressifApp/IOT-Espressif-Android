package com.espressif.iot.command.user;

import com.espressif.iot.type.user.EspRegisterResult;

public interface IEspCommandUserRegisterPhoneInternet extends IEspCommandUserRegister
{
    /**
     * Register user account with phone number
     * 
     * @param phoneNumber
     * @param captchaCode
     * @param userPassword
     * @return
     */
    EspRegisterResult doCommandUserRegisterPhone(String phoneNumber, String captchaCode, String userPassword);
}
