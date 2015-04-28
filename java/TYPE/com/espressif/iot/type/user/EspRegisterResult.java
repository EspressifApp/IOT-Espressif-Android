package com.espressif.iot.type.user;

import org.apache.http.HttpStatus;

public enum EspRegisterResult
{
    SUC, USER_OR_EMAIL_EXIST_ALREADY, USER_OR_EMAIL_ERR_FORMAT, NETWORK_UNACCESSIBLE;
    public static EspRegisterResult getEspLoginResult(int status)
    {
        if (status == HttpStatus.SC_OK)
        {
            return SUC;
        }
        else if (status == HttpStatus.SC_CONFLICT)
        {
            return USER_OR_EMAIL_EXIST_ALREADY;
        }
        else if (status == HttpStatus.SC_BAD_REQUEST)
        {
            return USER_OR_EMAIL_ERR_FORMAT;
        }
        else if (status == -HttpStatus.SC_OK)
        {
            return NETWORK_UNACCESSIBLE;
        }
        else
        {
            return null;
        }
    }
}
