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
    
    @Override
    public String getLocalUrl(InetAddress inetAddress)
    {
        return "http://" + inetAddress.getHostAddress() + "/" + "config?command=remote";
    }
    
    private IEspStatusRemote getCurrentRemoteStatus2(InetAddress inetAddress, String deviceBssid, boolean isMeshDevice)
    {
        String uriString = getLocalUrl(inetAddress);
        JSONObject jo = null;
        if (deviceBssid == null || !isMeshDevice)
        {
            jo = EspBaseApiUtil.Get(uriString);
        }
        else
        {
            jo = EspBaseApiUtil.GetForJson(uriString, deviceBssid);
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
        IEspStatusRemote result = getCurrentRemoteStatus2(inetAddress, null, false);
        log.debug(Thread.currentThread().toString() + "##doCommandRemoteGetStatusLocal(inetAddress=[" + inetAddress
            + "]): " + result);
        return result;
    }
    
    @Override
    public IEspStatusRemote doCommandRemoteGetStatusLocal(InetAddress inetAddress, String deviceBssid,
        boolean isMeshDevice)
    {
        IEspStatusRemote result = getCurrentRemoteStatus2(inetAddress, deviceBssid, isMeshDevice);
        log.debug(Thread.currentThread().toString() + "##doCommandRemoteGetStatusLocal(inetAddress=[" + inetAddress
            + "],deviceBssid=[" + deviceBssid + "],isMeshDevice=[" + isMeshDevice + "])");
        return result;
    }
    
}
