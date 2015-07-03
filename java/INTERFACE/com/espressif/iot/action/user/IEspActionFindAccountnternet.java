package com.espressif.iot.action.user;

import com.espressif.iot.action.IEspActionInternet;
import com.espressif.iot.action.IEspActionUser;

public interface IEspActionFindAccountnternet extends IEspActionUser, IEspActionInternet
{
    boolean doActionFindUsernametInternet(String userName);
    
    boolean doActionFindEmailInternet(String email);
}
