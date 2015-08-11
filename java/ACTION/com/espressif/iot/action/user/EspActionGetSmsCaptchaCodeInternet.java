package com.espressif.iot.action.user;

import com.espressif.iot.base.application.EspApplication;
import com.espressif.iot.command.user.EspCommandGetSmsCaptchaCodeInternet;
import com.espressif.iot.command.user.IEspCommandGetSmsCaptchaCodeInternet;

public class EspActionGetSmsCaptchaCodeInternet implements IEspActionGetSmsCaptchaCodeInternet
{
    
    @Override
    public boolean doActionGetSmsCaptchaCode(String phoneNumber)
    {
        IEspCommandGetSmsCaptchaCodeInternet command = new EspCommandGetSmsCaptchaCodeInternet();
        String signatureMD5 = EspApplication.sharedInstance().getSignatureMD5();
        return command.doCommandGetSmsCaptchaCode(phoneNumber, signatureMD5);
    }
    
}
