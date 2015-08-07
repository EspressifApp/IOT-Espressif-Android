package com.espressif.iot.command.device.plug;

import org.apache.http.HttpStatus;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.espressif.iot.base.api.EspBaseApiUtil;
import com.espressif.iot.type.device.status.IEspStatusPlug;
import com.espressif.iot.type.net.HeaderPair;

public class EspCommandPlugPostStatusInternet implements IEspCommandPlugPostStatusInternet
{
    private final static Logger log = Logger.getLogger(EspCommandPlugPostStatusInternet.class);
    
    private boolean postPlugStatus(String deviceKey, IEspStatusPlug statusPlug)
    {
        
        JSONObject jsonObject = new JSONObject();
        JSONObject jsonObjectX = new JSONObject();
        String headerKey = Authorization;
        String headerValue = Token + " " + deviceKey;
        HeaderPair header = new HeaderPair(headerKey, headerValue);
        
        JSONObject result = null;
        try
        {
            if (statusPlug.isOn())
                jsonObjectX.put("x", 1);
            else
                jsonObjectX.put("x", 0);
            jsonObject.put(Datapoint, jsonObjectX);
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        
        String url = URL;
        result = EspBaseApiUtil.Post(url, jsonObject, header);
        int status = -1;
        try
        {
            if (result != null)
                status = Integer.parseInt(result.getString(Status));
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        if (status == HttpStatus.SC_OK)
        {
            return true;
        }
        else
        {
            return false;
        }
        
    }
    
    @Override
    public boolean doCommandPlugPostStatusInternet(String deviceKey, IEspStatusPlug statusPlug)
    {
        boolean result = postPlugStatus(deviceKey, statusPlug);
        log.debug(Thread.currentThread().toString() + "##doCommandPlugPostStatusInternet(deviceKey=[" + deviceKey
            + "],statusPlug=[" + statusPlug + "]): " + result);
        return result;
    }
    
}
