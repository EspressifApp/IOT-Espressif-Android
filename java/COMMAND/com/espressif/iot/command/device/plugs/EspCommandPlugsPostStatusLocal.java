package com.espressif.iot.command.device.plugs;

import java.net.InetAddress;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.espressif.iot.base.api.EspBaseApiUtil;
import com.espressif.iot.type.device.status.IEspStatusPlugs;
import com.espressif.iot.type.device.status.IEspStatusPlugs.IAperture;

public class EspCommandPlugsPostStatusLocal implements IEspCommandPlugsPostStatusLocal
{
    @Override
    public String getLocalUrl(InetAddress inetAddress)
    {
        return "http://" + inetAddress.getHostAddress() + "/" + "config?command=plugs";
    }
    
    @Override
    public boolean doCommandPlugsPostStatusLocal(InetAddress inetAddress, IEspStatusPlugs status, String deviceBssid,
        boolean isMeshDevice)
    {
        String url = getLocalUrl(inetAddress);
        
        JSONObject params = new JSONObject();
        JSONObject statusJSON = new JSONObject();
        try
        {
            List<IAperture> apertures = status.getStatusApertureList();
            int valueSum = 0;
            for (IAperture aperture : apertures)
            {
                int value;
                if (aperture.isOn())
                {
                    value = 1 << aperture.getId();
                }
                else
                {
                    value = 0;
                }
                valueSum += value;
            }
            statusJSON.put(KEY_PLUGS_VALUE, valueSum);
            statusJSON.put(KEY_APERTURE_COUNT, apertures.size());
            params.put(KEY_PLUGS_STATUS, statusJSON);
        }
        catch (JSONException e1)
        {
            e1.printStackTrace();
        }
        
        JSONObject result;
        if (deviceBssid == null || !isMeshDevice)
        {
            result = EspBaseApiUtil.Post(url, params);
        }
        else
        {
            result = EspBaseApiUtil.PostForJson(url, deviceBssid, params);
        }
        
        return result != null;
    }
}
