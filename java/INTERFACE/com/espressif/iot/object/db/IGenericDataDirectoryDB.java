package com.espressif.iot.object.db;

public interface IGenericDataDirectoryDB
{
    /**
     * get the id of the generic data directory
     * @return the id of the generic data directory
     */
    Long getId();
    
    /**
     * set the id of the generic data directory
     * @param id the id of the generic data directory
     */
    void setId(Long id);
    
    /**
     * get the device's id which device the generic data directory is belong to
     * @return the device's id which device the generic data directory is belong to
     */
    long getDeviceId();
    
    /**
     * set the device's id which device the generic data directory is belong to
     * @param deviceId the device's id which device the generic data directory is belong to
     */
    void setDeviceId(long deviceId);
    
    /**
     * get the UTC timestamp when the generic data directory is started from
     * @return the UTC timestamp when the generic data directory is started from
     */
    long getDay_start_timestamp();
    
    /**
     * set the UTC timestamp when the generic data directory is started from
     * @param day_start_timestamp the UTC timestamp when the generic data directory is started from
     */
    void setDay_start_timestamp(long day_start_timestamp);
    
    /**
     * get the UTC index timestamp when the generic data is gotten from server ultimately
     * @return the UTC index timestamp when the generic data is gotten from server ultimately
     */
    long getIndex_timestamp();
    
    /**
     * set the UTC index timestamp when the generic data is gotten from server ultimately
     * @param index_timestamp the UTC index timestamp when the generic data is gotten from server ultimately
     */
    void setIndex_timestamp(long index_timestamp);
    
    /**
     * get the UTC timestamp when the generic data directory is touched by user last time
     * @return the UTC timestamp when the generic data directory is touched by user last time
     */
    long getLastest_accessed_timestamp();
    
    /**
     * set the UTC timestamp when the generic data directory is touched by user last time
     * @param lastest_accessed_timestamp the UTC timestamp when the generic data directory is touched by user last time
     */
    void setLastest_accessed_timestamp(long lastest_accessed_timestamp);
}
