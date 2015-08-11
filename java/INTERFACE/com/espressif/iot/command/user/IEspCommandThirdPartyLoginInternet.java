package com.espressif.iot.command.user;

import com.espressif.iot.model.user.EspThirdPartyLoginPlat;
import com.espressif.iot.type.user.EspLoginResult;

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
