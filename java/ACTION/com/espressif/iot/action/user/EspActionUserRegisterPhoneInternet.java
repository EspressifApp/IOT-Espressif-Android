package com.espressif.iot.action.user;

import com.espressif.iot.action.IEspActionUserRegisterPhoneInternet;
import com.espressif.iot.command.user.EspCommandUserRegisterPhoneInternet;
import com.espressif.iot.command.user.IEspCommandUserRegisterPhoneInternet;
import com.espressif.iot.type.user.EspRegisterResult;

public class EspActionUserRegisterPhoneInternet implements IEspActionUserRegisterPhoneInternet
{

    @Override
    public EspRegisterResult doActionUserRegisterPhone(String phoneNumber, String captchaCode, String userPassword)
    {
        IEspCommandUserRegisterPhoneInternet command = new EspCommandUserRegisterPhoneInternet();
        
        return command.doCommandUserRegisterPhone(phoneNumber, captchaCode, userPassword);
    }
    
}
