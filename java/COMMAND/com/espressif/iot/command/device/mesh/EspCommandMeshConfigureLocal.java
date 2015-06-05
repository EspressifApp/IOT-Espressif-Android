package com.espressif.iot.command.device.mesh;

import java.net.InetAddress;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.espressif.iot.base.api.EspBaseApiUtil;

public class EspCommandMeshConfigureLocal implements IEspCommandMeshConfigureLocal
{
    
    private final static Logger log = Logger.getLogger(EspCommandMeshConfigureLocal.class);
    
    @Override
    public String getLocalUrl(InetAddress inetAddress)
    {
        return "http://" + inetAddress.getHostAddress() + "/" + "config?command=wifi";
    }
    
    @Override
    public boolean doCommandMeshConfigureLocal(String router, String deviceBssid, MeshMode meshMode,
        InetAddress inetAddress, String apSsid, String apPassword, String randomToken)
    {
        String urlString = getLocalUrl(inetAddress);
        JSONObject Content = new JSONObject();
        JSONObject Connect_Station = new JSONObject();
        JSONObject Station = new JSONObject();
        JSONObject Request = new JSONObject();
        try
        {
            if (apSsid != null && meshMode == MeshMode.MESH_ONLINE)
            {
                if (apPassword != null)
                {
                    Content.put("password", apPassword);
                }
                else
                {
                    Content.put("password", "");
                }
                Content.put("ssid", apSsid);
            }
            if (randomToken != null)
            {
                Content.put("token", randomToken);
            }
            Content.put("mesh", meshMode.ordinal());
            
            Connect_Station.put("Connect_Station", Content);
            
            Station.put("Station", Connect_Station);
            
            Request.put("Request", Station);
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        JSONObject result = EspBaseApiUtil.PostForJson(urlString, router, deviceBssid, Request);
        boolean isSuc = result != null;
        log.debug(Thread.currentThread().toString() + "##doCommandMeshConfigureLocal(router=[" + router
            + "],deviceBssid=[" + deviceBssid + "],meshMode=[" + meshMode + "],inetAddress=[" + inetAddress
            + "],apSsid=[" + apSsid + "],apPassword=[" + apPassword + "]): " + isSuc);
        return isSuc;
    }
    
}
