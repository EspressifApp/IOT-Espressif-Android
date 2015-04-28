package com.espressif.iot.command.device.flammable;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.espressif.iot.command.device.sensor.EspCommandSensorGetStatusInternet;
import com.espressif.iot.type.device.status.EspStatusFlammable;
import com.espressif.iot.type.device.status.IEspStatusFlammable;
import com.espressif.iot.type.device.status.IEspStatusSensor;
import com.espressif.iot.util.TimeUtil;

public class EspCommandFlammableGetStatusInternet extends EspCommandSensorGetStatusInternet implements
    IEspCommandFlammableGetStatusInternet
{
    
    private final static Logger log = Logger.getLogger(EspCommandFlammableGetStatusInternet.class);
    
    @Override
    public IEspStatusFlammable doCommandFlammableGetStatusInternet(String deviceKey)
    
    {
        IEspStatusFlammable result = (IEspStatusFlammable)super.doCommandSensorGetStatusInternet(deviceKey);
        log.debug(Thread.currentThread().toString() + "##doCommandFlammableGetStatusInternet(deviceKey=[" + deviceKey
            + "]): " + result);
        return result;
    }
    
    @Override
    public IEspStatusSensor parseJson(JSONObject json)
    {
        try
        {
            long at = json.getLong(At) * TimeUtil.ONE_SECOND_LONG_VALUE;
            double x = json.getDouble(X);
            IEspStatusFlammable statusFlammable = new EspStatusFlammable();
            statusFlammable.setAt(at);
            statusFlammable.setX(x);
            return statusFlammable;
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        return null;
    }
    
    @Override
    public String getUrl()
    {
        return URL;
    }
    
}
