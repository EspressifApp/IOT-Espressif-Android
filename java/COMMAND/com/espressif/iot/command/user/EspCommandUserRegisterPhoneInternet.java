package com.espressif.iot.command.user;

import org.json.JSONException;
import org.json.JSONObject;

import com.espressif.iot.base.api.EspBaseApiUtil;
import com.espressif.iot.type.user.EspRegisterResult;

public class EspCommandUserRegisterPhoneInternet implements IEspCommandUserRegisterPhoneInternet
{

    @Override
    public EspRegisterResult doCommandUserRegisterPhone(String phoneNumber, String captchaCode, String userPassword)
    {
        JSONObject json = new JSONObject();
        try
        {
            json.put(KEY_STATE, "phone_password");
            json.put(User_Phone, phoneNumber);
            json.put(User_Password, userPassword);
            json.put(KEY_CODE, captchaCode);
            
            JSONObject jsonResult = EspBaseApiUtil.Post(URL, json);
            if (jsonResult == null)
            {
                return EspRegisterResult.NETWORK_UNACCESSIBLE;
            }
            
            int status = jsonResult.getInt(Status);
            EspRegisterResult result = EspRegisterResult.getEspLoginResult(status);
            return result;
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        
        return EspRegisterResult.NETWORK_UNACCESSIBLE;
    }
    
}
