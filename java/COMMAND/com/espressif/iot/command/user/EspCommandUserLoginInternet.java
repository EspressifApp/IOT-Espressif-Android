package com.espressif.iot.command.user;

import org.apache.http.HttpStatus;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.espressif.iot.base.api.EspBaseApiUtil;
import com.espressif.iot.type.user.EspLoginResult;
import com.espressif.iot.user.IEspUser;
import com.espressif.iot.user.builder.BEspUser;

public class EspCommandUserLoginInternet implements IEspCommandUserLoginInternet
{
    private final static Logger log = Logger.getLogger(EspCommandUserLoginInternet.class);
    
    @Override
    public EspLoginResult doCommandUserLoginInternet(String userEmail, String userPassword)
    {
        try
        {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(Email, userEmail);
            jsonObject.put(Password, userPassword);
            jsonObject.put(Remember, 1);
            JSONObject jsonObjectResult = EspBaseApiUtil.Post(URL, jsonObject);
            EspLoginResult result = null;
            if (jsonObjectResult == null)
            {
                log.debug(Thread.currentThread().toString() + "##doCommandUserLoginInternet(userEmail=[" + userEmail
                    + "],userPassword=[" + userPassword + "]): " + EspLoginResult.NETWORK_UNACCESSIBLE);
                return EspLoginResult.NETWORK_UNACCESSIBLE;
            }
            int status = jsonObjectResult.getInt(Status);
            if (status == HttpStatus.SC_OK)
            {
                JSONObject userJSON = jsonObjectResult.getJSONObject(USER);
                long userId = userJSON.getLong(Id);
                String userName = userJSON.getString(User_Name);
                JSONObject keyJSON = jsonObjectResult.getJSONObject(Key);
                String userKey = keyJSON.getString(Token);
                
                IEspUser user = BEspUser.getBuilder().getInstance();
                user.setUserKey(userKey);
                user.setUserId(userId);
                user.setUserName(userName);
                user.setUserEmail(userEmail);
            }
            else
            {
            }
            result = EspLoginResult.getEspLoginResult(status);
            log.debug(Thread.currentThread().toString() + "##doCommandUserLoginInternet(userEmail=[" + userEmail
                + "],userPassword=[" + userPassword + "]): " + result);
            return result;
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        log.debug(Thread.currentThread().toString() + "##doCommandUserLoginInternet(userEmail=[" + userEmail
            + "],userPassword=[" + userPassword + "]): " + EspLoginResult.NETWORK_UNACCESSIBLE);
        return EspLoginResult.NETWORK_UNACCESSIBLE;
    }
    
}
