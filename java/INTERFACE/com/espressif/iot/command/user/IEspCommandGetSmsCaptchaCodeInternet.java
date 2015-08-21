package com.espressif.iot.command.user;

import com.espressif.iot.command.IEspCommandInternet;
import com.espressif.iot.command.IEspCommandUser;

public interface IEspCommandGetSmsCaptchaCodeInternet extends IEspCommandUser, IEspCommandInternet
{
    public static String URL = "https://iot.espressif.cn/v1/resource/sms/";
    
    /**
     * Get sms captcha code from server
     * 
     * @param phoneNumber
     * @param resourceToken the token which support to get sms captcha
     * @param state the state of the captcha
     * @return
     */
    boolean doCommandGetSmsCaptchaCode(String phoneNumber, String resourceToken, String state);
}
