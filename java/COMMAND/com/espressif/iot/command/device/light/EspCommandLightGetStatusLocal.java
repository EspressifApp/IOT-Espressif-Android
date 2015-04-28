package com.espressif.iot.command.device.light;

import java.net.InetAddress;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.espressif.iot.base.api.EspBaseApiUtil;
import com.espressif.iot.type.device.status.EspStatusLight;
import com.espressif.iot.type.device.status.IEspStatusLight;

public class EspCommandLightGetStatusLocal implements IEspCommandLightGetStatusLocal
{
    private final static Logger log = Logger.getLogger(EspCommandLightGetStatusLocal.class);
    
    private String getLightLocalUrl(InetAddress inetAddress)
    {
        return "http:/" + inetAddress + "/" + "config?command=light";
    }
    
    private IEspStatusLight getCurrentLightStatus(InetAddress inetAddress, String deviceBssid, String router)
    {
        String uriString = getLightLocalUrl(inetAddress);
        JSONObject jo = null;
        if (deviceBssid == null || router == null)
        {
            jo = EspBaseApiUtil.Get(uriString);
        }
        else
        {
            jo = EspBaseApiUtil.GetForJson(uriString, router, deviceBssid);
        }
        if (jo == null)
        {
            return null;
        }
        try
        {
            int freq = jo.getInt(Freq);
            JSONObject rgb = jo.getJSONObject(Rgb);
            int red = rgb.getInt(Red);
            int green = rgb.getInt(Green);
            int blue = rgb.getInt(Blue);
            IEspStatusLight status = new EspStatusLight();
            status.setFreq(freq);
            status.setRed(red);
            status.setGreen(green);
            status.setBlue(blue);
            return status;
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        return null;
    }
    
    @Override
    public IEspStatusLight doCommandLightGetStatusLocal(InetAddress inetAddress)
    {
        IEspStatusLight result = getCurrentLightStatus(inetAddress, null, null);
        log.debug(Thread.currentThread().toString() + "##doCommandLightGetStatusLocal(inetAddress=[" + inetAddress
            + "]): " + result);
        return result;
    }
    
    @Override
    public IEspStatusLight doCommandLightGetStatusLocal(InetAddress inetAddress, String deviceBssid, String router)
    {
        IEspStatusLight result = getCurrentLightStatus(inetAddress, deviceBssid, router);
        log.debug(Thread.currentThread().toString() + "##doCommandLightGetStatusLocal(inetAddress=[" + inetAddress
            + "],deviceBssid=[" + deviceBssid + "],router=[" + router + "]): " + result);
        return result;
    }
    
}
