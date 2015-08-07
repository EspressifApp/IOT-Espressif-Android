package com.espressif.iot.command.device.plug;

import java.net.InetAddress;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.espressif.iot.base.api.EspBaseApiUtil;
import com.espressif.iot.type.device.status.EspStatusPlug;
import com.espressif.iot.type.device.status.IEspStatusPlug;

public class EspCommandPlugGetStatusLocal implements IEspCommandPlugGetStatusLocal
{
    private final static Logger log = Logger.getLogger(EspCommandPlugGetStatusLocal.class);
    
    @Override
    public String getLocalUrl(InetAddress inetAddress)
    {
        return "http://" + inetAddress.getHostAddress() + "/" + "config?command=switch";
    }
    
    private IEspStatusPlug getCurrentPlugStatus2(InetAddress inetAddress, String deviceBssid, boolean isMeshDevice)
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
            JSONObject responseJson = jo.getJSONObject(Response);
            int on = responseJson.getInt(Status);
            IEspStatusPlug status = new EspStatusPlug();
            status.setIsOn(on == 1);
            return status;
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        return null;
    }
    
    @Override
    public IEspStatusPlug doCommandPlugGetStatusLocal(InetAddress inetAddress)
    {
        IEspStatusPlug result = getCurrentPlugStatus2(inetAddress, null, false);
        log.debug(Thread.currentThread().toString() + "##doCommandPlugGetStatusLocal(inetAddress=[" + inetAddress
            + "]): " + result);
        return result;
    }
    
    @Override
    public IEspStatusPlug doCommandPlugGetStatusLocal(InetAddress inetAddress, String deviceBssid, boolean isMeshDevice)
    {
        IEspStatusPlug result = getCurrentPlugStatus2(inetAddress, deviceBssid, isMeshDevice);
        log.debug(Thread.currentThread().toString() + "##doCommandPlugGetStatusLocal(inetAddress=[" + inetAddress
            + "],deviceBssid=[" + deviceBssid + "],isMeshDevice=[" + isMeshDevice + "])");
        return result;
    }
    
}
