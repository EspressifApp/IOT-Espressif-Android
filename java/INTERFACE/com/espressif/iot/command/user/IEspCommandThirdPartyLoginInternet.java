package com.espressif.iot.command.user;

import com.espressif.iot.type.user.EspLoginResult;
import com.espressif.iot.type.user.EspThirdPartyLoginPlat;

public interface IEspCommandThirdPartyLoginInternet extends IEspCommandUserLogin
{
    /**
     * Third-party login
     * 
     * @param espPlat
     * @return @see EspLoginResult
     */
    EspLoginResult doCommandThirdPartLoginInternet(EspThirdPartyLoginPlat espPlat);
}
