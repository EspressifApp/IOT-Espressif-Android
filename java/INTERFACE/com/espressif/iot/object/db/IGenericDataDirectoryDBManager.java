package com.espressif.iot.object.db;

import com.espressif.iot.object.IEspDBManager;

public interface IGenericDataDirectoryDBManager extends IEspDBManager
{
    final static int MAX_DATA_COUNT = 80000;
    
    /**
     * delete the expired data directory and data
     */
    void deleteExpiredDataDirectoryAndData();
    
    /**
     * 
     * @param deviceId device id
     * @param startTimestampFromUI the start UTC timestamp from UI
     * @param endTimestampFromUI the end UTC timestamp from UI
     * @return the start UTC timestamp to get data from Server
     */
    long getStartTimestampFromServer(long deviceId, long startTimestampFromUI, long endTimestampFromUI);
    
    /**
     * 
     * @param deviceId device id
     * @param startTimestampFromUI the start UTC timestamp from UI
     * @param endTimestampFromUI the end UTC timestamp from UI
     * @return the end UTC timestamp to get data from Server
     */
    long getEndTimestampFromServer(long deviceId, long startTimestampFromUI, long endTimestampFromUI);
    
    /**
     * insert or replace data directory
     * 
     * @param deviceId device id
     * @param dayStartTimestamp the UTC time of 00:00:00 A.M.
     * @param indexTimestamp index timestamp which is the last data timestamp get data from server
     * @return data directory id
     */
    long __insertOrReplaceDataDirectory(long deviceId, long dayStartTimestamp, long indexTimestamp);
    
}
