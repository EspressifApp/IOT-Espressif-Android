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
    
    private JSONObject getRequestJSONObject(IEspStatusLight statusLight, boolean isResponseRequired)
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
            if (isResponseRequired)
            {
                request.put(Response, 1);
            }
            else
            {
                request.put(Response, 0);
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        return request;
    }
    
    private boolean postLightStatus2(InetAddress inetAddress, IEspStatusLight statusLight, String deviceBssid,
        boolean isMeshDevice, boolean isResponseRequired, Runnable disconnectedCallback)
    {
        String uriString = getLocalUrl(inetAddress);
        JSONObject jsonObject = getRequestJSONObject(statusLight, isResponseRequired);
        JSONObject result = null;
        if (isResponseRequired)
        {
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
        else
        {
            // no response is available, so we treat it suc, when fail socket will be disconnected
            if (deviceBssid == null || !isMeshDevice)
            {
                // normal device
                EspBaseApiUtil.PostInstantly(uriString, jsonObject, disconnectedCallback);
            }
            else
            {
                // mesh device
                EspBaseApiUtil.PostForJsonInstantly(uriString, deviceBssid, jsonObject, disconnectedCallback);
            }
            return true;
        }
    }
    
    @Override
    public boolean doCommandLightPostStatusLocal(InetAddress inetAddress, IEspStatusLight statusLight)
    {
        boolean result = postLightStatus2(inetAddress, statusLight, null, false, true, null);
        log.debug(Thread.currentThread().toString() + "##doCommandLightPostStatusLocal(inetAddress=[" + inetAddress
            + "],statusLight=[" + statusLight + "]): " + result);
        return result;
    }
    
    @Override
    public boolean doCommandLightPostStatusLocal(InetAddress inetAddress, IEspStatusLight statusLight,
        String deviceBssid, boolean isMeshDevice)
    {
        boolean result = postLightStatus2(inetAddress, statusLight, deviceBssid, isMeshDevice, true, null);
        log.debug(Thread.currentThread().toString() + "##doCommandLightPostStatusLocal(inetAddress=[" + inetAddress
            + "],statusLight=[" + statusLight + "],deviceBssid=[" + deviceBssid + "],isMeshDevice=[" + isMeshDevice
            + "]): " + result);
        return result;
    }
    
    @Override
    public void doCommandLightPostStatusLocalInstantly(InetAddress inetAddress, IEspStatusLight statusLight,
        String deviceBssid, boolean isMeshDevice, Runnable disconnectedCallback)
    {
        postLightStatus2(inetAddress, statusLight, deviceBssid, isMeshDevice, false, disconnectedCallback);
        log.debug(Thread.currentThread().toString() + "##doCommandLightPostStatusLocalInstantly(inetAddress=["
            + inetAddress + "],statusLight=[" + statusLight + "],deviceBssid=[" + deviceBssid + "],isMeshDevice=["
            + isMeshDevice + "])");
    }
    
}
