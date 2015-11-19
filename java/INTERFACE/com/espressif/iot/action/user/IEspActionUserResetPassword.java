package com.espressif.iot.action.user;

import com.espressif.iot.action.IEspActionInternet;
import com.espressif.iot.action.IEspActionUser;
import com.espressif.iot.type.user.EspResetPasswordResult;

public interface IEspActionUserResetPassword extends IEspActionUser, IEspActionInternet
{
    /**
     * Reset password with email
     * 
     * @param email
     * @return
     */
    public EspResetPasswordResult doActionResetPassword(String email);
}
