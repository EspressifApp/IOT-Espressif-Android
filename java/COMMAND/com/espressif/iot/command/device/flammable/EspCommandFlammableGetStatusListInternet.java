package com.espressif.iot.command.device.flammable;

import java.util.List;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.espressif.iot.command.device.sensor.EspCommandSensorGetStatusListInternet;
import com.espressif.iot.type.device.status.EspStatusFlammable;
import com.espressif.iot.type.device.status.IEspStatusFlammable;
import com.espressif.iot.type.device.status.IEspStatusSensor;
import com.espressif.iot.util.TimeUtil;

public class EspCommandFlammableGetStatusListInternet extends EspCommandSensorGetStatusListInternet implements
    IEspCommandFlammableGetStatusListInternet
{
    private final static Logger log = Logger.getLogger(EspCommandFlammableGetStatusListInternet.class);
    
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
    
    @SuppressWarnings("unchecked")
    @Override
    public List<IEspStatusFlammable> doCommandFlammableGetStatusListInternet(String deviceKey, long startTimestamp,
        long endTimestamp)
    {
        List<IEspStatusFlammable> result =
            (List<IEspStatusFlammable>)(List<?>)super.doCommandSensorGetStatusListInternet(deviceKey,
                startTimestamp,
                endTimestamp);
        log.debug(Thread.currentThread().toString() + "##doCommandFlammableGetStatusListInternet(deviceKey=["
            + deviceKey + "],startTimestamp=[" + TimeUtil.getDateStr(startTimestamp, null) + "],endTimestamp=["
            + TimeUtil.getDateStr(endTimestamp, null) + "]): receive " + result.size());
        return result;
    }
    
}
