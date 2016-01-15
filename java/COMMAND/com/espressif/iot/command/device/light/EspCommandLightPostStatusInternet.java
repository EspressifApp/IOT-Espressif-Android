package com.espressif.iot.command.device.light;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpStatus;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.espressif.iot.base.api.EspBaseApiUtil;
import com.espressif.iot.type.device.status.IEspStatusLight;
import com.espressif.iot.type.net.HeaderPair;

public class EspCommandLightPostStatusInternet implements IEspCommandLightPostStatusInternet
{
    private final static Logger log = Logger.getLogger(EspCommandLightPostStatusInternet.class);
    
    private JSONObject getJSONByStatus(IEspStatusLight statusLight)
    {
        JSONObject jsonObject = new JSONObject();
        JSONObject jsonObjectX = new JSONObject();
        try
        {
            jsonObjectX.put(X, statusLight.getPeriod());
            jsonObjectX.put(Y, statusLight.getRed());
            jsonObjectX.put(Z, statusLight.getGreen());
            jsonObjectX.put(K, statusLight.getBlue());
            jsonObjectX.put(L, statusLight.getCWhite());
            jsonObject.put(Datapoint, jsonObjectX);
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        
        return jsonObject;
    }
    
    private boolean postCurrentLightStatus(String deviceKey, IEspStatusLight statusLight)
    {
        
        String headerKey = Authorization;
        String headerValue = Token + " " + deviceKey;
        HeaderPair header = new HeaderPair(headerKey, headerValue);
        String url = URL;
        JSONObject jsonObject = getJSONByStatus(statusLight);
        JSONObject result = EspBaseApiUtil.Post(url, jsonObject, header);
        if (result == null)
        {
            return false;
        }
        
        // try {
        int status = -1;
        try
        {
            if (result != null)
            {
                status = result.getInt(Status);
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
    public boolean doCommandLightPostStatusInternet(String deviceKey, IEspStatusLight statusLight)
    {
        boolean result = postCurrentLightStatus(deviceKey, statusLight);
        log.debug(Thread.currentThread().toString() + "##doCommandLightPostStatusInternet(deviceKey=[" + deviceKey
            + "],statusLight=[" + statusLight + "]): " + result);
        return result;
    }
    
    @Override
    public boolean doCommandMulticastPostStatusInternet(String deviceKey, IEspStatusLight statusLight,
        List<String> bssids)
    {
        boolean result = true;
        List<String> macList = new ArrayList<String>();
        for (String bssid : bssids)
        {
            macList.add(bssid);
            if (macList.size() == MULTICAST_GROUP_LENGTH_LIMIT)
            {
                if (!postMulticastCommand(deviceKey, statusLight, macList))
                {
                    result = false;
                }
                macList.clear();
            }
        }
        if (!macList.isEmpty())
        {
            if (!postMulticastCommand(deviceKey, statusLight, macList))
            {
                result = false;
            }
        }
        return result;
    }

    private boolean postMulticastCommand(String deviceKey, IEspStatusLight statusLight, List<String> macList)
    {
        String headerKey = Authorization;
        String headerValue = Token + " " + deviceKey;
        HeaderPair header = new HeaderPair(headerKey, headerValue);
        
        StringBuilder urlBuilder = new StringBuilder(URL_MULTICAST);
        for (String mac : macList) {
            urlBuilder.append(mac).append(",");
        }
        
        try
        {
            JSONObject postJSON = getJSONByStatus(statusLight);
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
