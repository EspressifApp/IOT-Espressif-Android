package com.espressif.iot.command.user;

import com.espressif.iot.command.IEspCommandInternet;
import com.espressif.iot.command.IEspCommandUser;

public interface IEspCommandUserRegister  extends IEspCommandUser, IEspCommandInternet
{
    static final String URL = "https://iot.espressif.cn/v1/user/join/";
}
