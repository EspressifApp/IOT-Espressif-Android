package com.espressif.iot.command.device.espbutton;

import java.net.InetAddress;

import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.espressif.iot.base.api.EspBaseApiUtil;
import com.espressif.iot.device.IEspDevice;
import com.espressif.iot.type.net.HeaderPair;

public class EspCommandEspButtonGroupDelete implements IEspCommandEspButtonGroupDelete
{
    
    @Override
    public String getLocalUrl(InetAddress inetAddress)
    {
        return "http://" + inetAddress.getHostAddress() + "/device/button/groups/?action=delete";
    }
    
    @Override
    public boolean doCommandEspButtonDeleteGroup(IEspDevice inetDevice, String buttonMac, long[] delGroupIds)
    {
        HeaderPair header = new HeaderPair(Authorization, Token + " " + buttonMac);
        String url = getLocalUrl(inetDevice.getInetAddress());
        
        try
        {
            JSONArray groupsArray = new JSONArray();
            for (long id : delGroupIds)
            {
                JSONObject groupJSON = new JSONObject();
                groupJSON.put(KEY_GROUP_ID, id);
                groupsArray.put(groupJSON);
            }
            JSONObject postJSON = new JSONObject();
            postJSON.put(KEY_GROUP, groupsArray);
            
            JSONObject resultJSON = EspBaseApiUtil.PostForJson(url, inetDevice.getBssid(), postJSON, header);
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
