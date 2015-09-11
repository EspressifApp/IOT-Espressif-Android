package com.espressif.iot.type.user;

import org.apache.http.HttpStatus;

public enum EspRegisterResult
{
    SUC, USER_OR_EMAIL_EXIST_ALREADY, CONTENT_FORMAT_ERROR, NETWORK_UNACCESSIBLE;
    
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
            return CONTENT_FORMAT_ERROR;
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
