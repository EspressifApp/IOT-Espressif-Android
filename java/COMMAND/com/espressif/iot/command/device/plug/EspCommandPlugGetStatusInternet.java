package com.espressif.iot.command.device.plug;

import org.apache.http.HttpStatus;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.espressif.iot.base.api.EspBaseApiUtil;
import com.espressif.iot.type.device.status.EspStatusPlug;
import com.espressif.iot.type.device.status.IEspStatusPlug;
import com.espressif.iot.type.net.HeaderPair;

public class EspCommandPlugGetStatusInternet implements IEspCommandPlugGetStatusInternet
{
    
    private final static Logger log = Logger.getLogger(EspCommandPlugGetStatusInternet.class);
    
    private EspStatusPlug getCurrentPlugStatus(String deviceKey)
    {
        
        String headerKey = Authorization;
        String headerValue = Token + " " + deviceKey;
        
        JSONObject result = null;
        
        HeaderPair header = new HeaderPair(headerKey, headerValue);
        result = EspBaseApiUtil.Get(URL, header);
        
        int status = -1;
        try
        {
            if (result != null)
                status = Integer.parseInt(result.getString(Status));
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        if (status == HttpStatus.SC_OK)
        {
            JSONObject data;
            try
            {
                data = result.getJSONObject(Datapoint);
                int isOn = data.getInt(X);
                EspStatusPlug statusPlug = new EspStatusPlug();
                statusPlug.setIsOn(isOn == 1);
                return statusPlug;
            }
            catch (JSONException e)
            {
                e.printStackTrace();
                return null;
            }
        }
        else
        {
            return null;
        }
    }
    
    @Override
    public IEspStatusPlug doCommandPlugGetStatusInternet(String deviceKey)
    {
        IEspStatusPlug result = getCurrentPlugStatus(deviceKey);
        log.debug(Thread.currentThread().toString() + "##doCommandPlugGetStatusInternet(deviceKey=[" + deviceKey
            + "]): " + result);
        return result;
    }
    
}
