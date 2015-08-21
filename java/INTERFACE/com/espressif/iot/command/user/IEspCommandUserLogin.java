package com.espressif.iot.command.user;

import com.espressif.iot.command.IEspCommandInternet;
import com.espressif.iot.command.IEspCommandUser;

public interface IEspCommandUserLogin extends IEspCommandUser, IEspCommandInternet
{
    public static final String URL = "https://iot.espressif.cn/v1/user/login/";
    
    public static final String KEY_ACCESS_TOKEN = KEY_CODE;
    
    public static final String KEY_OPEN_ID = "openid";
}
