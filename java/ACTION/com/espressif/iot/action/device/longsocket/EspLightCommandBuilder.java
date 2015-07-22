package com.espressif.iot.action.device.longsocket;

import java.net.InetAddress;

import org.json.JSONException;
import org.json.JSONObject;

import com.espressif.iot.base.net.rest.mesh.EspSocketRequestBaseEntity;
import com.espressif.iot.type.device.status.IEspStatusLight;

public class EspLightCommandBuilder implements IEspLightCommandBuilder
{
    /*
     * Singleton lazy initialization start
     */
    private EspLightCommandBuilder()
    {
    }
    
    private static class InstanceHolder
    {
        static EspLightCommandBuilder instance = new EspLightCommandBuilder();
    }
    
    public static EspLightCommandBuilder getInstance()
    {
        return InstanceHolder.instance;
    }
    
    /*
     * Singleton lazy initialization end
     */
    
    // it hasn't been used, let it here just for it maybe used in the future
    @Override
    public EspSocketRequestBaseEntity buildLocalGetStatusRequest(InetAddress inetAddress, String router)
    {
        String uriStr = "http:/" + inetAddress + "/" + "config?command=light";
        EspSocketRequestBaseEntity request = new EspSocketRequestBaseEntity("GET", uriStr);
        request.putHeaderParams("Connection", "keep-alive");
        return request;
    }
    
    @Override
    public EspSocketRequestBaseEntity buildLocalPostStatusRequest(InetAddress inetAddress, IEspStatusLight statusLight, String router)
    {
        String uriStr = "http:/" + inetAddress + "/" + "config?command=light";
        JSONObject requestJson = new JSONObject();
        JSONObject rgb = new JSONObject();
        try
        {
            rgb.put(Red, statusLight.getRed());
            rgb.put(Green, statusLight.getGreen());
            rgb.put(Blue, statusLight.getBlue());
            rgb.put(CWhite, statusLight.getCWhite());
            rgb.put(WWhite, statusLight.getWWhite());
            requestJson.put("response", 0);
            requestJson.put(Period, statusLight.getPeriod());
            requestJson.put(Rgb, rgb);
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        EspSocketRequestBaseEntity request = new EspSocketRequestBaseEntity("POST", uriStr, requestJson.toString());
        request.putHeaderParams("Connection", "keep-alive");
        return request;
    }
    
    @Override
    public EspSocketRequestBaseEntity buildInternetGetStatusRequest(String deviceKey)
    {
        String uriStr = "https://iot.espressif.cn/v1/datastreams/light/datapoint/?deliver_to_device=true";
        String headerKey = Authorization;
        String headerValue = Token + " " + deviceKey;
        EspSocketRequestBaseEntity request = new EspSocketRequestBaseEntity("GET", uriStr);
        request.putHeaderParams("Connection", "keep-alive");
        request.putHeaderParams(headerKey, headerValue);
        return request;
    }
    
    @Override
    public EspSocketRequestBaseEntity buildInternetPostStatusRequest(String deviceKey, IEspStatusLight statusLight, String router)
    {
        String uriStr = "https://iot.espressif.cn/v1/datastreams/light/datapoint/?deliver_to_device=true";
        String headerKey = Authorization;
        String headerValue = Token + " " + deviceKey;
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
            return null;
        }
        // for Internet, we don't care whether the device is router or not, so we treat them as non-router
        EspSocketRequestBaseEntity request = new EspSocketRequestBaseEntity("POST", uriStr, jsonObject.toString());
        request.putHeaderParams("Connection", "keep-alive");
        request.putHeaderParams(headerKey, headerValue);
        return request;
    }
}
