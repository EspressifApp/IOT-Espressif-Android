package com.espressif.iot.command.device.light;

import org.apache.http.HttpStatus;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.espressif.iot.base.api.EspBaseApiUtil;
import com.espressif.iot.command.device.plug.EspCommandPlugGetStatusInternet;
import com.espressif.iot.type.device.status.EspStatusLight;
import com.espressif.iot.type.device.status.IEspStatusLight;
import com.espressif.iot.type.net.HeaderPair;

public class EspCommandLightGetStatusInternet implements IEspCommandLightGetStatusInternet
{
    
    private final static Logger log = Logger.getLogger(EspCommandPlugGetStatusInternet.class);
    
    private IEspStatusLight getCurrentLightStatus(String deviceKey)
    {
        String headerKey = Authorization;
        String headerValue = Token + " " + deviceKey;
        JSONObject result = null;
        HeaderPair header = new HeaderPair(headerKey, headerValue);
        result = EspBaseApiUtil.Get(URL, header);
        if (result == null)
        {
            return null;
        }
        try
        {
            int status = -1;
            try
            {
                if (result != null)
                {
                    status = Integer.parseInt(result.getString(Status));
                }
            }
            catch (JSONException e)
            {
                e.printStackTrace();
            }
            if (status == HttpStatus.SC_OK)
            {
                JSONObject data = result.getJSONObject(Datapoint);
                int freq = data.getInt(X);
                int red = data.getInt(Y);
                int green = data.getInt(Z);
                int blue = data.getInt(K);
                IEspStatusLight statusLight = new EspStatusLight();
                statusLight.setFreq(freq);
                statusLight.setRed(red);
                statusLight.setGreen(green);
                statusLight.setBlue(blue);
                return statusLight;
            }
            else
            {
                return null;
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
            return null;
        }
    }
    
    @Override
    public IEspStatusLight doCommandLightGetStatusInternet(String deviceKey)
    {
        IEspStatusLight result = getCurrentLightStatus(deviceKey);
        log.debug(Thread.currentThread().toString() + "##doCommandLightGetStatusInternet(deviceKey=[" + deviceKey
            + "]): " + result);
        return result;
    }
    
}
