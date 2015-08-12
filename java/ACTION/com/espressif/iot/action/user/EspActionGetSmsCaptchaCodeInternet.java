package com.espressif.iot.action.user;

import com.espressif.iot.command.user.EspCommandGetSmsCaptchaCodeInternet;
import com.espressif.iot.command.user.IEspCommandGetSmsCaptchaCodeInternet;

public class EspActionGetSmsCaptchaCodeInternet implements IEspActionGetSmsCaptchaCodeInternet
{
    
    @Override
    public boolean doActionGetSmsCaptchaCode(String phoneNumber)
    {
        IEspCommandGetSmsCaptchaCodeInternet command = new EspCommandGetSmsCaptchaCodeInternet();
        String resourceKey = getResourceKey();
        return command.doCommandGetSmsCaptchaCode(phoneNumber, resourceKey);
    }
    
    private String getResourceKey()
    {
        return "9c3190edca91c67ad0d18123bdc2761fdb5ec0da";
    }
}
