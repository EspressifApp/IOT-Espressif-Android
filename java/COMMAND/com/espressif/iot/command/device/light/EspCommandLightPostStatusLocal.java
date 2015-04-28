package com.espressif.iot.command.device.light;

import java.net.InetAddress;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.espressif.iot.base.api.EspBaseApiUtil;
import com.espressif.iot.type.device.status.IEspStatusLight;

public class EspCommandLightPostStatusLocal implements IEspCommandLightPostStatusLocal
{
    private final static Logger log = Logger.getLogger(EspCommandLightPostStatusLocal.class);
    
    private String getLightLocalUrl(InetAddress inetAddress)
    {
        return "http:/" + inetAddress + "/" + "config?command=light";
    }
    
    private JSONObject getRequestJSONObject(IEspStatusLight statusLight)
    {
        JSONObject request = new JSONObject();
        JSONObject rgb = new JSONObject();
        try
        {
            rgb.put(Red, statusLight.getRed());
            rgb.put(Green, statusLight.getGreen());
            rgb.put(Blue, statusLight.getBlue());
            request.put(Freq, statusLight.getFreq());
            request.put(Rgb, rgb);
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        return request;
    }
    
    private boolean postLightStatus(InetAddress inetAddress, IEspStatusLight statusLight, String deviceBssid,
        String router)
    {
        String uriString = getLightLocalUrl(inetAddress);
        JSONObject jsonObject = getRequestJSONObject(statusLight);
        JSONObject result = null;
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
    
    @Override
    public boolean doCommandLightPostStatusLocal(InetAddress inetAddress, IEspStatusLight statusLight)
    {
        boolean result = postLightStatus(inetAddress, statusLight, null, null);
        log.debug(Thread.currentThread().toString() + "##doCommandLightPostStatusLocal(inetAddress=[" + inetAddress
            + "],statusLight=[" + statusLight + "]): " + result);
        return result;
    }
    
    @Override
    public boolean doCommandLightPostStatusLocal(InetAddress inetAddress, IEspStatusLight statusLight,
        String deviceBssid, String router)
    {
        boolean result = postLightStatus(inetAddress, statusLight, deviceBssid, router);
        log.debug(Thread.currentThread().toString() + "##doCommandLightPostStatusLocal(inetAddress=[" + inetAddress
            + "],statusLight=[" + statusLight + "],deviceBssid=[" + deviceBssid + "],router=[" + router + "]): "
            + result);
        return result;
    }
    
}
