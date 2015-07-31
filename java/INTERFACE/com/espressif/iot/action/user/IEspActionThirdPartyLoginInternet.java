package com.espressif.iot.action.user;

import com.espressif.iot.action.IEspActionInternet;
import com.espressif.iot.action.IEspActionUser;
import com.espressif.iot.model.user.EspThirdPartyLoginPlat;
import com.espressif.iot.type.user.EspLoginResult;

public interface IEspActionThirdPartyLoginInternet extends IEspActionUser, IEspActionInternet
{
    /**
     * Third-party login
     * 
     * @param espPlat
     * @return @see EspLoginResult
     */
    EspLoginResult doActionThirdPartyLoginInternet(EspThirdPartyLoginPlat espPlat);
}
