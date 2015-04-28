package com.espressif.iot.command.device.New;

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
    public boolean doCommandDeviceNewConfigureLocal(String deviceSsid, WifiCipherType deviceWifiCipherType,
        String devicePassword, String apSsid, WifiCipherType apWifiCipherType, String apPassword, String randomToken)
        throws InterruptedException
    {
        // 1. connect to device
        boolean connectDeviceResult = EspBaseApiUtil.connect(deviceSsid, deviceWifiCipherType, devicePassword);
        // 2. post configure info
        if (connectDeviceResult)
        {
            log.error("##doCommandDeviceNewConfigureLocal(): connectDeviceResult = true");
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
            JSONObject result = EspBaseApiUtil.Post(URL.replace("192.168.4.1", gateWay), Request);
            log.debug(Thread.currentThread().toString() + "##doCommandDeviceNewConfigureLocal(deviceSsid=["
                + deviceSsid + "],deviceWifiCipherType=[" + deviceWifiCipherType + "],devicePassword=["
                + devicePassword + "],apSsid=[" + apSsid + "],apWifiCipherType=[" + apWifiCipherType + "],apPassword=["
                + apPassword + "],randomToken=[" + randomToken + "]): " + result);
            return true;
        }
        else
        {
            log.warn(Thread.currentThread().toString() + "##doCommandDeviceNewConfigureLocal(deviceSsid=[" + deviceSsid
                + "],deviceWifiCipherType=[" + deviceWifiCipherType + "],devicePassword=[" + devicePassword
                + "],apSsid=[" + apSsid + "],apWifiCipherType=[" + apWifiCipherType + "],apPassword=[" + apPassword
                + "],randomToken=[" + randomToken + "]): " + false);
            return false;
        }
        
    }
    
    @Override
    public boolean doCommandMeshDeviceNewConfigureLocal(String deviceSsid, WifiCipherType deviceWifiCipherType,
        String devicePassword, String randomToken)
        throws InterruptedException
    {
        // 1. connect to device
        boolean connectDeviceResult = EspBaseApiUtil.connect(deviceSsid, deviceWifiCipherType, devicePassword);
        // 2. post configure info
        if (connectDeviceResult)
        {
            log.error("##doCommandDeviceNewConfigureLocal(): connectDeviceResult = true");
            JSONObject Content = new JSONObject();
            JSONObject Connect_Station = new JSONObject();
            JSONObject Station = new JSONObject();
            JSONObject Request = new JSONObject();
            String gateWay = EspApplication.sharedInstance().getGateway();
            
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
            JSONObject result = EspBaseApiUtil.Post(URL.replace("192.168.4.1", gateWay), Request);
            log.debug(Thread.currentThread().toString() + "##doCommandDeviceNewConfigureLocal(deviceSsid=["
                + deviceSsid + "],deviceWifiCipherType=[" + deviceWifiCipherType + "],devicePassword=["
                + devicePassword + "],randomToken=[" + randomToken + "]): " + result);
            return true;
        }
        else
        {
            log.warn(Thread.currentThread().toString() + "##doCommandDeviceNewConfigureLocal(deviceSsid=[" + deviceSsid
                + "],deviceWifiCipherType=[" + deviceWifiCipherType + "],devicePassword=[" + devicePassword
                + "],randomToken=[" + randomToken + "]): " + false);
            return false;
        }
        
    }
    
}
