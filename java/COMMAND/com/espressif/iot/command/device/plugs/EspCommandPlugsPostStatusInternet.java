package com.espressif.iot.command.device.plugs;

import java.util.List;

import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.json.JSONObject;

import com.espressif.iot.base.api.EspBaseApiUtil;
import com.espressif.iot.type.device.status.IEspStatusPlugs;
import com.espressif.iot.type.device.status.IEspStatusPlugs.IAperture;
import com.espressif.iot.type.net.HeaderPair;

public class EspCommandPlugsPostStatusInternet implements IEspCommandPlugsPostStatusInternet
{
    
    @Override
    public boolean doCommandPlugsPostStatusInternet(String deviceKey, IEspStatusPlugs status)
    {
        String headerKey = Authorization;
        String headerValue = Token + " " + deviceKey;
        HeaderPair header = new HeaderPair(headerKey, headerValue);
        
        JSONObject params = new JSONObject();
        JSONObject dataJSON = new JSONObject();
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
            dataJSON.put(X, valueSum);
            params.put(Datapoint, dataJSON);
        }
        catch (JSONException e1)
        {
            e1.printStackTrace();
        }
        
        String url = URL;
        JSONObject result = EspBaseApiUtil.Post(url, params, header);
        if (result == null)
        {
            return false;
        }
        
        try
        {
            int httpStatus = result.getInt(Status);
            return httpStatus == HttpStatus.SC_OK;
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        
        return false;
    }
    
}
