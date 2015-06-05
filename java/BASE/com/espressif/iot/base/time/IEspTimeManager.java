package com.espressif.iot.base.time;

public interface IEspTimeManager
{
    static final String Status = "status";
    
    static final String epoch = "epoch";
    
    static final String Url = "https://iot.espressif.cn/v1/ping/";
    
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
