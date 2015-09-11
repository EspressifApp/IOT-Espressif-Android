package com.espressif.iot.command.group;

import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.espressif.iot.base.api.EspBaseApiUtil;
import com.espressif.iot.command.group.IEspCommandGroupDeleteInternet;
import com.espressif.iot.type.net.HeaderPair;

public class EspCommandGroupDeleteInternet implements IEspCommandGroupDeleteInternet
{

    @Override
    public boolean doCommandDeleteGroupInternet(String userKey, long groupId)
    {
        String headerKey = Authorization;
        String headerValue = Token + " " + userKey;
        HeaderPair header = new HeaderPair(headerKey, headerValue);
        
        try
        {
            JSONObject groupJSON = new JSONObject();
            groupJSON.put(Id, groupId);
            JSONArray groupArray = new JSONArray();
            groupArray.put(groupJSON);
            JSONObject postJSON = new JSONObject();
            postJSON.put(KEY_DEVICE_GROUPS, groupArray);
            
            JSONObject resultJSON = EspBaseApiUtil.Post(URL_DELETE, postJSON, header);
            if (resultJSON != null)
            {
                int status = resultJSON.getInt(Status);
                return status == HttpStatus.SC_OK;
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        
        return false;
    }
    
}
