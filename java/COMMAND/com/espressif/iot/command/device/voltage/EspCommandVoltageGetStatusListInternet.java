package com.espressif.iot.command.device.voltage;

import java.util.List;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.espressif.iot.command.device.sensor.EspCommandSensorGetStatusListInternet;
import com.espressif.iot.command.voltage.IEspCommandVoltageGetStatusListInternet;
import com.espressif.iot.type.device.status.EspStatusVoltage;
import com.espressif.iot.type.device.status.IEspStatusSensor;
import com.espressif.iot.type.device.status.IEspStatusVoltage;
import com.espressif.iot.util.TimeUtil;

public class EspCommandVoltageGetStatusListInternet extends EspCommandSensorGetStatusListInternet implements
    IEspCommandVoltageGetStatusListInternet
{
    private final static Logger log = Logger.getLogger(EspCommandVoltageGetStatusListInternet.class);
    
    private final static int TOTAL_INDEX = 20;
    
    private final static long EACH_INDEX_INTERVAL = 1 * TimeUtil.ONE_MINUTE_LONG_VALUE;
    
    // for the info in the server is like these:
    // # at x y
    // 9397473 2015-03-15 19:01:26 19 65535
    // 9397472 2015-03-15 19:01:26 18 65535
    // ......
    // 9397472 2015-03-15 19:01:26 1 65535
    // 9397472 2015-03-15 19:01:26 0 65535
    // 9397445 2015-03-15 18:59:22 19 65535
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
            // the voltage value = y/1024.0
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
    
    @SuppressWarnings("unchecked")
    @Override
    public List<IEspStatusVoltage> doCommandVoltageGetStatusListInternet(String deviceKey, long startTimestamp,
        long endTimestamp)
    {
        List<IEspStatusVoltage> result =
            (List<IEspStatusVoltage>)(List<?>)super.doCommandSensorGetStatusListInternet(deviceKey,
                startTimestamp,
                endTimestamp);
        log.debug(Thread.currentThread().toString() + "##doCommandVoltageGetStatusListInternet(deviceKey=[" + deviceKey
            + "],startTimestamp=[" + TimeUtil.getDateStr(startTimestamp, null) + "],endTimestamp=["
            + TimeUtil.getDateStr(endTimestamp, null) + "]): receive " + result.size());
        return result;
    }
    
}
