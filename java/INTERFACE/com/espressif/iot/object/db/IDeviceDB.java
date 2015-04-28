package com.espressif.iot.object.db;

import com.espressif.iot.object.IEspDBObject;

/**
 * the Device db should save such properties as follows
 * 
 * @author afunx
 * 
 */
public interface IDeviceDB extends IEspDBObject
{
    long getId();
    
    void setId(long id);
    
    String getKey();

    void setKey(String key);
    
    String getBssid();
    
    void setBssid(String bssid);
    
    int getType();
    
    void setType(int type);
    
    int getState();
    
    void setState(int state);
    
    boolean getIsOwner();
    
    void setIsOwner(boolean isOwner);
    
    String getName();
    
    void setName(String name);
    
    String getRom_version();

    void setRom_version(String rom_version);

    String getLatest_rom_version();

    void setLatest_rom_version(String latest_rom_version);
    
    long getTimestamp();
    
    void setTimestamp(long timestamp);
    
    long getUserId();
    
    void setUserId(long userId);
    
}
