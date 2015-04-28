package com.espressif.iot.command.device.plug;

import java.net.InetAddress;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.espressif.iot.base.api.EspBaseApiUtil;
import com.espressif.iot.type.device.status.IEspStatusPlug;

public class EspCommandPlugPostStatusLocal implements IEspCommandPlugPostStatusLocal
{
    
    private final static Logger log = Logger.getLogger(EspCommandPlugPostStatusLocal.class);
    
    private String getPlugLocalUrl(InetAddress inetAddress)
    {
        return "http:/" + inetAddress + "/" + "config?command=switch";
    }
    
    private JSONObject getRequestJSONObject(IEspStatusPlug statusPlug)
    {
        JSONObject request = new JSONObject();
        JSONObject response = new JSONObject();
        try
        {
            int status = 0;
            if (statusPlug.isOn())
            {
                status = 1;
            }
            response.put(Status, status);
            request.put(Response, response);
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        return request;
    }
    
    private boolean postPlugStatus(InetAddress inetAddress, IEspStatusPlug statusPlug, String deviceBssid, String router)
    {
        String uriString = getPlugLocalUrl(inetAddress);
        JSONObject jsonObject;
        jsonObject = getRequestJSONObject(statusPlug);
        if (EspBaseApiUtil.Post(uriString, jsonObject) != null)
        {
            return true;
        }
        else
        {
            return false;
        }
    }
    
    @Override
    public boolean doCommandPlugPostStatusLocal(InetAddress inetAddress, IEspStatusPlug statusPlug)
    {
        boolean result = postPlugStatus(inetAddress, statusPlug, null, null);
        log.debug(Thread.currentThread().toString() + "##doCommandPlugPostStatusInternet(inetAddress=[" + inetAddress
            + "],statusPlug=[" + statusPlug + "]): " + result);
        return result;
    }
    
    @Override
    public boolean doCommandPlugPostStatusLocal(InetAddress inetAddress, IEspStatusPlug statusPlug, String deviceBssid,
        String router)
    {
        boolean result = postPlugStatus(inetAddress, statusPlug, null, null);
        log.debug(Thread.currentThread().toString() + "##doCommandPlugPostStatusLocal(inetAddress=[" + inetAddress
            + "],statusPlug=[" + statusPlug + "],deviceBssid=[" + deviceBssid + "],router=[" + router + "]): " + result);
        return result;
    }
    
}
