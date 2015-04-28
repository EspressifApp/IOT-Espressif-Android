package com.espressif.iot.command.device.voltage;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.espressif.iot.command.device.sensor.EspCommandSensorGetStatusInternet;
import com.espressif.iot.command.voltage.IEspCommandVoltageGetStatusInternet;
import com.espressif.iot.type.device.status.EspStatusVoltage;
import com.espressif.iot.type.device.status.IEspStatusSensor;
import com.espressif.iot.type.device.status.IEspStatusVoltage;
import com.espressif.iot.util.TimeUtil;

public class EspCommandVoltageGetStatusInternet extends EspCommandSensorGetStatusInternet implements
    IEspCommandVoltageGetStatusInternet
{
    
    private final static Logger log = Logger.getLogger(EspCommandVoltageGetStatusInternet.class);
    
    private final static int TOTAL_INDEX = 20;
    
    private final static long EACH_INDEX_INTERVAL = 1 * TimeUtil.ONE_MINUTE_LONG_VALUE;
    
    // for the info in the server is like these:
    // #                        at                      x                       y
    // 9397473                  2015-03-15 19:01:26     19                      65535
    // 9397472                  2015-03-15 19:01:26     18                      65535
    // ......
    // 9397472                  2015-03-15 19:01:26     1                       65535
    // 9397472                  2015-03-15 19:01:26     0                       65535
    // 9397445                  2015-03-15 18:59:22     19                      65535
    // ......
    @Override
    public IEspStatusSensor parseJson(JSONObject json)
    {
        try
        {
            long at = json.getLong(At) * TimeUtil.ONE_SECOND_LONG_VALUE;
            // x store the index of the data, the device upload 20 data together after 20 minutes
            double x = json.getDouble(X);
            double y = json.getDouble(Y);
            
            IEspStatusVoltage statusVoltage = new EspStatusVoltage();
            // update the time 
            at = (long)(at - (TOTAL_INDEX - 1 - x) * EACH_INDEX_INTERVAL);
            statusVoltage.setAt(at);
            // voltage value is stored in y,
            // the voltage value =  y/1024.0
            statusVoltage.setX(y / 1024.0);
            
            return statusVoltage;
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

    @Override
    public IEspStatusVoltage doCommandVoltageGetStatusInternet(String deviceKey)
    {
        IEspStatusVoltage result = (IEspStatusVoltage)super.doCommandSensorGetStatusInternet(deviceKey);
        log.debug(Thread.currentThread().toString() + "##doCommandVoltageGetStatusInternet(deviceKey=[" + deviceKey
            + "]): " + result);
        return result;
    }
    
}
