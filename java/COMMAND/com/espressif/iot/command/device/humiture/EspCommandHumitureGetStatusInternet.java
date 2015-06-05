package com.espressif.iot.command.device.humiture;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.espressif.iot.command.device.sensor.EspCommandSensorGetStatusInternet;
import com.espressif.iot.type.device.status.EspStatusHumiture;
import com.espressif.iot.type.device.status.IEspStatusHumiture;
import com.espressif.iot.type.device.status.IEspStatusSensor;
import com.espressif.iot.util.TimeUtil;

public class EspCommandHumitureGetStatusInternet extends EspCommandSensorGetStatusInternet implements
    IEspCommandHumitureGetStatusInternet
{
    
    private final static Logger log = Logger.getLogger(EspCommandHumitureGetStatusInternet.class);
    
    @Override
    public IEspStatusHumiture doCommandHumitureGetStatusInternet(String deviceKey)
    
    {
        IEspStatusHumiture result = (IEspStatusHumiture)super.doCommandSensorGetStatusInternet(deviceKey);
        log.debug(Thread.currentThread().toString() + "##doCommandHumitureGetStatusInternet(deviceKey=[" + deviceKey
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
    
}
