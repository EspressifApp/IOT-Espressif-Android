package com.espressif.iot.command.user;

import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.espressif.iot.base.api.EspBaseApiUtil;
import com.espressif.iot.type.user.EspLoginResult;
import com.espressif.iot.user.IEspUser;
import com.espressif.iot.user.builder.BEspUser;

public class EspCommandThirdPartyLoginInternet implements IEspCommandThirdPartyLoginInternet
{

    @Override
    public EspLoginResult doCommandThirdPartLoginInternet(String state, String accessToken)
    {
        try
        {
            JSONObject json = new JSONObject();
            json.put(Scope, USER);
            json.put(KEY_STATE, state);
            json.put(KEY_ACCESS_TOKEN, accessToken);
            
            JSONObject jsonResult = EspBaseApiUtil.Post(URL, json);
            if (jsonResult == null)
            {
                return EspLoginResult.NETWORK_UNACCESSIBLE;
            }
            
            int status = jsonResult.getInt(Status);
            if (status != HttpStatus.SC_OK)
            {
                return EspLoginResult.getEspLoginResult(status);
            }
            
            JSONArray keys = jsonResult.getJSONArray(Keys);
            JSONObject key = keys.getJSONObject(0);
            String userKey = key.getString(Token);
            long userId = Long.parseLong(key.getString(User_Id));
            IEspUser user = BEspUser.getBuilder().getInstance();
            user.setUserKey(userKey);
            user.setUserId(userId);
            
            return EspLoginResult.SUC;
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        
        return EspLoginResult.NETWORK_UNACCESSIBLE;
    }
    
}
