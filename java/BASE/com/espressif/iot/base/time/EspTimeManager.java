package com.espressif.iot.base.time;

import org.apache.http.HttpStatus;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import com.espressif.iot.base.api.EspBaseApiUtil;
import com.espressif.iot.object.IEspSingletonObject;
import com.espressif.iot.util.TimeUtil;

public class EspTimeManager implements IEspTimeManager, IEspSingletonObject
{
    
    /*
     * Singleton lazy initialization start
     */
    private EspTimeManager()
    {
        TimeListenManager.getInstance().registerReceiver();
    }
    
    private static class InstanceHolder
    {
        static EspTimeManager instance = new EspTimeManager();
    }
    
    public static EspTimeManager getInstance()
    {
        return InstanceHolder.instance;
    }
    
    /*
     * Singleton lazy initialization end
     */
    private static final Logger log = Logger.getLogger(EspTimeManager.class);
    
    /**
     * whether the timeStamp is today
     * 
     * @param UTCTimeStamp the UTC time stamp used to check whehter it is today
     * @return whether UTCTimeStamp is today
     */
    public boolean isDateToday(long UTCTimeStamp)
    {
        long currentTimeLong = getUTCTimeLong();
        if (currentTimeLong == Long.MIN_VALUE)
        {
            currentTimeLong = TimeUtil.getSystemCurrentTimeLong();
        }
        return TimeUtil.isTheSameDate(UTCTimeStamp, currentTimeLong);
    }
    
    /**
     * 
     * @return the UTC time, if fail it will return {@link CONSTANTS#UTC_TIME_INVALID}
     */
    public long getUTCTimeLong()
    {
        UITimeManager uiTimeManager = UITimeManager.getInstance();
        long timeLong = uiTimeManager.getUTCTimeLong();
        if (timeLong == Long.MIN_VALUE)
        {
            timeLong = getDateStrFromServer();
            
        }
        return timeLong;
    }
    
    private long getDateStrFromServer()
    {
        JSONObject result = EspBaseApiUtil.Get(Url);
        
        if (result == null)
        {
            return Long.MIN_VALUE;
        }
        long dateLong = Long.MIN_VALUE;
        int status = -1;
        try
        {
            status = Integer.parseInt(result.getString(Status));
            if (HttpStatus.SC_OK == status)
            {
                dateLong = result.getLong(epoch) * TimeUtil.ONE_SECOND_LONG_VALUE;
                // store the server time utc long
                UITimeManager.getInstance().setServerLocalTimeLong(dateLong);
            }
            else
            {
                log.warn("getDateSteFromServer() status!=200");
            }
        }
        catch (Exception e)
        {
            log.error(e);
        }
        if (dateLong != Long.MIN_VALUE)
        {
            log.debug("getDateSteFromServer() suc");
        }
        else
        {
            log.warn("getDateSteFromServer() fail");
        }
        return dateLong;
    }
}
