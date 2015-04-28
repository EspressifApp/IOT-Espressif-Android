package com.espressif.iot.command.device.common;

import org.apache.http.HttpStatus;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.espressif.iot.base.api.EspBaseApiUtil;
import com.espressif.iot.type.net.HeaderPair;

public class EspCommandDeviceDeleteInternet implements IEspCommandDeviceDeleteInternet
{
    private final static Logger log = Logger.getLogger(EspCommandDeviceDeleteInternet.class);
    
    private boolean deleteDevice(String deviceKey)
    {
        
        String headerKey = Authorization;
        String headerValue = "token " + deviceKey;
        
        JSONObject result = null;
        HeaderPair header = new HeaderPair(headerKey, headerValue);
        result = EspBaseApiUtil.Post(URL, null, header);
        
        if (result == null)
        {
            return false;
        }
        
        int status = -1;
        try
        {
            if (result != null)
            {
                status = Integer.parseInt(result.getString("status"));
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        
        // if status is HttpStatus.SC_FORBIDDEN(403),
        // the device has been deleted on server already before long
        if (status == HttpStatus.SC_OK || status == HttpStatus.SC_FORBIDDEN)
        {
            return true;
        }
        else
        {
            return false;
        }
    }
    
    @Override
    public boolean doCommandDeviceDeleteInternet(String deviceKey)
    {
        boolean result = deleteDevice(deviceKey);
        log.debug(Thread.currentThread().toString() + "##doCommandDeviceDeleteInternet(deviceKey=[" + deviceKey
            + "]): " + result);
        return result;
    }
    
}
