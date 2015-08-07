package com.espressif.iot.command.device.plugs;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.espressif.iot.base.api.EspBaseApiUtil;
import com.espressif.iot.type.device.EspPlugsAperture;
import com.espressif.iot.type.device.status.EspStatusPlugs;
import com.espressif.iot.type.device.status.IEspStatusPlugs;
import com.espressif.iot.type.device.status.IEspStatusPlugs.IAperture;

public class EspCommandPlugsGetStatusLocal implements IEspCommandPlugsGetStatusLocal
{
    @Override
    public String getLocalUrl(InetAddress inetAddress)
    {
        return "http://" + inetAddress.getHostAddress() + "/" + "config?command=plugs";
    }
    
    @Override
    public IEspStatusPlugs doCommandPlugsGetStatusLocal(InetAddress inetAddress, String deviceBssid,
        boolean isMeshDevice)
    {
        String url = getLocalUrl(inetAddress);
        JSONObject resultJSON = null;
        if (deviceBssid == null || !isMeshDevice)
        {
            resultJSON = EspBaseApiUtil.Get(url);
        }
        else
        {
            resultJSON = EspBaseApiUtil.GetForJson(url, deviceBssid);
        }
        
        if (resultJSON == null)
        {
            return null;
        }
        
        try
        {
            IEspStatusPlugs plugsStatus = new EspStatusPlugs();
            List<IAperture> apertures = new ArrayList<IAperture>();
            JSONObject statusJSON = resultJSON.getJSONObject(KEY_PLUGS_STATUS);
            int count = statusJSON.getInt(KEY_APERTURE_COUNT);
            int valueSum = statusJSON.getInt(KEY_PLUGS_VALUE);
            for (int i = 0; i < count; i++)
            {
                IAperture aperture = new EspPlugsAperture(i);
                aperture.setTitle("Plug " + i);
                boolean isOn = (valueSum >> i) % 2 == 1;
                aperture.setOn(isOn);
                
                apertures.add(aperture);
            }
            
            plugsStatus.setStatusApertureList(apertures);
            return plugsStatus;
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        
        return null;
    }
    
}
