package com.espressif.iot.object.db;

public interface IGenericDataDirectoryDB
{
    Long getId();
    
    void setId(Long id);
    
    long getDeviceId();
    
    void setDeviceId(long deviceId);
    
    long getDay_start_timestamp();
    
    void setDay_start_timestamp(long day_start_timestamp);
    
    long getIndex_timestamp();
    
    void setIndex_timestamp(long index_timestamp);
    
    long getLastest_accessed_timestamp();
    
    void setLastest_accessed_timestamp(long lastest_accessed_timestamp);
}
