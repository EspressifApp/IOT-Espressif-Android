package com.espressif.iot.command.user;

import com.espressif.iot.command.IEspCommandInternet;
import com.espressif.iot.command.IEspCommandUser;
import com.espressif.iot.model.user.EspThirdPartyLoginPlat;
import com.espressif.iot.type.user.EspLoginResult;

public interface IEspCommandThirdPartyLoginInternet extends IEspCommandUser, IEspCommandInternet
{
    public static final String URL = "https://iot.espressif.cn/v1/user/login/";
    
    public static final String KEY_STATE = "state";
    
    public static final String KEY_ACCESS_TOKEN = "code";
    
    public static final String KEY_OPEN_ID = "openid";
    
    /**
     * Third-party login
     * 
     * @param espPlat
     * @return @see EspLoginResult
     */
    EspLoginResult doCommandThirdPartLoginInternet(EspThirdPartyLoginPlat espPlat);
}
