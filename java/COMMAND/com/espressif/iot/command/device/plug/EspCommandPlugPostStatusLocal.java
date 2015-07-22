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
    
    @Override
    public String getLocalUrl(InetAddress inetAddress)
    {
        return "http://" + inetAddress.getHostAddress() + "/" + "config?command=switch";
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
        String uriString = getLocalUrl(inetAddress);
        JSONObject jsonObject;
        JSONObject result = null;
        jsonObject = getRequestJSONObject(statusPlug);
        if (deviceBssid == null || router == null)
        {
            result = EspBaseApiUtil.Post(uriString, jsonObject);
        }
        else
        {
            result = EspBaseApiUtil.PostForJson(uriString, router, deviceBssid, jsonObject);
        }
        return (result != null);
    }
    
    private boolean postPlugStatus2(InetAddress inetAddress, IEspStatusPlug statusPlug, String deviceBssid, boolean isMeshDevice)
    {
        String uriString = getLocalUrl(inetAddress);
        JSONObject jsonObject;
        JSONObject result = null;
        jsonObject = getRequestJSONObject(statusPlug);
        if (deviceBssid == null || !isMeshDevice)
        {
            result = EspBaseApiUtil.Post(uriString, jsonObject);
        }
        else
        {
            result = EspBaseApiUtil.PostForJson(uriString, null, deviceBssid, jsonObject);
        }
        return (result != null);
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
        boolean result = postPlugStatus(inetAddress, statusPlug, deviceBssid, router);
        log.debug(Thread.currentThread().toString() + "##doCommandPlugPostStatusLocal(inetAddress=[" + inetAddress
            + "],statusPlug=[" + statusPlug + "],deviceBssid=[" + deviceBssid + "],router=[" + router + "]): " + result);
        return result;
    }

    @Override
    public boolean doCommandPlugPostStatusLocal(InetAddress inetAddress, IEspStatusPlug statusPlug, String deviceBssid,
        boolean isMeshDevice)
    {
        boolean result = postPlugStatus2(inetAddress, statusPlug, deviceBssid, isMeshDevice);
        log.debug(Thread.currentThread().toString() + "##doCommandPlugPostStatusLocal(inetAddress=[" + inetAddress
            + "],statusPlug=[" + statusPlug + "],deviceBssid=[" + deviceBssid + "],router=[" + isMeshDevice + "]): "
            + result);
        return result;
    }
    
}
