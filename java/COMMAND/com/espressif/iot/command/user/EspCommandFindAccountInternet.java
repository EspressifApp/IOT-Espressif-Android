package com.espressif.iot.command.user;

import org.json.JSONException;
import org.json.JSONObject;

import com.espressif.iot.base.api.EspBaseApiUtil;

public class EspCommandFindAccountInternet implements IEspCommandFindAccountInternet
{

    @Override
    public boolean doCommandFindUsernametInternet(String userName)
    {
        return find(User_Name, userName);
    }

    @Override
    public boolean doCommandFindEmailInternet(String email)
    {
        return find(User_Email, email);
    }
    
    private boolean find(String key, String value)
    {
        JSONObject jsonObject = new JSONObject();
        try
        {
            jsonObject.put(key, value);
            
            JSONObject result = EspBaseApiUtil.Post(URL, jsonObject);
            if (result == null)
            {
                return false;
            }
            
            int status = result.getInt(Status);
            
            if (status == RESULT_FOUND)
            {
                return true;
            }
            else if (status == RESULT_NOT_FOUND)
            {
                return false;
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        
        return false;
    }
}
