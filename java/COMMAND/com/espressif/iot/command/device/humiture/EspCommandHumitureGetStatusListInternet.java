package com.espressif.iot.command.device.humiture;

import java.util.List;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.espressif.iot.command.device.sensor.EspCommandSensorGetStatusListInternet;
import com.espressif.iot.type.device.status.EspStatusHumiture;
import com.espressif.iot.type.device.status.IEspStatusHumiture;
import com.espressif.iot.type.device.status.IEspStatusSensor;
import com.espressif.iot.util.TimeUtil;

public class EspCommandHumitureGetStatusListInternet extends EspCommandSensorGetStatusListInternet implements
    IEspCommandHumitureGetStatusListInternet
{
    private final static Logger log = Logger.getLogger(EspCommandHumitureGetStatusListInternet.class);
    
    @Override
    public IEspStatusSensor parseJson(JSONObject json)
    {
        try
        {
            long at = json.getLong(At) * TimeUtil.ONE_SECOND_LONG_VALUE;
            double x = json.getDouble(X);
            double y = json.getDouble(Y);
            IEspStatusHumiture statusHumiture = new EspStatusHumiture();
            statusHumiture.setAt(at);
            statusHumiture.setX(x);
            statusHumiture.setY(y);
            return statusHumiture;
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
    public List<IEspStatusHumiture> doCommandHumitureGetStatusListInternet(String deviceKey, long startTimestamp,
        long endTimestamp)
    {
        List<IEspStatusHumiture> result =
            (List<IEspStatusHumiture>)(List<?>)super.doCommandSensorGetStatusListInternet(deviceKey,
                startTimestamp,
                endTimestamp);
        log.debug(Thread.currentThread().toString() + "##doCommandHumitureGetStatusListInternet(deviceKey=["
            + deviceKey + "],startTimestamp=[" + TimeUtil.getDateStr(startTimestamp, null) + "],endTimestamp=["
            + TimeUtil.getDateStr(endTimestamp, null) + "]): receive " + result.size());
        return result;
    }
}
