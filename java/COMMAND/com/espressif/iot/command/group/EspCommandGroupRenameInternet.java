package com.espressif.iot.command.group;

import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.espressif.iot.base.api.EspBaseApiUtil;
import com.espressif.iot.command.group.IEspCommandGroupRenameInternet;
import com.espressif.iot.type.net.HeaderPair;

public class EspCommandGroupRenameInternet implements IEspCommandGroupRenameInternet
{
    
    @Override
    public boolean doCommandRenameGroupInternet(String userKey, long groupId, String newName)
    {
        String headerKey = Authorization;
        String headerValue = Token + " " + userKey;
        HeaderPair header = new HeaderPair(headerKey, headerValue);
        
        try
        {
            JSONObject groupJSON = new JSONObject();
            groupJSON.put(Id, groupId);
            groupJSON.put(KEY_GROUP_NAME, newName);
            JSONArray groupArray = new JSONArray();
            groupArray.put(groupJSON);
            JSONObject postJSON = new JSONObject();
            postJSON.put(KEY_DEVICE_GROUPS, groupArray);
            
            JSONObject resultJSON = EspBaseApiUtil.Post(URL_MODIFY, postJSON, header);
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
