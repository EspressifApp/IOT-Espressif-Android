package com.espressif.iot.command.device.sensor;

import org.apache.http.HttpStatus;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.espressif.iot.base.api.EspBaseApiUtil;
import com.espressif.iot.type.device.status.IEspStatusSensor;
import com.espressif.iot.type.net.HeaderPair;
import com.espressif.iot.util.TimeUtil;

public abstract class EspCommandSensorGetStatusInternet implements IEspCommandSensorGetStatusInternet
{
    private final static Logger log = Logger.getLogger(EspCommandSensorGetStatusInternet.class);
    
    private IEspStatusSensor getCurrentSensorStatus(String deviceKey)
    {
        String headerKey = Authorization;
        String headerValue = Token + " " + deviceKey;
        HeaderPair header1 = new HeaderPair(headerKey, headerValue);
        HeaderPair header2 = new HeaderPair(Time_Zone, Epoch);
        
        String url =
            this.getUrl() + "?offset=" + 0 + "&row_count=" + 1 + "&start=" + TimeUtil.getOriginTime() + "&end="
                + TimeUtil.getLastTime();
        JSONObject jsonObjectResult = EspBaseApiUtil.Get(url, header1, header2);
        
        if (jsonObjectResult != null)
        {
            try
            {
                int status = Integer.parseInt(jsonObjectResult.getString(Status));
                if (status == HttpStatus.SC_OK)
                {
                    JSONArray jsonArray = jsonObjectResult.getJSONArray(Datapoints);
                    JSONObject jsonObject = jsonArray.getJSONObject(0);
                    return this.parseJson(jsonObject);
                }
                else
                {
                    return null;
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        return null;
    }
    
    @Override
    public IEspStatusSensor doCommandSensorGetStatusInternet(String deviceKey)
    {
        IEspStatusSensor result = getCurrentSensorStatus(deviceKey);
        log.debug(Thread.currentThread().toString() + "##doCommandSensorGetStatusInternet(deviceKey=[" + deviceKey
            + "]): " + result);
        return result;
    }
    
}
