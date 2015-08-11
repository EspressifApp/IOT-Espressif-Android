package com.espressif.iot.action.user;

import com.espressif.iot.action.IEspActionInternet;
import com.espressif.iot.action.IEspActionUser;

public interface IEspActionGetSmsCaptchaCodeInternet extends IEspActionUser, IEspActionInternet
{
    /**
     * Get sms captcha code from server
     * 
     * @param phoneNumber
     * @return
     */
    boolean doActionGetSmsCaptchaCode(String phoneNumber);
}
