package com.espressif.iot.command.user;

import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.json.JSONObject;

import com.espressif.iot.base.api.EspBaseApiUtil;
import com.espressif.iot.type.user.EspLoginResult;
import com.espressif.iot.type.user.EspThirdPartyLoginPlat;
import com.espressif.iot.user.IEspUser;
import com.espressif.iot.user.builder.BEspUser;

public class EspCommandThirdPartyLoginInternet implements IEspCommandThirdPartyLoginInternet
{

    @Override
    public EspLoginResult doCommandThirdPartLoginInternet(EspThirdPartyLoginPlat espPlat)
    {
        try
        {
            JSONObject json = new JSONObject();
            json.put(Remember, 1);
            json.put(KEY_STATE, espPlat.getState());
            json.put(KEY_ACCESS_TOKEN, espPlat.getAccessToken());
            if (espPlat.getOpenId() != null)
            {
                json.put(KEY_OPEN_ID, espPlat.getOpenId());
            }
            
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
            
            JSONObject userJSON = jsonResult.getJSONObject(USER);
            long userId = userJSON.getLong(Id);
            String userName = userJSON.getString(User_Name);
            String email = userJSON.getString(User_Email);
            JSONObject keyJSON = jsonResult.getJSONObject(Key);
            String userKey = keyJSON.getString(Token);
            
            IEspUser user = BEspUser.getBuilder().getInstance();
            user.setUserKey(userKey);
            user.setUserId(userId);
            user.setUserName(userName);
            user.setUserEmail(email);
            
            return EspLoginResult.SUC;
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        
        return EspLoginResult.NETWORK_UNACCESSIBLE;
    }
    
}
