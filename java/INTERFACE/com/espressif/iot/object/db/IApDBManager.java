package com.espressif.iot.object.db;

import java.util.List;

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
    
    /**
     * get the Ap's password by the Ap's bssid
     * @param bssid the Ap's bssid
     * @return the Ap's password
     */
    String getPassword(String bssid);
    
    /**
     * get the last selected Ap
     * @return the last selected Ap
     */
    IApDB getLastSelectedApDB();
    
    /**
     * get the list of all Aps
     * @return the list of all Aps
     */
    List<IApDB> getAllApDBList();
    
    /**
     * delete the Ap(s) by the Ap's ssid
     * @param ssid the ssid of which Ap is to be deleted
     */
    void delete(String ssid);
    
    /**
     * update the Ap(s)'s password by its ssid
     * @param ssid the Ap's ssid
     * @param password the Ap's password
     */
    void updatePassword(String ssid, String password);
}
