package com.espressif.iot.command.device.espbutton;

import java.net.InetAddress;

import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.json.JSONObject;

import com.espressif.iot.base.api.EspBaseApiUtil;
import com.espressif.iot.device.IEspDevice;
import com.espressif.iot.type.net.HeaderPair;

public class EspCommandEspButtonGroupCreate implements IEspCommandEspButtonGroupCreate
{

    @Override
    public String getLocalUrl(InetAddress inetAddress)
    {
        return "http://" + inetAddress.getHostAddress() + "/device/button/groups/?action=create";
    }

    @Override
    public long doCommandEspButtonCreateGroup(IEspDevice inetDevice, String buttonMac)
    {
        HeaderPair header = new HeaderPair(Authorization, Token + " " + buttonMac);
        String url = getLocalUrl(inetDevice.getInetAddress());
        
        JSONObject resultJSON = EspBaseApiUtil.PostForJson(url, inetDevice.getBssid(), null, header);
        if (resultJSON != null)
        {
            int status;
            try
            {
                status = resultJSON.getInt(Status);
                if (status == HttpStatus.SC_OK)
                {
                    long id = resultJSON.getLong(KEY_GROUP_ID);
                    return id;
                }
                else if (status == HttpStatus.SC_FORBIDDEN)
                {
                    return -status;
                }
            }
            catch (JSONException e)
            {
                e.printStackTrace();
            }
        }
        
        return -1;
    }
    
}
