package com.espressif.iot.command.user;

import com.espressif.iot.command.IEspCommandInternet;
import com.espressif.iot.command.IEspCommandUser;
import com.espressif.iot.type.user.EspResetPasswordResult;

public interface IEspCommandUserResetPassword extends IEspCommandUser, IEspCommandInternet
{
    public static final String URL = "https://iot.espressif.cn/v1/user/resetpassword/mail/";
    
    /**
     * Reset password with email
     * 
     * @param email
     * @return
     */
    public EspResetPasswordResult doCommandResetPassword(String email);
}
