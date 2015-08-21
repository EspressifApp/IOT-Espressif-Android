package com.espressif.iot.action.user;

import com.espressif.iot.command.user.EspCommandGetSmsCaptchaCodeInternet;
import com.espressif.iot.command.user.IEspCommandGetSmsCaptchaCodeInternet;

public class EspActionGetSmsCaptchaCodeInternet implements IEspActionGetSmsCaptchaCodeInternet
{
    
    @Override
    public boolean doActionGetSmsCaptchaCode(String phoneNumber, String state)
    {
        IEspCommandGetSmsCaptchaCodeInternet command = new EspCommandGetSmsCaptchaCodeInternet();
        String resourceKey = getResourceKey();
        return command.doCommandGetSmsCaptchaCode(phoneNumber, resourceKey, state);
    }
    
    private String getResourceKey()
    {
        return "null";
    }
}
