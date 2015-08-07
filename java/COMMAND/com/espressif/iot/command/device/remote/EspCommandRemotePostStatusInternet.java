package com.espressif.iot.command.device.remote;

import org.apache.http.HttpStatus;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.espressif.iot.base.api.EspBaseApiUtil;
import com.espressif.iot.type.device.status.IEspStatusRemote;
import com.espressif.iot.type.net.HeaderPair;

public class EspCommandRemotePostStatusInternet implements IEspCommandRemotePostStatusInternet
{
    
    private final static Logger log = Logger.getLogger(EspCommandRemotePostStatusInternet.class);
    
    private boolean postCurrentRemoteStatus(String deviceKey, IEspStatusRemote statusRemote)
    {
        JSONObject jsonObject = new JSONObject();
        JSONObject jsonObjectX = new JSONObject();
        String headerKey = Authorization;
        String headerValue = Token + " " + deviceKey;
        
        JSONObject result = null;
        try
        {
            jsonObjectX.put(X, statusRemote.getAddress());
            jsonObjectX.put(Y, statusRemote.getCommand());
            jsonObjectX.put(Z, statusRemote.getRepeat());
            jsonObject.put(Datapoint, jsonObjectX);
        }
        catch (JSONException e)
        {
            e.printStackTrace();
            return false;
        }
        
        HeaderPair header = new HeaderPair(headerKey, headerValue);
        String url = URL;
        result = EspBaseApiUtil.Post(url, jsonObject, header);
        if (result == null)
        {
            return false;
        }
        
        int status = -1;
        try
        {
            if (result != null)
            {
                status = Integer.parseInt(result.getString(Status));
            }
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
    public boolean doCommandRemotePostStatusInternet(String deviceKey, IEspStatusRemote statusRemote)
    {
        boolean result = postCurrentRemoteStatus(deviceKey, statusRemote);
        log.debug(Thread.currentThread().toString() + "##doCommandRemotePostStatusInternet(deviceKey=[" + deviceKey
            + "],statusRemote=[" + statusRemote + "]): " + result);
        return result;
    }
    
}
