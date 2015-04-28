package com.espressif.iot.user;

import java.util.Collection;
import java.util.List;

import org.json.JSONObject;

import com.espressif.iot.adt.tree.IEspDeviceTreeElement;
import com.espressif.iot.device.IEspDevice;
import com.espressif.iot.device.IEspDeviceNew;
import com.espressif.iot.object.IEspSingletonObject;
import com.espressif.iot.type.device.DeviceInfo;
import com.espressif.iot.type.device.EspDeviceType;
import com.espressif.iot.type.device.IEspDeviceStatus;
import com.espressif.iot.type.device.status.IEspStatusFlammable;
import com.espressif.iot.type.device.status.IEspStatusHumiture;
import com.espressif.iot.type.net.WifiCipherType;
import com.espressif.iot.type.upgrade.EspUpgradeDeviceCompatibility;
import com.espressif.iot.type.upgrade.EspUpgradeDeviceTypeResult;
import com.espressif.iot.type.user.EspLoginResult;
import com.espressif.iot.type.user.EspRegisterResult;

public interface IEspUser extends IEspSingletonObject
{
    String[] DEVICE_SSID_PREFIX = {"ESP_", "espressif_"};
    
    /**
     * when the device is configured just now, the softap will be scanned, but it should be ignored
     */
    static final long SOFTAP_IGNORE_TIMESTAMP = 60 * 1000;
    
    /**
     * Set the email of the user
     * 
     * @param userEmail
     */
    void setUserEmail(final String userEmail);
    
    /**
     * Get the email of the user
     * 
     * @return email address
     */
    String getUserEmail();
    
    /**
     * Set the id of the user
     * 
     * @param userId
     */
    void setUserId(final long userId);
    
    /**
     * Get the id of the user
     * 
     * @return the user id
     */
    long getUserId();
    
    /**
     * Set the key of the user
     * 
     * @param userKey
     */
    void setUserKey(final String userKey);
    
    /**
     * Get the key of the user
     * 
     * @return user key
     */
    String getUserKey();
    
    /**
     * Set the password of the user
     * 
     * @param userPassword
     */
    void setUserPassword(final String userPassword);
    
    /**
     * Get the password of the user
     * 
     * @return the user password
     */
    String getUserPassword();
    
    void setIsPwdSaved(final boolean isPwdSaved);
    
    boolean isPwdSaved();
    
    /**
     * Set whether the user is auto login
     * 
     * @param isAutoLogin
     */
    void setAutoLogin(final boolean isAutoLogin);
    
    /**
     * Get whether the user is auto login
     * 
     * @return whether the user is auto login
     */
    boolean isAutoLogin();
    
    /**
     * Get the devices of this user
     * 
     * @return the list of devices
     */
    List<IEspDevice> getDeviceList();
    
    /**
     * Get the device in user device list
     * 
     * @param deviceKey
     * @return the device
     */
    IEspDevice getUserDevice(String deviceKey);
    
    /**
     * Get the SoftAp devices
     * 
     * @return the list of SoftAp devices
     */
    List<IEspDeviceNew> scanSoftapDeviceList();
    
    /**
     * Get the SoftAp devices
     * 
     * @param isFilter true is filter the device configured just now
     * @return the list of SoftAp devices
     */
    List<IEspDeviceNew> scanSoftapDeviceList(boolean isFilter);
    
    void setLastConnectedSsid(String ssid);
    
    String getLastConnectedSsid();
    /**
     * 
     * @return last selected ap's bssid or null
     */
    String getLastSelectedApBssid();
    
    /**
     * Get the last selected ap password
     * 
     * @return the last selected ap password
     */
    String getLastSelectedApPassword();
    
    /**
     * the length of String array is 3, position 0 of is bssid, 1 is ssid, 2 is password
     * 
     * @return all configured ap info
     */
    List<String[]> getConfiguredAps();
    
    /**
     * Get the password of configured AP
     * 
     * @param bssid
     * @return the password of configured AP
     */
    String getApPassword(String bssid);
    
