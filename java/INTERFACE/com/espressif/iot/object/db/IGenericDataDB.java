package com.espressif.iot.object.db;

public interface IGenericDataDB
{
    public Long getId();
    
    public void setId(Long id);
    
    public long getDeviceId();
    
    public void setDeviceId(long deviceId);
    
    public long getTimestamp();
    
    public void setTimestamp(long timestamp);
    
    public String getData();
    
    public void setData(String data);
    
    public long getDirectoryId();
    
    public void setDirectoryId(long directoryId);
}
