package com.espressif.iot.command.user;

import com.espressif.iot.command.IEspCommandInternet;
import com.espressif.iot.command.IEspCommandUser;

public interface IEspCommandFindAccountInternet extends IEspCommandUser, IEspCommandInternet
{
    final String URL = "https://iot.espressif.cn/v1/user/find/";
    
    final int RESULT_FOUND = 200;
    
    final int RESULT_NOT_FOUND = 404;
    
    /**
     * check whether the user name is available
     * 
     * @param userName user name
     * @return whether the user name is available
     */
    boolean doCommandFindUsernametInternet(String userName);
    
    /**
     * check whether the email is available
     * 
     * @param email user's email
     * @return whether the email is available
     */
    boolean doCommandFindEmailInternet(String email);
}
