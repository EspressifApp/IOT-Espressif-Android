package com.espressif.iot.base.time;

public interface IEspTimeManager
{
    /**
     * 
     * @param UTCTimeStamp the utc time to be compared
     * @return whether the UTCTimeStamp is the same date of today
     */
    public boolean isDateToday(long UTCTimeStamp);
    
    /**
     * 
     * @return the UTC time, if fail it will return Long.MIN
     */
    public long getUTCTimeLong();
}
