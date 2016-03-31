package com.espressif.iot.command.device.plug;

import java.util.ArrayList;
import java.util.List;

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
    
    private JSONObject getJSONByStatus(IEspStatusPlug statusPlug)
    {
        JSONObject jsonObject = new JSONObject();
        JSONObject jsonObjectX = new JSONObject();
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
        
        return jsonObject;
    }
    
    private boolean postPlugStatus(String deviceKey, IEspStatusPlug statusPlug)
    {
        String headerKey = Authorization;
        String headerValue = Token + " " + deviceKey;
        HeaderPair header = new HeaderPair(headerKey, headerValue);
        
        String url = URL;
        
        JSONObject jsonObject = getJSONByStatus(statusPlug);
        
        JSONObject result = EspBaseApiUtil.Post(url, jsonObject, header);
        int status = -1;
        try
        {
            if (result != null)
                status = result.getInt(Status);
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
    
    @Override
    public boolean doCommandMulticastPostStatusInternet(String deviceKey, IEspStatusPlug statusPlug, List<String> bssids)
    {
        boolean result = true;
        List<String> macList = new ArrayList<String>();
        for (String bssid : bssids)
        {
            macList.add(bssid);
            if (macList.size() == MULTICAST_GROUP_LENGTH_LIMIT)
            {
                if (!postMulticastCommand(deviceKey, statusPlug, macList))
                {
                    result = false;
                }
                macList.clear();
            }
        }
        if (!macList.isEmpty())
        {
            if (!postMulticastCommand(deviceKey, statusPlug, macList))
            {
                result = false;
            }
        }
        return result;
    }
    
    private boolean postMulticastCommand(String deviceKey, IEspStatusPlug statusPlug, List<String> macList)
    {
        String headerKey = Authorization;
        String headerValue = Token + " " + deviceKey;
        HeaderPair header = new HeaderPair(headerKey, headerValue);
        
        StringBuilder urlBuilder = new StringBuilder(URL_MULTICAST);
        for (int i = 0; i < macList.size(); i++) {
            String mac = macList.get(i);
            urlBuilder.append(mac);
            if (i < macList.size() - 1) {
                urlBuilder.append(",");
            }
        }
        
        try
        {
            JSONObject postJSON = getJSONByStatus(statusPlug);
            JSONObject result = EspBaseApiUtil.Post(urlBuilder.toString(), postJSON, header);
            if (result != null)
            {
                int httpStatus = result.getInt(Status);
                return httpStatus == HttpStatus.SC_OK;
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
            return false;
        }
        
        return false;
    }
}
