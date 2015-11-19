package com.espressif.iot.type.user;

import java.net.HttpURLConnection;

public enum EspResetPasswordResult
{
    SUC, FAILED, EMAIL_NOT_EXIST;
    
    public static EspResetPasswordResult getResetPasswordResult(int status)
    {
        switch (status)
        {
            case HttpURLConnection.HTTP_OK:
                return SUC;
            case HttpURLConnection.HTTP_NOT_FOUND:
                return EMAIL_NOT_EXIST;
            default:
                return FAILED;
        }
    }
}
