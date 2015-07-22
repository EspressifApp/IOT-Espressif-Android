package com.espressif.iot.action.user;

import com.espressif.iot.command.user.EspCommandThirdPartyLoginInternet;
import com.espressif.iot.command.user.IEspCommandThirdPartyLoginInternet;
import com.espressif.iot.type.user.EspLoginResult;
import com.espressif.iot.user.IEspUser;
import com.espressif.iot.user.builder.BEspUser;

public class EspActionThirdPartyLoginInternet implements IEspActionThirdPartyLoginInternet
{
    
    @Override
    public EspLoginResult doActionThirdPartyLoginInternet(String state, String accessToken)
    {
        IEspCommandThirdPartyLoginInternet command = new EspCommandThirdPartyLoginInternet();
        EspLoginResult loginResult = command.doCommandThirdPartLoginInternet(state, accessToken);
        if (loginResult == EspLoginResult.SUC)
        {
            IEspUser user = BEspUser.getBuilder().getInstance();
            user.saveUserInfoInDB(false, true);
        }
        return loginResult;
    }
    
}
