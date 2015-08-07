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
    
    @Override
    public String getLocalUrl(InetAddress inetAddress)
    {
        return "http://" + inetAddress.getHostAddress() + "/" + "config?command=light";
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
            rgb.put(CWhite, statusLight.getCWhite());
            rgb.put(WWhite, statusLight.getWWhite());
            request.put(Period, statusLight.getPeriod());
            request.put(Rgb, rgb);
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        return request;
    }
    
    private boolean postLightStatus2(InetAddress inetAddress, IEspStatusLight statusLight, String deviceBssid,
        boolean isMeshDevice)
    {
        String uriString = getLocalUrl(inetAddress);
        JSONObject jsonObject = getRequestJSONObject(statusLight);
        JSONObject result = null;
        if (deviceBssid == null || !isMeshDevice)
        {
            result = EspBaseApiUtil.Post(uriString, jsonObject);
        }
        else
        {
            result = EspBaseApiUtil.PostForJson(uriString, deviceBssid, jsonObject);
        }
        return (result != null);
    }
    
    @Override
    public boolean doCommandLightPostStatusLocal(InetAddress inetAddress, IEspStatusLight statusLight)
    {
        boolean result = postLightStatus2(inetAddress, statusLight, null, false);
        log.debug(Thread.currentThread().toString() + "##doCommandLightPostStatusLocal(inetAddress=[" + inetAddress
            + "],statusLight=[" + statusLight + "]): " + result);
        return result;
    }
    
    @Override
    public boolean doCommandLightPostStatusLocal(InetAddress inetAddress, IEspStatusLight statusLight,
        String deviceBssid, boolean isMeshDevice)
    {
        boolean result = postLightStatus2(inetAddress, statusLight, deviceBssid, isMeshDevice);
        log.debug(Thread.currentThread().toString() + "##doCommandLightPostStatusLocal(inetAddress=[" + inetAddress
            + "],statusLight=[" + statusLight + "],deviceBssid=[" + deviceBssid + "],isMeshDevice=[" + isMeshDevice
            + "]): " + result);
        return result;
    }
    
}
