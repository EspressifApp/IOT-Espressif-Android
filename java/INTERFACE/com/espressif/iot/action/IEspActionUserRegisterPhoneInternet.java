package com.espressif.iot.action;

import com.espressif.iot.type.user.EspRegisterResult;

public interface IEspActionUserRegisterPhoneInternet extends IEspActionUser, IEspActionInternet
{
    /**
     * Register user account with phone number
     * 
     * @param phoneNumber
     * @param captchaCode
     * @param userPassword
     * @return
     */
    EspRegisterResult doActionUserRegisterPhone(String phoneNumber, String captchaCode, String userPassword);
}
