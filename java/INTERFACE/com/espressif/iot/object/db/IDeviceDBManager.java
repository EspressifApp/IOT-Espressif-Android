package com.espressif.iot.object.db;

import com.espressif.iot.object.IEspDBManager;

public interface IDeviceDBManager extends IEspDBManager
{
    /**
     * insert or replace the device in local db
     * 
     * @param deviceId device's id
     * @param key device's key(activated) or device' temp random40 key(activating)
     * @param bssid device's bssid(sta)
     * @param type device's type
     * @param state device's state
     * @param isOwner whether the user is the device owner
     * @param name device's name
     * @param rom_version the current sdk rom version
     * @param latest_rom_version the latest sdk rom version
     * @param timestamp the UTC millisecond timestamp when the device is activated
     * @param activatedTime the UTC millisecond time activated at server
     * @param userId the user's id
     */
    void insertOrReplace(long deviceId, String key, String bssid, int type, int state, boolean isOwner, String name,
        String rom_version, String latest_rom_version, long timestamp, long activatedTime, long userId);
    
    /**
     * delete the device in local db
     * 
     * @param deviceId the device's id
     */
    void delete(long deviceId);
    
    /**
     * delete the devices whose bssid is the same with the device whose bssid is deviceId 
     * 
     * @param deviceId the device's id
     */
    void deleteDevicesByDeviceId(long deviceId);
    
    /**
     * delete the devices by bssid
     * @param bssid the devices' bssid to be deleted
     */
    void deleteDevicesByBssid(String bssid);
    
    /**
     * insert the activating device
     * 
     * @param key the temp random40 key
     * @param bssid the device's bssid(sta)
     * @param type the device's type
     * @param state the device's state
     * @param name the device's name
     * @param rom_version the current sdk rom version
     * @param latest_rom_version the latest sdk rom version
     * @param timestamp the timestamp the device is activating
     * @param userId the user id
     * @return the device id
     */
    long insertActivatingDevice(String key, String bssid, int type, int state, String name, String rom_version,
        String latest_rom_version, long timestamp, long userId);

    /**
     * rename the device in local db if the device exist
     * @param deviceId the deivce's id
     * @param name the device's new name
     */
    void renameDevice(long deviceId,String name);
}
