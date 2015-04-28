package com.espressif.iot.command.user;

import org.apache.http.HttpStatus;
import org.apache.log4j.Logger;
import org.json.JSONArray;
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
        JSONObject jsonObject = new JSONObject();
        try
        {
            jsonObject = new JSONObject();
            jsonObject.put(Email, userEmail);
            jsonObject.put(Password, userPassword);
            jsonObject.put(Scope, USER);
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
                JSONArray keys = jsonObjectResult.getJSONArray(Keys);
                JSONObject key = keys.getJSONObject(0);
                String token = key.getString(Token);
                long id = Long.parseLong(key.getString(User_Id));
                IEspUser user = BEspUser.getBuilder().getInstance();
                user.setUserKey(token);
                user.setUserId(id);
                user.setUserEmail(userEmail);
                user.setUserPassword(userPassword);
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
