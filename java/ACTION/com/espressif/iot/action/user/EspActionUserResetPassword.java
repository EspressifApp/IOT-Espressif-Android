package com.espressif.iot.action.user;

import com.espressif.iot.command.user.EspCommandUserResetPassword;
import com.espressif.iot.command.user.IEspCommandUserResetPassword;
import com.espressif.iot.type.user.EspResetPasswordResult;

public class EspActionUserResetPassword implements IEspActionUserResetPassword
{

    @Override
    public EspResetPasswordResult doActionResetPassword(String email)
    {
        IEspCommandUserResetPassword command = new EspCommandUserResetPassword();
        return command.doCommandResetPassword(email);
    }

}
