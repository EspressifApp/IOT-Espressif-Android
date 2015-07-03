package com.espressif.iot.command.user;

import com.espressif.iot.command.IEspCommandInternet;
import com.espressif.iot.command.IEspCommandUser;

public interface IEspCommandFindAccountInternet extends IEspCommandUser, IEspCommandInternet
{
    static final String URL = "https://iot.espressif.cn/v1/user/find/";
    
    static final int RESULT_FOUND = 200;
    static final int RESULT_NOT_FOUND = 404;
    
    boolean doCommandFindUsernametInternet(String userName);
    
    boolean doCommandFindEmailInternet(String email);
}
