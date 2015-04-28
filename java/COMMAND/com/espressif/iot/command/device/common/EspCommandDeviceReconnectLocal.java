package com.espressif.iot.command.device.common;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.espressif.iot.base.api.EspBaseApiUtil;
import com.espressif.iot.base.application.EspApplication;
import com.espressif.iot.type.net.WifiCipherType;

public class EspCommandDeviceReconnectLocal implements IEspCommandDeviceReconnectLocal
{
    private final static Logger log = Logger.getLogger(EspCommandDeviceReconnectLocal.class);
    
    @Override
    public boolean doCommandReconnectLocal(String router, String deviceBssid, String apSsid, WifiCipherType type,
        String... apPassword)
    {
        JSONObject Content = new JSONObject();
        JSONObject Connect_Station = new JSONObject();
        JSONObject Station = new JSONObject();
        JSONObject Request = new JSONObject();
        try
        {
            if (apPassword != null)
            {
                Content.put("password", apPassword[0]);
            }
            Content.put("ssid", apSsid);
            
            Connect_Station.put("Connect_Station", Content);
            
            Station.put("Station", Connect_Station);
            
            Request.put("Request", Station);
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        String gateWay = EspApplication.sharedInstance().getGateway();
        JSONObject result =
            EspBaseApiUtil.PostForJson(URL.replace("192.168.4.1", gateWay), router, deviceBssid, Request);
        log.debug(Thread.currentThread().toString() + "##doCommandReconnectLocal(deviceBssid=[" + deviceBssid + "],apSsid=["
            + apSsid + "],apPassword=[" + apPassword + "]): " + result);
        return result != null;
    }
}