    /**
     * Save the information of the configured AP
     * 
     * @param bssid
     * @param ssid
     * @param password
     */
    void saveApInfoInDB(String bssid, String ssid, String password);
    
    /**
     * Save the information of the configured AP
     * @param bssid
     * @param ssid
     * @param password
     * @param deviceBssid
     */
    void saveApInfoInDB(String bssid, String ssid, String password, String deviceBssid);
    
    /**
     * save the user info in local db
     * 
     * @param isPwdSaved whether the password is saved
     * @param isAutoLogin whether it is to skip login process
     */
    Void saveUserInfoInDB(final boolean isPwdSaved, final boolean isAutoLogin);
    
    /**
     * configure the new device to an AP accessible to Internet (if configure suc, save the device into local db with
     * negative device id)
     * 
     * @param device the device to be configured
     * @param apSsid Ap's ssid
     * @param apWifiCipherType Ap's wifi cipher type
     * @param apPassword Ap's password
     */
    void doActionConfigure(final IEspDevice device, String apSsid, WifiCipherType apWifiCipherType, String apPassword);
    
    /**
     * post the status to device(via local or internet) if local it will use local first
     * 
     * @param device the device
     * @param status the new status
     * @return whether the post action is suc
     */
    boolean doActionPostDeviceStatus(final IEspDevice device, final IEspDeviceStatus status);
    
    /**
     * post the status to device(via local or internet) if local it will use local first
     * 
     * @param device the device
     * @param status the new status
     * @param isBroadcast whether post the status to its child or not
     * @return whether the post action is suc
     */
    boolean doActionPostDeviceStatus(final IEspDevice device, final IEspDeviceStatus status, boolean isBroadcast);
    
    /**
     * get the current status of device(via local or internet) if local it will use local first
     * 
     * @param device the device
     * @return whether the get action is suc
     */
    boolean doActionGetDeviceStatus(final IEspDevice device);
    
    /**
     * get the Humiture status list
     * 
     * @param device the Humiture
     * @param startTimestamp startTimestamp
     * @param endTimestamp endTimestamp
     * @param interval the interval of each point
     * @return the Humiture status list
     */
    List<IEspStatusHumiture> doActionGetHumitureStatusList(final IEspDevice device, long startTimestamp,
        long endTimestamp, long interval);
    
    /**
     * get the Flammable status list
     * 
     * @param device the Humiture
     * @param startTimestamp startTimestamp
     * @param endTimestamp endTimestamp
     * @param interval the interval of each point
     * @return the Flammable status list
     */
    List<IEspStatusFlammable> doActionGetFlammableStatusList(final IEspDevice device, long startTimestamp,
        long endTimestamp, long interval);
    
    /**
     * delete the device both in local db and server
     * 
     * if delete on server fail, it will be deleted later automatically
     * 
     * @param device the device
     */
    void doActionDelete(final IEspDevice device);
    
    /**
     * delete the devices both in local db and server
     * 
     * if delete on server fail, it will be deleted later automatically
     * 
     * @param devices
     */
    void doActionDelete(final Collection<IEspDevice> devices);
    
    /**
     * rename the device both in local db and server
     * 
     * if rename on server fail, it will be renamed later automatically
     * 
     * @param device the device
     * @param deviceName the new device name
     */
    void doActionRename(final IEspDevice device, String deviceName);
    
    /**
     * upgrade the device locally, step 1. get *.bin from local step 2. if step 1 fail, get *.bin from server step 3.
     * post *.bin to the device locally
     * 
     * @param device the deivce
     */
    void doActionUpgradeLocal(final IEspDevice device);
    
    /**
     * notify the Server to upgrade the device by Server
     * 
     * @param device the device to be upgraded
     */
    void doActionUpgradeInternet(final IEspDevice device);
    
    /**
     * auto login according to local db
     * 
     * @return @see IEspUser
     */
    IEspUser doActionUserLoginDB();
    
