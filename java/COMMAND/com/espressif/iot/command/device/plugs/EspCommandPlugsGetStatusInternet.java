package com.espressif.iot.command.device.plugs;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.json.JSONObject;

import com.espressif.iot.base.api.EspBaseApiUtil;
import com.espressif.iot.type.device.EspPlugsAperture;
import com.espressif.iot.type.device.status.EspStatusPlugs;
import com.espressif.iot.type.device.status.IEspStatusPlugs;
import com.espressif.iot.type.device.status.IEspStatusPlugs.IAperture;
import com.espressif.iot.type.net.HeaderPair;

public class EspCommandPlugsGetStatusInternet implements IEspCommandPlugsGetStatusInternet
{
    
    @Override
    public IEspStatusPlugs doCommandPlugsGetStatusInternet(String deviceKey)
    {
        String headerKey = Authorization;
        String headerValue = Token + " " + deviceKey;
        HeaderPair header = new HeaderPair(headerKey, headerValue);
        
        JSONObject resultJSON = EspBaseApiUtil.Get(URL, header);
        if (resultJSON == null)
        {
            return null;
        }
        
        try
        {
            int status = resultJSON.getInt(Status);
            if (status != HttpStatus.SC_OK)
            {
                return null;
            }
            
            IEspStatusPlugs plugsStatus = new EspStatusPlugs();
            List<IAperture> apertures = new ArrayList<IAperture>();
            JSONObject dataJSON = resultJSON.getJSONObject(Datapoint);
            int valueSum = dataJSON.getInt(X);
            int count = dataJSON.getInt(Y);
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
