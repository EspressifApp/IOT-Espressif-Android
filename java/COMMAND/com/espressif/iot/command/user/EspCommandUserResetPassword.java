package com.espressif.iot.command.user;

import org.json.JSONException;
import org.json.JSONObject;

import com.espressif.iot.base.api.EspBaseApiUtil;
import com.espressif.iot.type.user.EspResetPasswordResult;

public class EspCommandUserResetPassword implements IEspCommandUserResetPassword
{

    @Override
    public EspResetPasswordResult doCommandResetPassword(String email)
    {
        JSONObject postJSON = new JSONObject();
        int status = -1;
        try
        {
            postJSON.put(Email, email);
            
            JSONObject responseJSON = EspBaseApiUtil.Post(URL, postJSON);
            if (responseJSON != null)
            {
                int httpStatus = responseJSON.getInt(Status);
                status = httpStatus;
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        
        return EspResetPasswordResult.getResetPasswordResult(status);
    }

}
