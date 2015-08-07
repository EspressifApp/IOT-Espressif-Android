package com.espressif.iot.command.device.remote;

import java.net.InetAddress;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.espressif.iot.base.api.EspBaseApiUtil;
import com.espressif.iot.type.device.status.IEspStatusRemote;

public class EspCommandRemotePostStatusLocal implements IEspCommandRemotePostStatusLocal
{
    private final static Logger log = Logger.getLogger(EspCommandRemotePostStatusLocal.class);
    
    @Override
    public String getLocalUrl(InetAddress inetAddress)
    {
        return "http://" + inetAddress.getHostAddress() + "/" + "config?command=remote";
    }
    
    private JSONObject getRequestJSONObject(IEspStatusRemote statusRemote)
    {
        JSONObject request = new JSONObject();
        JSONObject remote = new JSONObject();
        try
        {
            remote.put(Addr, statusRemote.getAddress());
            remote.put(Cmd, statusRemote.getCommand());
            remote.put(Rep, statusRemote.getRepeat());
            request.put(Remote, remote);
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        return request;
    }
    
    private boolean postRemoteStatus(InetAddress inetAddress, IEspStatusRemote statusRemote, String deviceBssid)
    {
        String uriString = getLocalUrl(inetAddress);
        JSONObject jsonObject;
        jsonObject = getRequestJSONObject(statusRemote);
        JSONObject result = null;
        if (deviceBssid == null)
        {
            EspBaseApiUtil.Post(uriString, jsonObject);
        }
        else
        {
            EspBaseApiUtil.PostForJson(uriString, deviceBssid, jsonObject);
        }
        return (result != null);
    }
    
    private boolean postRemoteStatus2(InetAddress inetAddress, IEspStatusRemote statusRemote, String deviceBssid,
        boolean isMeshDevice)
    {
        String uriString = getLocalUrl(inetAddress);
        JSONObject jsonObject;
        jsonObject = getRequestJSONObject(statusRemote);
        JSONObject result = null;
        if (deviceBssid == null || !isMeshDevice)
        {
            EspBaseApiUtil.Post(uriString, jsonObject);
        }
        else
        {
            EspBaseApiUtil.PostForJson(uriString, deviceBssid, jsonObject);
        }
        return (result != null);
    }
    
    @Override
    public boolean doCommandRemotePostStatusLocal(InetAddress inetAddress, IEspStatusRemote statusRemote)
    {
        boolean result = postRemoteStatus(inetAddress, statusRemote, null);
        log.debug(Thread.currentThread().toString() + "##doCommandRemotePostStatusInternet(inetAddress=[" + inetAddress
            + "],statusRemote=[" + statusRemote + "]): " + result);
        return result;
    }
    
    @Override
    public boolean doCommandRemotePostStatusLocal(InetAddress inetAddress, IEspStatusRemote statusRemote,
        String deviceBssid, boolean isMeshDevice)
    {
        boolean result = postRemoteStatus2(inetAddress, statusRemote, deviceBssid, isMeshDevice);
        log.debug(Thread.currentThread().toString() + "##doCommandRemotePostStatusLocal(inetAddress=[" + inetAddress
            + "],statusRemote=[" + statusRemote + "],deviceBssid=[" + deviceBssid + "],isMeshDevice=[" + isMeshDevice
            + "]): " + result);
        return result;
    }
}
