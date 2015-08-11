package com.espressif.iot.action.user;

import com.espressif.iot.command.user.EspCommandUserLoginPhoneInternet;
import com.espressif.iot.command.user.IEspCommandUserLoginPhoneInternet;
import com.espressif.iot.type.user.EspLoginResult;
import com.espressif.iot.user.IEspUser;
import com.espressif.iot.user.builder.BEspUser;

public class EspActionUserLoginPhoneInternet implements IEspActionUserLoginPhoneInternet
{
    
    @Override
    public EspLoginResult doActionUserLoginPhone(String phoneNumber, String password)
    {
        IEspCommandUserLoginPhoneInternet command = new EspCommandUserLoginPhoneInternet();
        EspLoginResult result = command.doCommandUserLoginPhone(phoneNumber, password);
        if (result == EspLoginResult.SUC)
        {
            IEspUser user = BEspUser.getBuilder().getInstance();
            user.saveUserInfoInDB();
        }
        return result;
    }
    
}
