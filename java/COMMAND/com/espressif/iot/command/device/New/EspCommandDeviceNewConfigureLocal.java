package com.espressif.iot.command.device.New;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.espressif.iot.base.api.EspBaseApiUtil;
import com.espressif.iot.base.application.EspApplication;
import com.espressif.iot.type.net.WifiCipherType;

public class EspCommandDeviceNewConfigureLocal implements IEspCommandDeviceNewConfigureLocal
{
    private final static Logger log = Logger.getLogger(EspCommandDeviceNewConfigureLocal.class);
    
    @Override
    public String getLocalUrl(InetAddress inetAddress)
    {
        return "http://" + inetAddress.getHostAddress() + "/" + "config?command=wifi";
    }
    
    @Override
    public boolean doCommandDeviceNewConfigureLocal(String deviceSsid, WifiCipherType deviceWifiCipherType,
        String devicePassword, String apSsid, WifiCipherType apWifiCipherType, String apPassword, String randomToken)
    {
        JSONObject Content = new JSONObject();
        JSONObject Connect_Station = new JSONObject();
        JSONObject Station = new JSONObject();
        JSONObject Request = new JSONObject();
        try
        {
            Content.put("token", randomToken);
            Content.put("password", apPassword);
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
        InetAddress inetAddress = null;
        try
        {
            inetAddress = InetAddress.getByName(gateWay);
        }
        catch (UnknownHostException e)
        {
            e.printStackTrace();
        }
        String urlString = getLocalUrl(inetAddress);
        JSONObject result = EspBaseApiUtil.Post(urlString, Request);
        log.debug(Thread.currentThread().toString() + "##doCommandDeviceNewConfigureLocal(deviceSsid=[" + deviceSsid
            + "],deviceWifiCipherType=[" + deviceWifiCipherType + "],devicePassword=[" + devicePassword + "],apSsid=["
            + apSsid + "],apWifiCipherType=[" + apWifiCipherType + "],apPassword=[" + apPassword + "],randomToken=["
            + randomToken + "]): " + result);
        return true;
    }
    
    @Override
    public boolean doCommandMeshDeviceNewConfigureLocal(String deviceBssid, String deviceSsid,
        WifiCipherType deviceWifiCipherType, String devicePassword, String randomToken)
    {
        JSONObject Content = new JSONObject();
        JSONObject Connect_Station = new JSONObject();
        JSONObject Station = new JSONObject();
        JSONObject Request = new JSONObject();
        try
        {
            Content.put("token", randomToken);
            
            Connect_Station.put("Connect_Station", Content);
            
            Station.put("Station", Connect_Station);
            
            Request.put("Request", Station);
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        
        String gateWay = EspApplication.sharedInstance().getGateway();
        InetAddress inetAddress = null;
        try
        {
            inetAddress = InetAddress.getByName(gateWay);
        }
        catch (UnknownHostException e)
        {
            e.printStackTrace();
        }
        String urlString = getLocalUrl(inetAddress);
        JSONObject result = EspBaseApiUtil.PostForJson(urlString, deviceBssid, Request);
        log.debug(Thread.currentThread().toString() + "##doCommandDeviceNewConfigureLocal(deviceBssid=[" + deviceBssid
            + "],deviceSsid=[" + deviceSsid + "],deviceWifiCipherType=[" + deviceWifiCipherType + "],devicePassword=["
            + devicePassword + "],randomToken=[" + randomToken + "]): " + result);
        return true;
    }
    
}
