package com.espressif.iot.command.device.remote;

import java.net.InetAddress;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.espressif.iot.base.api.EspBaseApiUtil;
import com.espressif.iot.type.device.status.EspStatusRemote;
import com.espressif.iot.type.device.status.IEspStatusRemote;

public class EspCommandRemoteGetStatusLocal implements IEspCommandRemoteGetStatusLocal
{
    private final static Logger log = Logger.getLogger(EspCommandRemoteGetStatusLocal.class);
    
    private String getRemoteLocalUrl(InetAddress inetAddress)
    {
        return "http:/" + inetAddress + "/" + "config?command=remote";
    }
    
    private IEspStatusRemote getCurrentRemoteStatus(InetAddress inetAddress, String deviceBssid, String router)
    {
        String uriString = getRemoteLocalUrl(inetAddress);
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
            JSONObject rgb = jo.getJSONObject(Remote);
            int address = rgb.getInt(Addr);
            int cmd = rgb.getInt(Cmd);
            int rep = rgb.getInt(Rep);
            IEspStatusRemote status = new EspStatusRemote();
            status.setAddress(address);
            status.setCommand(cmd);
            status.setRepeat(rep);
            return status;
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        return null;
    }
    
    @Override
    public IEspStatusRemote doCommandRemoteGetStatusLocal(InetAddress inetAddress)
    {
        IEspStatusRemote result = getCurrentRemoteStatus(inetAddress, null, null);
        log.debug(Thread.currentThread().toString() + "##doCommandRemoteGetStatusLocal(inetAddress=[" + inetAddress
            + "]): " + result);
        return result;
    }

    @Override
    public IEspStatusRemote doCommandRemoteGetStatusLocal(InetAddress inetAddress, String deviceBssid, String router)
    {
        IEspStatusRemote result = getCurrentRemoteStatus(inetAddress, deviceBssid, router);
        log.debug(Thread.currentThread().toString() + "##doCommandRemoteGetStatusLocal(inetAddress=[" + inetAddress
            + "],deviceBssid=[" + deviceBssid + "],router=[" + router + "]): " + result);
        return result;
    }
    
}
