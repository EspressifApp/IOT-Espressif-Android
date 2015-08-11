package com.espressif.iot.type.user;

import org.apache.http.HttpStatus;

public enum EspLoginResult
{
    SUC, PASSWORD_ERR, NOT_REGISTER, NETWORK_UNACCESSIBLE, FAILED;
    
    public static EspLoginResult getEspLoginResult(int status)
    {
        if (status == HttpStatus.SC_OK)
        {
            return SUC;
        }
        else if (status == HttpStatus.SC_FORBIDDEN)
        {
            return PASSWORD_ERR;
        }
        else if (status == HttpStatus.SC_NOT_FOUND)
        {
            return NOT_REGISTER;
        }
        else if (status == -HttpStatus.SC_OK)
        {
            return NETWORK_UNACCESSIBLE;
        }
        else
        {
            return FAILED;
        }
    }
}
