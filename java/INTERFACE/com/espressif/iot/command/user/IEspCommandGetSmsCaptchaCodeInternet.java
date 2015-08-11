package com.espressif.iot.command.user;

import com.espressif.iot.command.IEspCommandInternet;
import com.espressif.iot.command.IEspCommandUser;

public interface IEspCommandGetSmsCaptchaCodeInternet extends IEspCommandUser, IEspCommandInternet
{
    public static String URL = "https://iot.espressif.cn/v1/resources/sms/?phone=";
    
    /**
     * Get sms captcha code from server
     * 
     * @param phoneNumber
     * @param codeToken the token which support to get sms captcha
     * @return
     */
    boolean doCommandGetSmsCaptchaCode(String phoneNumber, String codeToken);
}