    /**
     * login by Internet
     * 
     * @param userEmail user's email
     * @param userPassword user's password
     * @param isPwdSaved whether the password will be saved
     * @param isAutoLogin whether it is auto login
     * @return @see EspLoginResult
     */
    EspLoginResult doActionUserLoginInternet(String userEmail, String userPassword, boolean isPwdSaved,
        boolean isAutoLogin);
    
    /**
     * register user account by Internet
     * 
     * @param userName user's name
     * @param userEmail user's email
     * @param userPassword user's password
     * @return @see EspRegisterResult
     */
    EspRegisterResult doActionUserRegisterInternet(String userName, String userEmail, String userPassword);
    
    /**
     * when device's updated, the broadcast of DEVICES_ARRIVE_STATEMACHINE or DEVICES_ARRIVE_PULLREFRESH(@see
     * EspStrings) will sent. when IUser receiving the broadcast DEVICES_ARRIVE_STATEMACHINE, he should call this method
     * using "isStateMachine=true". when receiving DEVICES_ARRIVE_PULLREFRESH, he should call this method using
     * "isStateMachine=false".using Void instead of void just to indicate the method is blocked until it finished
     * 
     * @param isStateMachine whether it is device stateMachine notify the user to update
     * @return null
     */
    Void doActionDevicesUpdated(boolean isStateMachine);
    
    /**
     * refresh the devices's status belong to the Player. it will check whether the device is Local , Internet ,
     * Offline, or Coexist of Local and Internet in the background thread. after it is finished, the broadcast of
     * DEVICES_ARRIVE_PULLREFRESH (@see EspStrings) will sent. when IUser receive the broadcast, he should
     * {@link #doActionDevicesUpdated(boolean)}. using void instead of Void just to indicate the method is unblocked
     * while it is executing
     */
    void doActionRefreshDevices();
    
    /**
     * Share device to others, get the share key from server
     * 
     * @param ownerDeviceKey
     * @return the share key
     */
    String doActionGenerateShareKey(String ownerDeviceKey);
    
    /**
     * Get other user's shared device
     * 
     * @param sharedDeviceKey the key from other user
     * @return get shared device success or failed
     */
    boolean doActionActivateSharedDevice(String sharedDeviceKey);
    
    /**
     * check the compatibility between app and device
     * 
     * @param device the device
     * @return @see EspUpgradeDeviceCompatibility
     */
    EspUpgradeDeviceCompatibility checkDeviceCompatibility(IEspDevice device);
    
    /**
     * get the device upgrade Type
     * 
     * @param device the device to be upgraded
     * @return @see getDeviceUpgradeTypeResult
     */
    EspUpgradeDeviceTypeResult getDeviceUpgradeTypeResult(IEspDevice device);
    
    // TODO
    // gotten sharing
    
    /**
     * Get the timers from server, and store the time in the timer list of device
     * 
     * @param device
     * @return get timers success or failed
     */
    boolean doActionDeviceTimerGet(IEspDevice device);
    
    /**
     * Delete the timer from server and local list
     * 
     * @param device
     * @return delete timer success or failed
     */
    boolean doActionDeviceTimerDelete(IEspDevice device, long timerId);
    
    /**
     * Create or edit timer
     * 
     * @param device
     * @param timerJSON
     * @return post success or failed
     */
    boolean doActionDeviceTimerPost(IEspDevice device, JSONObject timerJSON);
    
    /**
     * Connect the SoftAp and get the device info
     * 
     * @param device
     * @return the information of the SoftAP
     */
    DeviceInfo doActionDeviceNewGetInfo(IEspDeviceNew device);
    
    /**
     * Sleep or Reboot the device
     * 
     * @param type @see EspDeviceType
     */
    void doActionDeviceSleepRebootLocal(EspDeviceType type);
    
    /**
     * Get the device's device tree element list
     * @param allDeviceList the list of all device belong to the IUser
     * @return the device's device tree element list
     */
    
    /**
     * Get the tree element list of the user's all device
     * @return the tree element list of the user's all device
     */
    List<IEspDeviceTreeElement> getAllDeviceTreeElementList();
}
