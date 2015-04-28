package com.espressif.iot.action.user;

import com.espressif.iot.action.IEspActionInternet;
import com.espressif.iot.action.IEspActionUser;
import com.espressif.iot.type.user.EspRegisterResult;

public interface IEspActionUserRegisterInternet extends IEspActionUser, IEspActionInternet
{
    /**
     * register user account by Internet
     * 
     * @param userName user's name
     * @param userEmail user's email
     * @param userPassword user's password
     * @return @see EspRegisterResult
     */
    EspRegisterResult doActionUserRegisterInternet(String userName, String userEmail, String userPassword);
}
