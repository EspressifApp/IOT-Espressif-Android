package com.espressif.iot.object.db;

import java.util.List;

import com.espressif.iot.db.greenrobot.daos.ApDB;

public interface IApDBManager
{
    /**
     * the separator of the deviceBssids
     */
    static final String SEPARATOR = ",";
    /**
     * max failed time when the AP will be deleted from the local db
     */
    static final int MAX_FAILED_COUNT = 3;
    /**
     * @deprecated Use {link{@link #insertOrReplace(String, String, String, int, String)} instead of it
     * 
     * add AP's [bssid,ssid,password] into local db
     * 
     * @param bssid AP's bssid
     * @param ssid AP's ssid
     * @param password AP's password
     */
    void insertOrReplace(String bssid, String ssid, String password);
    
    /**
     * 
     * add AP's [bssid,ssid,password,0,deviceBssid] into local db
     * 
     * @param bssid AP's bssid
     * @param ssid Ap's ssid
     * @param password AP's password
     * @param deviceBssid device's bssid
     */
    void insertOrReplace(String bssid, String ssid, String password, String deviceBssid);
    
    /**
     * update the AP's info when the device activated suc or fail
     * 
     * @param deviceBssid the device's bssid
     * @param isConfiguredSuc whether the configure is suc
     */
    void updateApInfo(String deviceBssid, boolean isConfiguredSuc);
    
    String getPassword(String bssid);
    
    ApDB getLastSelectedApDB();
    
    List<ApDB> getAllApDBList();
    
    void delete(String ssid);
    
    void updatePassword(String ssid, String password);
}
