package com.espressif.iot.command.user;

import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.json.JSONObject;

import com.espressif.iot.base.api.EspBaseApiUtil;
import com.espressif.iot.type.user.EspLoginResult;
import com.espressif.iot.user.IEspUser;
import com.espressif.iot.user.builder.BEspUser;

public class EspCommandUserLoginPhoneInternet implements IEspCommandUserLoginPhoneInternet
{

    @Override
    public EspLoginResult doCommandUserLoginPhone(String phoneNumber, String userPassword)
    {
        try
        {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(User_Phone, phoneNumber);
            jsonObject.put(User_Password, userPassword);
            jsonObject.put(KEY_STATE, "phone_password");
            jsonObject.put(KEY_CODE, "phone login");
            JSONObject jsonObjectResult = EspBaseApiUtil.Post(URL, jsonObject);
            EspLoginResult result = null;
            if (jsonObjectResult == null)
            {
                return EspLoginResult.NETWORK_UNACCESSIBLE;
            }
            int status = jsonObjectResult.getInt(Status);
            if (status == HttpStatus.SC_OK)
            {
                JSONObject userJSON = jsonObjectResult.getJSONObject(USER);
                long userId = userJSON.getLong(Id);
                String userName = userJSON.getString(User_Name);
                String email = userJSON.getString(User_Email);
                JSONObject keyJSON = jsonObjectResult.getJSONObject(Key);
                String userKey = keyJSON.getString(Token);
                
                IEspUser user = BEspUser.getBuilder().getInstance();
                user.setUserKey(userKey);
                user.setUserId(userId);
                user.setUserName(userName);
                user.setUserEmail(email);
            }
            else
            {
            }
            result = EspLoginResult.getEspLoginResult(status);
            return result;
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        return EspLoginResult.NETWORK_UNACCESSIBLE;
    }
    
}
