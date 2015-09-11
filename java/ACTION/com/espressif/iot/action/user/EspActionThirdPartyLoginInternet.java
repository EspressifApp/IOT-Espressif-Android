package com.espressif.iot.action.user;

import com.espressif.iot.command.user.EspCommandThirdPartyLoginInternet;
import com.espressif.iot.command.user.IEspCommandThirdPartyLoginInternet;
import com.espressif.iot.type.user.EspLoginResult;
import com.espressif.iot.type.user.EspThirdPartyLoginPlat;
import com.espressif.iot.user.IEspUser;
import com.espressif.iot.user.builder.BEspUser;

public class EspActionThirdPartyLoginInternet implements IEspActionThirdPartyLoginInternet
{
    
    @Override
    public EspLoginResult doActionThirdPartyLoginInternet(EspThirdPartyLoginPlat espPlat)
    {
        IEspCommandThirdPartyLoginInternet command = new EspCommandThirdPartyLoginInternet();
        EspLoginResult loginResult = command.doCommandThirdPartLoginInternet(espPlat);
        if (loginResult == EspLoginResult.SUC)
        {
            IEspUser user = BEspUser.getBuilder().getInstance();
            user.saveUserInfoInDB();
        }
        return loginResult;
    }
    
}
