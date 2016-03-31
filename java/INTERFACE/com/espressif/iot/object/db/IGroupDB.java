package com.espressif.iot.object.db;

public interface IGroupDB
{
    public long getId();
    
    public void setId(long id);
    
    /** Not-null value. */
    public String getName();
    
    /** Not-null value; ensure this value is available before it is saved to the database. */
    public void setName(String name);
    
    public String getUserKey();
    
    public void setUserKey(String userKey);
    
    public int getState();
    
    public void setState(int state);
    
    public String getRemoveDeviceBssids();
    
    public String getLocalDeviceBssids();
    
    public String getCloudDeviceBssids();
}
