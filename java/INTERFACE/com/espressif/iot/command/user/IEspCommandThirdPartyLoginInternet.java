package com.espressif.iot.command.user;

import com.espressif.iot.command.IEspCommandInternet;
import com.espressif.iot.command.IEspCommandUser;
import com.espressif.iot.type.user.EspLoginResult;

public interface IEspCommandThirdPartyLoginInternet extends IEspCommandUser, IEspCommandInternet
{
    public static final String URL = "https://iot.espressif.cn/v1/keys/";
    
    public static final String KEY_STATE = "state";
    
    public static final String KEY_ACCESS_TOKEN = "code";
    
    /**
     * Third-party login
     * 
     * @param state
     * @param token
     * @return @see EspLoginResult
     */
    EspLoginResult doCommandThirdPartLoginInternet(String state, String accessToken);
}
