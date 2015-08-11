package com.espressif.iot.action.user;

import com.espressif.iot.action.IEspActionInternet;
import com.espressif.iot.action.IEspActionUser;
import com.espressif.iot.type.user.EspLoginResult;

public interface IEspActionUserLoginPhoneInternet extends IEspActionUser, IEspActionInternet
{
    /**
     * Login with phone number
     * 
     * @param phoneNumber
     * @param password
     * @return
     */
    EspLoginResult doActionUserLoginPhone(String phoneNumber, String password);
}
