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
    /**
     * get the device's id
     * @return the device's id
     */
    long getId();
    
    /**
     * set the device's id
     * @param id the device's id
     */
    void setId(long id);
    
    /**
     * get the device's key
     * @return the device's key
     */
    String getKey();
    
    /**
     * set the device's key
     * @param key the device's key
     */
    void setKey(String key);
    
    /**
     * get the device's bssid
     * @return the device's bssid
     */
    String getBssid();
    
    /**
     * set the device's bssid
     * @param bssid the device's bssid
     */
    void setBssid(String bssid);
    
    /**
     * get the device's type int value
     * @return the device's type int value
     */
    int getType();
    
    /**
     * set the device's type int value
     * @param type the device's type int value
     */
    void setType(int type);
    
    /**
     * get the device's state int value
     * @return the device's state int value
     */
    int getState();
    
    /**
     * set the device's state int value
     * @param state the device's state int value
     */
    void setState(int state);
    
    /**
     * get whether the user is the device's owner
     * @return whether the user is the device's owner
     */
    boolean getIsOwner();
    
    /**
     * set whether the user is the device's owner
     * @param isOwner whether the user is the device's owner
     */
    void setIsOwner(boolean isOwner);
    
    /**
     * get the device's name
     * @return the device's name
     */
    String getName();
    
    /**
     * set the device's name
     * @param name the device's name
     */
    void setName(String name);
    
    /**
     * get the current rom version of the device
     * @return the current rom version of the device
     */
    String getRom_version();
    
    /**
     * set the current rom version of the device
     * @param rom_version the current rom version of the device
     */
    void setRom_version(String rom_version);
    
    /**
     * get the latest rom version of the device
     * @return the latest rom version of the device
     */
    String getLatest_rom_version();
    
    /**
     * set the latest rom version of the device
     * @param latest_rom_version the latest rom version of the device
     */
    void setLatest_rom_version(String latest_rom_version);
    
    /**
     * get the timestamp of the device when the device is configured by user
     * @return the timestamp of the device when the device is configure by user
     */
    long getTimestamp();
    
    /**
     * set the timestamp of the device when the device is configured by user
     * @param timestamp the timestamp of the device when the device is configured by user
     */
    void setTimestamp(long timestamp);
    
    /**
     * get the user's id who has the authority to use the device
     * @return the user's id who has the authority to use the device
     */
    long getUserId();
    
    /**
     * set the user's id who has the authority to use the device
     * @param userId the user's id who has the authority to use the device
     */
    void setUserId(long userId);
    
    /**
     * Get the activated time on server
     * 
     * @return
     */
    long getActivatedTime();
    
    /**
     * Set the activated time on server
     * 
     * @param activatedTime
     */
    void setActivatedTime(long activatedTime);
}
