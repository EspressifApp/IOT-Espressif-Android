package com.espressif.iot.model.user;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.ScanResult;
import android.text.TextUtils;

import com.espressif.iot.action.IEspActionUserRegisterPhoneInternet;
import com.espressif.iot.action.device.New.EspActionDeviceNewGetInfoLocal;
import com.espressif.iot.action.device.New.IEspActionDeviceNewGetInfoLocal;
import com.espressif.iot.action.device.common.EspActionDeviceActivateSharedInternet;
import com.espressif.iot.action.device.common.EspActionDeviceGenerateShareKeyInternet;
import com.espressif.iot.action.device.common.EspActionDeviceGetStatusInternet;
import com.espressif.iot.action.device.common.EspActionDeviceGetStatusLocal;
import com.espressif.iot.action.device.common.EspActionDevicePostStatusInternet;
import com.espressif.iot.action.device.common.EspActionDevicePostStatusLocal;
import com.espressif.iot.action.device.common.EspActionDeviceSleepRebootLocal;
import com.espressif.iot.action.device.common.EspActionDeviceSynchronizeInterentDiscoverLocal;
import com.espressif.iot.action.device.common.IEspActionDeviceActivateSharedInternet;
import com.espressif.iot.action.device.common.IEspActionDeviceGenerateShareKeyInternet;
import com.espressif.iot.action.device.common.IEspActionDeviceGetStatusInternet;
import com.espressif.iot.action.device.common.IEspActionDeviceGetStatusLocal;
import com.espressif.iot.action.device.common.IEspActionDevicePostStatusInternet;
import com.espressif.iot.action.device.common.IEspActionDevicePostStatusLocal;
import com.espressif.iot.action.device.common.IEspActionDeviceSleepRebootLocal;
import com.espressif.iot.action.device.common.IEspActionDeviceSynchronizeInterentDiscoverLocal;
import com.espressif.iot.action.device.common.timer.EspActionDeviceTimerDeleteInternet;
import com.espressif.iot.action.device.common.timer.EspActionDeviceTimerGetInternet;
import com.espressif.iot.action.device.common.timer.EspActionDeviceTimerPostInternet;
import com.espressif.iot.action.device.common.timer.IEspActionDeviceTimerDeleteInternet;
import com.espressif.iot.action.device.common.timer.IEspActionDeviceTimerGetInternet;
import com.espressif.iot.action.device.common.timer.IEspActionDeviceTimerPostInternet;
import com.espressif.iot.action.device.esptouch.EspActionDeviceEsptouch;
import com.espressif.iot.action.device.esptouch.IEspActionDeviceEsptouch;
import com.espressif.iot.action.device.humiture.EspActionHumitureGetStatusListInternetDB;
import com.espressif.iot.action.device.humiture.IEspActionHumitureGetStatusListInternetDB;
import com.espressif.iot.action.user.EspActionFindAccountInternet;
import com.espressif.iot.action.user.EspActionGetSmsCaptchaCodeInternet;
import com.espressif.iot.action.user.EspActionThirdPartyLoginInternet;
import com.espressif.iot.action.user.EspActionUserDevicesUpdated;
import com.espressif.iot.action.user.EspActionUserLoginDB;
import com.espressif.iot.action.user.EspActionUserLoginInternet;
import com.espressif.iot.action.user.EspActionUserLoginPhoneInternet;
import com.espressif.iot.action.user.EspActionUserRegisterInternet;
import com.espressif.iot.action.user.EspActionUserRegisterPhoneInternet;
import com.espressif.iot.action.user.IEspActionFindAccountnternet;
import com.espressif.iot.action.user.IEspActionGetSmsCaptchaCodeInternet;
import com.espressif.iot.action.user.IEspActionThirdPartyLoginInternet;
import com.espressif.iot.action.user.IEspActionUserDevicesUpdated;
import com.espressif.iot.action.user.IEspActionUserLoginDB;
import com.espressif.iot.action.user.IEspActionUserLoginInternet;
import com.espressif.iot.action.user.IEspActionUserLoginPhoneInternet;
import com.espressif.iot.action.user.IEspActionUserRegisterInternet;
import com.espressif.iot.base.api.EspBaseApiUtil;
import com.espressif.iot.base.application.EspApplication;
import com.espressif.iot.db.IOTApDBManager;
import com.espressif.iot.db.IOTUserDBManager;
import com.espressif.iot.db.greenrobot.daos.ApDB;
import com.espressif.iot.db.greenrobot.daos.DeviceDB;
import com.espressif.iot.device.IEspDevice;
import com.espressif.iot.device.IEspDeviceConfigure;
import com.espressif.iot.device.IEspDeviceNew;
import com.espressif.iot.device.IEspDeviceSSS;
import com.espressif.iot.device.builder.BEspDevice;
import com.espressif.iot.device.builder.BEspDeviceNew;
import com.espressif.iot.device.builder.BEspDeviceRoot;
import com.espressif.iot.device.statemachine.IEspDeviceStateMachine;
import com.espressif.iot.device.statemachine.IEspDeviceStateMachine.Direction;
import com.espressif.iot.device.upgrade.IEspDeviceCheckCompatibility;
import com.espressif.iot.device.upgrade.IEspDeviceGetUpgradeTypeResult;
import com.espressif.iot.model.device.statemachine.EspDeviceStateMachine;
import com.espressif.iot.model.device.statemachine.EspDeviceStateMachineHandler;
import com.espressif.iot.model.device.statemachine.IEspDeviceStateMachineHandler;
import com.espressif.iot.model.device.upgrade.EspDeviceCheckCompatibility;
import com.espressif.iot.model.device.upgrade.EspDeviceGetUpgradeTypeResult;
import com.espressif.iot.object.db.IApDB;
import com.espressif.iot.type.device.DeviceInfo;
import com.espressif.iot.type.device.EspDeviceType;
import com.espressif.iot.type.device.IEspDeviceState;
import com.espressif.iot.type.device.IEspDeviceStatus;
import com.espressif.iot.type.device.esptouch.EsptouchResult;
import com.espressif.iot.type.device.esptouch.IEsptouchResult;
import com.espressif.iot.type.device.state.EspDeviceState;
import com.espressif.iot.type.device.status.IEspStatusFlammable;
import com.espressif.iot.type.device.status.IEspStatusHumiture;
import com.espressif.iot.type.net.IOTAddress;
import com.espressif.iot.type.net.WifiCipherType;
import com.espressif.iot.type.upgrade.EspUpgradeDeviceCompatibility;
import com.espressif.iot.type.upgrade.EspUpgradeDeviceTypeResult;
import com.espressif.iot.type.user.EspLoginResult;
import com.espressif.iot.type.user.EspRegisterResult;
import com.espressif.iot.user.IEspUser;
import com.espressif.iot.util.BSSIDUtil;
import com.espressif.iot.util.EspStrings;
import com.espressif.iot.util.RandomUtil;
import com.espressif.iot.util.TimeUtil;

public class EspUser implements IEspUser
{
    
    private final static Logger log = Logger.getLogger(EspUser.class);
    
    private long mUserId;
    
    private String mUserKey;
    
    private String mUserName;
    
    private String mUserEmail;
    
    private List<IEspDevice> mDeviceList = new ArrayList<IEspDevice>();
    
    private final List<IEspDeviceSSS> mStaDeviceList = new ArrayList<IEspDeviceSSS>();
    
    private final ReentrantLock mDeviceListsLock = new ReentrantLock();
    
    private volatile IEspActionDeviceEsptouch mActionDeviceEsptouch = null;
    
    private final Object mEsptouchLock = new Object();
    
    private volatile int mIsEsptouchExecuteCount = 0;
    
    private volatile int mIsEsptouchCancelledCount = 0;
    
    @Override
    public String toString()
    {
        return "[id=" + mUserId + ",key=" + mUserKey + ",email=" + mUserEmail + "]";
    }
    
    @Override
    public Void saveUserInfoInDB()
    {
        IOTUserDBManager.getInstance().changeUserInfo(mUserId, mUserEmail, mUserKey, mUserName);
        return null;
    }
    
    @Override
    public void setUserEmail(String userEmail)
    {
        this.mUserEmail = userEmail;
    }
    
    @Override
    public String getUserEmail()
    {
        return this.mUserEmail;
    }
    
    @Override
    public void setUserId(long userId)
    {
        this.mUserId = userId;
    }
    
    @Override
    public long getUserId()
    {
        return this.mUserId;
    }
    
    @Override
    public void setUserKey(String userKey)
    {
        this.mUserKey = userKey;
    }
    
    @Override
    public String getUserKey()
    {
        return this.mUserKey;
    }
    
    @Override
    public void setUserName(String userName)
    {
        mUserName = userName;
    }
    
    @Override
    public String getUserName()
    {
        return mUserName;
    }
    
    @Override
    public boolean isLogin()
    {
        return !TextUtils.isEmpty(mUserKey);
    }
    
    @Override
    public List<IEspDevice> getDeviceList()
    {
        // for the mDeviceList maybe changed after the result return,
        // but we don't like UI layer get dirty device list,
        // so we return the copy list to prevent it
        List<IEspDevice> result = new ArrayList<IEspDevice>();
        lockUserDeviceLists();
        result.addAll(mDeviceList);
        unlockUserDeviceLists();
        return result;
    }
    
    private String mLastConnectedSsid;
    
    @Override
    public void setLastConnectedSsid(String ssid)
    {
        mLastConnectedSsid = ssid;
    }
    
    @Override
    public String getLastConnectedSsid()
    {
        return mLastConnectedSsid;
    }
    
    @Override
    public String getLastSelectedApBssid()
    {
        IOTApDBManager iotApDBManager = IOTApDBManager.getInstance();
        IApDB apDB = iotApDBManager.getLastSelectedApDB();
        if (apDB == null)
        {
            return null;
        }
        else
        {
            return apDB.getBssid();
        }
    }
    
    @Override
    public String getLastSelectedApPassword()
    {
        IOTApDBManager iotApDBManager = IOTApDBManager.getInstance();
        IApDB apDB = iotApDBManager.getLastSelectedApDB();
        if (apDB == null)
        {
            return null;
        }
        else
        {
            return apDB.getPassword();
        }
    }
    
    @Override
    public List<String[]> getConfiguredAps()
    {
        IOTApDBManager iotApDBManager = IOTApDBManager.getInstance();
        List<ApDB> apDBs = iotApDBManager.getAllApDBList();
        List<String[]> result = new ArrayList<String[]>();
        for (ApDB apDB : apDBs)
        {
            String[] ap = new String[3];
            ap[0] = apDB.getBssid();
            ap[1] = apDB.getSsid();
            ap[2] = apDB.getPassword();
            result.add(ap);
        }
        return result;
    }
    
    @Override
    public String getApPassword(String bssid)
    {
        IOTApDBManager iotApDBManager = IOTApDBManager.getInstance();
        return iotApDBManager.getPassword(bssid);
    }
    
    @Override
    public void saveApInfoInDB(String bssid, String ssid, String password)
    {
        IOTApDBManager iotApDBManager = IOTApDBManager.getInstance();
        iotApDBManager.insertOrReplace(bssid, ssid, password);
    }
    
    @Override
    public void saveApInfoInDB(String bssid, String ssid, String password, String deviceBssid)
    {
        IOTApDBManager iotApDBManager = IOTApDBManager.getInstance();
        iotApDBManager.insertOrReplace(bssid, ssid, password, deviceBssid);
    }
    
    @Override
    public IEspDevice getUserDevice(String deviceKey)
    {
        // Check Virtual Root router device
        IEspDevice deviceRoot = BEspDeviceRoot.getBuilder().getLocalRoot();
        if (deviceKey.equals(deviceRoot.getKey()))
        {
            return deviceRoot;
        }
        deviceRoot = BEspDeviceRoot.getBuilder().getInternetRoot();
        if (deviceKey.equals(deviceRoot.getKey()))
        {
            return deviceRoot;
        }
        deviceRoot = BEspDeviceRoot.getBuilder().getVirtualMeshRoot();
        if (deviceKey.equals(deviceRoot.getKey()))
        {
            return deviceRoot;
        }
        
        List<IEspDevice> deviceList = getAllDeviceList();
        for (IEspDevice device : deviceList)
        {
            // if the device is DELETED, ignore it
            if (device.getDeviceState().isStateDeleted())
            {
                continue;
            }
            if (deviceKey.equals(device.getKey()))
            {
                return device;
            }
        }
        
        return null;
    }
    
    @Override
    public void doActionConfigure(IEspDevice device, String apSsid, WifiCipherType apWifiCipherType, String apPassword)
    {
        String randomToken = RandomUtil.random40();
        IEspDeviceNew deviceNew = (IEspDeviceNew)device;
        deviceNew.setApPassword(apPassword);
        deviceNew.setApSsid(apSsid);
        deviceNew.setApWifiCipherType(apWifiCipherType);
        deviceNew.setKey(randomToken);
        deviceNew.setUserId(mUserId);
        IEspDeviceStateMachine stateMachine = EspDeviceStateMachine.getInstance();
        stateMachine.transformState(device, Direction.CONFIGURE);
    }
    
    @Override
    public boolean doActionPostDeviceStatus(IEspDevice device, IEspDeviceStatus status)
    {
        return doActionPostDeviceStatus(device, status, false);
    }
    
    @Override
    public boolean doActionPostDeviceStatus(IEspDevice device, IEspDeviceStatus status, boolean isBroadcast)
    {
        boolean isLocal = device.getDeviceState().isStateLocal();
        if (isLocal)
        {
            IEspActionDevicePostStatusLocal actionLocal = new EspActionDevicePostStatusLocal();
            return actionLocal.doActionDevicePostStatusLocal(device, status, isBroadcast);
        }
        else
        {
            IEspActionDevicePostStatusInternet actionInternet = new EspActionDevicePostStatusInternet();
            return actionInternet.doActionDevicePostStatusInternet(device, status, isBroadcast);
        }
    }
    
    @Override
    public boolean doActionGetDeviceStatus(IEspDevice device)
    {
        boolean isLocal = device.getDeviceState().isStateLocal();
        if (isLocal)
        {
            IEspActionDeviceGetStatusLocal actionLocal = new EspActionDeviceGetStatusLocal();
            return actionLocal.doActionDeviceGetStatusLocal(device);
        }
        else
        {
            IEspActionDeviceGetStatusInternet actionInternet = new EspActionDeviceGetStatusInternet();
            return actionInternet.doActionDeviceGetStatusInternet(device);
        }
    }
    
    @Override
    public void doActionDelete(IEspDevice device)
    {
        IEspDeviceStateMachine stateMachine = EspDeviceStateMachine.getInstance();
        stateMachine.transformState(device, Direction.DELETE);
    }
    
    @Override
    public void doActionDelete(final Collection<IEspDevice> devices)
    {
        IEspDeviceStateMachine stateMachine = EspDeviceStateMachine.getInstance();
        stateMachine.transformState(devices, Direction.DELETE);
    }
    
    @Override
    public void doActionRename(IEspDevice device, String deviceName)
    {
        device.setName(deviceName);
        IEspDeviceStateMachine stateMachine = EspDeviceStateMachine.getInstance();
        stateMachine.transformState(device, Direction.RENAME);
    }
    
    @Override
    public void doActionUpgradeLocal(IEspDevice device)
    {
        IEspDeviceStateMachine stateMachine = EspDeviceStateMachine.getInstance();
        stateMachine.transformState(device, Direction.UPGRADE_LOCAL);
    }
    
    @Override
    public void doActionUpgradeInternet(IEspDevice device)
    {
        IEspDeviceStateMachine stateMachine = EspDeviceStateMachine.getInstance();
        stateMachine.transformState(device, Direction.UPGRADE_INTERNET);
    }
    
    @Override
    public void doActionRefreshDevices()
    {
        IEspActionDeviceSynchronizeInterentDiscoverLocal action = new EspActionDeviceSynchronizeInterentDiscoverLocal();
        action.doActionDeviceSynchronizeInterentDiscoverLocal(mUserKey);
    }
    
    @Override
    public void doActionRefreshStaDevices(boolean isSyn)
    {
        IEspActionDeviceSynchronizeInterentDiscoverLocal action = new EspActionDeviceSynchronizeInterentDiscoverLocal();
        action.doActionDeviceSynchronizeDiscoverLocal(isSyn);
    }
    
    private void __loadUserDeviceList()
    {
        lockUserDeviceLists();
        IOTUserDBManager iotUserDBManager = IOTUserDBManager.getInstance();
        List<DeviceDB> deviceDBList = iotUserDBManager.getUserDeviceList(mUserId);
        mDeviceList = new ArrayList<IEspDevice>();
        // add device into mDeviceList by deviceDBList
        for (DeviceDB deviceDB : deviceDBList)
        {
            IEspDevice device = BEspDevice.getInstance().alloc(deviceDB);
            IEspDeviceState deviceState = device.getDeviceState();
            // if the device state is ACTIVATING, resume activating
            if (EspDeviceState.checkValidWithSpecificStates(deviceState, EspDeviceState.ACTIVATING))
            {
                IEspDeviceNew deviceNew = (IEspDeviceNew)device;
                deviceNew.resume();
            }
            // else LOCAL, INTERNET, UPGRADEING_LOCAL, UPGRADING_INTERNET should be set OFFLINE
            else
            {
                if (EspDeviceState.checkValidWithNecessaryStates(deviceState, EspDeviceState.LOCAL)
                    && EspDeviceState.checkValidWithPermittedStates(deviceState,
                        EspDeviceState.LOCAL,
                        EspDeviceState.RENAMED))
                {
                    deviceState.clearStateLocal();
                    deviceState.addStateOffline();
                }
                
                else if (EspDeviceState.checkValidWithNecessaryStates(deviceState, EspDeviceState.INTERNET)
                    && EspDeviceState.checkValidWithPermittedStates(deviceState,
                        EspDeviceState.INTERNET,
                        EspDeviceState.RENAMED))
                {
                    deviceState.clearStateInternet();
                    deviceState.addStateOffline();
                }
                
                else if (EspDeviceState.checkValidWithNecessaryStates(deviceState,
                    EspDeviceState.LOCAL,
                    EspDeviceState.INTERNET)
                    && EspDeviceState.checkValidWithPermittedStates(deviceState,
                        EspDeviceState.LOCAL,
                        EspDeviceState.INTERNET,
                        EspDeviceState.RENAMED))
                {
                    deviceState.clearStateLocal();
                    deviceState.clearStateInternet();
                    deviceState.addStateOffline();
                }
                
                else if (EspDeviceState.checkValidWithNecessaryStates(deviceState, EspDeviceState.OFFLINE)
                    && EspDeviceState.checkValidWithPermittedStates(deviceState,
                        EspDeviceState.OFFLINE,
                        EspDeviceState.RENAMED))
                {
                }
                
                else if (EspDeviceState.checkValidWithNecessaryStates(deviceState, EspDeviceState.UPGRADING_LOCAL)
                    && EspDeviceState.checkValidWithPermittedStates(deviceState,
                        EspDeviceState.UPGRADING_LOCAL,
                        EspDeviceState.LOCAL,
                        EspDeviceState.INTERNET,
                        EspDeviceState.RENAMED))
                {
                    deviceState.clearStateUpgradingLocal();
                    deviceState.clearStateLocal();
                    deviceState.clearStateInternet();
                    deviceState.addStateOffline();
                }
                
                else if (EspDeviceState.checkValidWithNecessaryStates(deviceState, EspDeviceState.UPGRADING_INTERNET)
                    && EspDeviceState.checkValidWithPermittedStates(deviceState,
                        EspDeviceState.UPGRADING_INTERNET,
                        EspDeviceState.LOCAL,
                        EspDeviceState.INTERNET,
                        EspDeviceState.RENAMED))
                {
                    deviceState.clearStateUpgradingInternet();
                    deviceState.clearStateLocal();
                    deviceState.clearStateInternet();
                    deviceState.addStateOffline();
                }
                
                else if (EspDeviceState.checkValidWithSpecificStates(deviceState, EspDeviceState.DELETED))
                {
                }
                
                else
                {
                    throw new IllegalStateException("device: " + device);
                }
            }
            mDeviceList.add(device);
        }
        // sort device list
        EspDeviceGenericComparator comparator = new EspDeviceGenericComparator();
        Collections.sort(mDeviceList, comparator);
        
        // do rename action or delete action if necessay
        for (IEspDevice device : mDeviceList)
        {
            IEspDeviceState deviceState = device.getDeviceState();
            if (deviceState.isStateRenamed() && !deviceState.isStateDeleted())
            {
                doActionRename(device, device.getName());
            }
            else if (deviceState.isStateDeleted())
            {
                doActionDelete(device);
            }
        }
        unlockUserDeviceLists();
    }
    
    @Override
    public EspLoginResult doActionUserLoginInternet(String userEmail, String userPassword)
    {
        IEspActionUserLoginInternet action = new EspActionUserLoginInternet();
        EspLoginResult result = action.doActionUserLoginInternet(userEmail, userPassword);
        if (result == EspLoginResult.SUC)
        {
            __loadUserDeviceList();
        }
        return result;
    }
    
    @Override
    public EspLoginResult doActionThirdPartyLoginInternet(EspThirdPartyLoginPlat espPlat)
    {
        IEspActionThirdPartyLoginInternet action = new EspActionThirdPartyLoginInternet();
        EspLoginResult result = action.doActionThirdPartyLoginInternet(espPlat);
        if (result == EspLoginResult.SUC)
        {
            __loadUserDeviceList();
        }
        return result;
    }
    
    @Override
    public EspLoginResult doActionUserLoginPhone(String phoneNumber, String password)
    {
        IEspActionUserLoginPhoneInternet action = new EspActionUserLoginPhoneInternet();
        EspLoginResult result = action.doActionUserLoginPhone(phoneNumber, password);
        if (result == EspLoginResult.SUC)
        {
            __loadUserDeviceList();
        }
        return result;
    }
    
    @Override
    public IEspUser doActionUserLoginDB()
    {
        IEspActionUserLoginDB action = new EspActionUserLoginDB();
        IEspUser result = action.doActionUserLoginDB();
        SharedPreferences shared =
            EspApplication.sharedInstance().getSharedPreferences(EspStrings.Key.SETTINGS_NAME, Context.MODE_PRIVATE);
        if (shared.getBoolean(EspStrings.Key.KEY_AUTO_LOGIN, false))
        {
            __loadUserDeviceList();
        }
        return result;
    }
    
    @Override
    public void loadUserDeviceListDB()
    {
        __loadUserDeviceList();
    }
    
    @Override
    public EspRegisterResult doActionUserRegisterInternet(String userName, String userEmail, String userPassword)
    {
        IEspActionUserRegisterInternet action = new EspActionUserRegisterInternet();
        return action.doActionUserRegisterInternet(userName, userEmail, userPassword);
    }
    
    @Override
    public EspRegisterResult doActionUserRegisterPhone(String phoneNumber, String captchaCode, String userPassword)
    {
        IEspActionUserRegisterPhoneInternet action = new EspActionUserRegisterPhoneInternet();
        return action.doActionUserRegisterPhone(phoneNumber, captchaCode, userPassword);
    }
    
    @Override
    public boolean findAccountUsernameRegistered(String userName)
    {
        IEspActionFindAccountnternet action = new EspActionFindAccountInternet();
        return action.doActionFindUsernametInternet(userName);
    }
    
    @Override
    public boolean findAccountEmailRegistered(String email)
    {
        IEspActionFindAccountnternet action = new EspActionFindAccountInternet();
        return action.doActionFindEmailInternet(email);
    }
    
    @Override
    public Void doActionDevicesUpdated(boolean isStateMachine)
    {
        IEspActionUserDevicesUpdated action = new EspActionUserDevicesUpdated();
        return action.doActionDevicesUpdated(isStateMachine);
    }
    
    // "espressif_" + MAC address's 6 places
    private boolean isESPMeshDevice(String SSID)
    {
        for (int i = 0; i < MESH_DEVICE_SSID_PREFIX.length; i++)
        {
            if (SSID.startsWith(MESH_DEVICE_SSID_PREFIX[i]))
            {
                return true;
            }
        }
        return false;
    }
    
    // "ESP_" + MAC address's 6 places, ordinary device
    // "espressif_" + MAC address's 6 places, mesh device
    private boolean isESPDevice(String SSID)
    {
        for (int i = 0; i < DEVICE_SSID_PREFIX.length; i++)
        {
            if (SSID.startsWith(DEVICE_SSID_PREFIX[i]))
            {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public List<IEspDeviceNew> scanSoftapDeviceList()
    {
        return scanSoftapDeviceList(true);
    }
    
    @Override
    public List<IEspDeviceNew> scanSoftapDeviceList(boolean isFilter)
    {
        List<IEspDeviceNew> softapDeviceList = new ArrayList<IEspDeviceNew>();
        List<ScanResult> scanResultList = EspBaseApiUtil.scan();
        for (ScanResult scanResult : scanResultList)
        {
            // mesh device don't support softap mode, although the softap can be found
            if (isESPDevice(scanResult.SSID) && !isESPMeshDevice(scanResult.SSID))
            {
                String ssid = scanResult.SSID;
                // change the device bssid to sta
                String bssid = BSSIDUtil.restoreStaBSSID(scanResult.BSSID);
                int rssi = scanResult.level;
                WifiCipherType wifiCipherType = WifiCipherType.getWifiCipherType(scanResult);
                int state = EspDeviceState.NEW.getStateValue();
                IEspDeviceNew deviceNew = BEspDeviceNew.getInstance().alloc(ssid, bssid, wifiCipherType, rssi, state);
                deviceNew.getDeviceState().addStateNew();
                softapDeviceList.add(deviceNew);
            }
        }
        
        // filter device configured just now
        // List<IEspDevice> mDeviceList
        if (isFilter)
        {
            String bssidSoftap;
            String bssidDevice;
            long timestampConfigure;
            for (IEspDevice device : getDeviceList())
            {
                for (int i = 0; i < softapDeviceList.size(); i++)
                {
                    bssidSoftap = softapDeviceList.get(i).getBssid();
                    bssidDevice = device.getBssid();
                    if (bssidSoftap.equals(bssidDevice))
                    {
                        timestampConfigure = device.getTimestamp();
                        log.error("timestampConfigure = " + TimeUtil.getDateStr(timestampConfigure, null));
                        if (System.currentTimeMillis() - timestampConfigure < SOFTAP_IGNORE_TIMESTAMP
                            || EspDeviceState.checkValidWithSpecificStates(device.getDeviceState(),
                                EspDeviceState.ACTIVATING))
                        {
                            log.error("device = " + device + " is removed");
                            softapDeviceList.remove(i);
                        }
                        break;
                    }
                }
            }
        }
        
        return softapDeviceList;
    }
    
    @Override
    public List<ScanResult> scanApList(boolean isFilter)
    {
        List<ScanResult> scanResultList = EspBaseApiUtil.scan();
        if (isFilter)
        {
            for (int i = 0; i < scanResultList.size(); ++i)
            {
                String ssid = scanResultList.get(i).SSID;
                if (isESPDevice(ssid))
                {
                    scanResultList.remove(i--);
                }
            }
        }
        return scanResultList;
    }
    
    @Override
    public String doActionGenerateShareKey(String ownerDeviceKey)
    {
        IEspActionDeviceGenerateShareKeyInternet action = new EspActionDeviceGenerateShareKeyInternet();
        return action.doActionDeviceGenerateShareKeyInternet(ownerDeviceKey);
    }
    
    @Override
    public boolean doActionActivateSharedDevice(String sharedDeviceKey)
    {
        IEspActionDeviceActivateSharedInternet action = new EspActionDeviceActivateSharedInternet();
        return action.doActionDeviceActivateSharedInternet(mUserId, mUserKey, sharedDeviceKey);
    }
    
    @Override
    public boolean doActionGetSmsCaptchaCode(String phoneNumber, String state)
    {
        IEspActionGetSmsCaptchaCodeInternet action = new EspActionGetSmsCaptchaCodeInternet();
        return action.doActionGetSmsCaptchaCode(phoneNumber, state);
    }
    
    @Override
    public EspUpgradeDeviceCompatibility checkDeviceCompatibility(IEspDevice device)
    {
        String version = device.getRom_version();
        IEspDeviceCheckCompatibility action = new EspDeviceCheckCompatibility();
        return action.checkDeviceCompatibility(version);
    }
    
    @Override
    public EspUpgradeDeviceTypeResult getDeviceUpgradeTypeResult(IEspDevice device)
    {
        String romVersion = device.getRom_version();
        String latestRomVersion = device.getLatest_rom_version();
        IEspDeviceGetUpgradeTypeResult action = new EspDeviceGetUpgradeTypeResult();
        return action.getDeviceUpgradeTypeResult(romVersion, latestRomVersion);
    }
    
    @Override
    public boolean doActionDeviceTimerGet(IEspDevice device)
    {
        IEspActionDeviceTimerGetInternet action = new EspActionDeviceTimerGetInternet();
        return action.doActionDeviceTimerGet(device);
    }
    
    @Override
    public boolean doActionDeviceTimerPost(IEspDevice device, JSONObject timerJSON)
    {
        IEspActionDeviceTimerPostInternet action = new EspActionDeviceTimerPostInternet();
        return action.doActionDeviceTimerPostInternet(device, timerJSON);
    }
    
    @Override
    public boolean doActionDeviceTimerDelete(IEspDevice device, long timerId)
    {
        IEspActionDeviceTimerDeleteInternet action = new EspActionDeviceTimerDeleteInternet();
        return action.doActionDeviceTimerDeleteInternet(device, timerId);
    }
    
    @Override
    public DeviceInfo doActionDeviceNewConnect(IEspDeviceNew device)
    {
        IEspActionDeviceNewGetInfoLocal action = new EspActionDeviceNewGetInfoLocal();
        return action.doActionNewGetInfoLocal(device);
    }
    
    @Override
    public void doActionDeviceSleepRebootLocal(EspDeviceType type)
    {
        IEspActionDeviceSleepRebootLocal action = new EspActionDeviceSleepRebootLocal();
        action.doActionDeviceSleepRebootLocal(type);
    }
    
    @Override
    public List<IEspStatusHumiture> doActionGetHumitureStatusList(IEspDevice device, long startTimestamp,
        long endTimestamp, long interval)
    {
        IEspActionHumitureGetStatusListInternetDB action = new EspActionHumitureGetStatusListInternetDB();
        long deviceId = device.getId();
        String deviceKey = device.getKey();
        return action.doActionHumitureGetStatusListInternetDB(deviceId,
            deviceKey,
            startTimestamp,
            endTimestamp,
            interval);
    }
    
    @Override
    public List<IEspStatusFlammable> doActionGetFlammableStatusList(IEspDevice device, long startTimestamp,
        long endTimestamp, long interval)
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    class EspDeviceGenericComparator implements Comparator<IEspDevice>
    {
        
        @Override
        public int compare(IEspDevice lhs, IEspDevice rhs)
        {
            String device1Name = lhs.getName();
            String device2Name = rhs.getName();
            /**
             * the order is determined by deviceName firstly
             */
            int result = device1Name.compareTo(device2Name);
            if (result == 0)
            {
                String bssid1 = lhs.getBssid();
                String bssid2 = rhs.getBssid();
                /**
                 * if deviceName is the same, it will determined by the bssid
                 */
                return bssid1.compareTo(bssid2);
            }
            return result;
        }
    }
    
    @Override
    public List<IEspDeviceSSS> __getOriginStaDeviceList()
    {
        return this.mStaDeviceList;
    }
    
    @Override
    public List<IEspDeviceSSS> getStaDeviceList()
    {
        // for the mStaDeviceList maybe changed after the result return,
        // but we don't like UI layer get dirty device list,
        // so we return the copy list to prevent it
        List<IEspDeviceSSS> result = new ArrayList<IEspDeviceSSS>();
        lockUserDeviceLists();
        result.addAll(mStaDeviceList);
        unlockUserDeviceLists();
        return result;
    }
    
    @Override
    public List<IEspDevice> __getOriginDeviceList()
    {
        return mDeviceList;
    }
    
    @Override
    public List<IEspDevice> getAllDeviceList()
    {
        // for the mDeviceList and mStaDeviceList maybe changed after the result return,
        // but we don't like UI layer get dirty device list,
        // so we return the copy list to prevent it
        List<IEspDevice> result = new ArrayList<IEspDevice>();
        lockUserDeviceLists();
        result.addAll(mDeviceList);
        result.addAll(mStaDeviceList);
        unlockUserDeviceLists();
        return result;
    }
    
    @Override
    public List<IEspDeviceNew> getSoftapDeviceList()
    {
        List<IEspDeviceNew> result = scanSoftapDeviceList(false);
        // it has been locked when call getAllDeviceList(),
        // so thre's no need locking here
        List<IEspDevice> allDeviceList = getAllDeviceList();
        // delete the IEspDeviceNew if the device is belong to getAllDeviceList()
        boolean isExist;
        for (int i = 0; i < result.size(); ++i)
        {
            isExist = false;
            String newDeviceBssid = result.get(i).getBssid();
            for (IEspDevice device : allDeviceList)
            {
                if (device.getBssid().equals(newDeviceBssid))
                {
                    isExist = true;
                    break;
                }
            }
            if (isExist)
            {
                result.remove(i--);
            }
        }
        return result;
    }
    
    @Override
    public void lockUserDeviceLists()
    {
        mDeviceListsLock.lock();
    }
    
    @Override
    public void unlockUserDeviceLists()
    {
        mDeviceListsLock.unlock();
    }
    
    @Override
    public boolean addDeviceSyn(final IEspDeviceSSS device)
    {
        log.debug("addDeviceSyn() device:" + device);
        // start add device task asyn
        boolean isAddDeviceAsynSuc = addDeviceAsyn(device);
        if (isAddDeviceAsynSuc)
        {
            // wait the add device task finished
            IEspDeviceStateMachineHandler handler = EspDeviceStateMachineHandler.getInstance();
            try
            {
                while (!handler.isTaskFinished(device.getBssid()))
                {
                    // busy waiting
                    Thread.sleep(500);
                }
                // let UI refresh the device list first
                Thread.sleep(100);
            }
            catch (InterruptedException e)
            {
                return false;
            }
            // check whether the device is added suc
            return handler.isTaskSuc(device.getBssid());
        }
        else
        {
            return false;
        }
    }
    
    @Override
    public boolean addDevicesSyn(final String apSsid, final String apBssid, final String apPassword,
        final boolean isSsidHidden, final boolean requiredActivate)
    {
        log.debug("addDevicesSyn(apSsid=[" + apSsid + "],apBssid=[" + apBssid + "],apPassword=[" + apPassword
            + "],isSsidHidden=[" + isSsidHidden + "],requiredActivate=[" + requiredActivate + "])");
        
        if (!doEsptouchTaskPrepare())
        {
            log.debug("addDevicesSyn fail for doEsptouchTaskPrepare()");
            return false;
        }
        if (mActionDeviceEsptouch.isExecuted())
        {
            log.debug("addDevicesSyn fail for mActionDeviceEsptouch.isExecuted()");
            return false;
        }
        
        List<IEsptouchResult> esptouchResultList =
            doEsptouchTaskSynAddDeviceAsyn(apSsid, apBssid, apPassword, isSsidHidden, requiredActivate);
        boolean isEsptouchSuc = esptouchResultList.get(0).isSuc();
        // when requiredActivate is false, the result is dependent upon isEsptouchSuc
        if (!requiredActivate)
        {
            return isEsptouchSuc;
        }
        
        IEspDeviceStateMachineHandler handler = EspDeviceStateMachineHandler.getInstance();
        while (!handler.isAllTasksFinished())
        {
            try
            {
                // busy waiting
                Thread.sleep(500);
            }
            catch (InterruptedException e)
            {
                return false;
            }
        }
        log.error("addDevicesSyn finish");
        // check whether there's device activating suc
        if (isEsptouchSuc)
        {
            for (IEsptouchResult esptouchResult : esptouchResultList)
            {
                String bssid = BSSIDUtil.restoreBSSID(esptouchResult.getBssid());
                // it is suc as long as one device activating suc
                if (handler.isTaskSuc(bssid))
                {
                    return true;
                }
            }
        }
        return false;
    }
    
    @Override
    public boolean addDeviceAsyn(final IEspDeviceSSS device)
    {
        log.info("addDeviceAsyn() device:" + device);
        IEspDeviceStateMachineHandler handler = EspDeviceStateMachineHandler.getInstance();
        // there's another task about the device is executing, so return false
        if (!handler.isTaskFinished(device.getBssid()))
        {
            return false;
        }
        log.info("addDeviceAsyn() device:" + device + " is finished");
        String randomToken = RandomUtil.random40();
        IEspDeviceConfigure deviceConfigure = device.createConfiguringDevice(randomToken);
        deviceConfigure.setName("Opps: It shouldn't be displayed 1");
        deviceConfigure.setUserId(mUserId);
        log.info("addDeviceAsyn() device:" + device + " before stateMachine");
        IEspDeviceStateMachine stateMachine = EspDeviceStateMachine.getInstance();
        stateMachine.transformState(deviceConfigure, Direction.CONFIGURE);
        log.info("addDeviceAsyn() device:" + device + " after stateMachine");
        return true;
    }
    
    @Override
    public boolean addDevicesAsyn(final String apSsid, final String apBssid, final String apPassword,
        final boolean isSsidHidden, final boolean requiredActivate)
    {
        log.debug("addDevicesAsyn(apSsid=[" + apSsid + "],apBssid=[" + apBssid + "],apPassword=[" + apPassword
            + "],isSsidHidden=[" + isSsidHidden + "],requiredActivate=[" + requiredActivate + "])");
        
        if (!doEsptouchTaskPrepare())
        {
            log.debug("addDevicesAsyn fail for doEsptouchTaskPrepare()");
            return false;
        }
        if (mActionDeviceEsptouch.isExecuted())
        {
            log.debug("addDevicesAsyn fail for mActionDeviceEsptouch.isExecuted()");
            return false;
        }
        
        EspBaseApiUtil.submit(new Runnable()
        {
            @Override
            public void run()
            {
                doEsptouchTaskSynAddDeviceAsyn(apSsid, apBssid, apPassword, isSsidHidden, requiredActivate);
            }
        });
        return true;
    }
    
    private List<IEsptouchResult> doEsptouchTaskSynAddDeviceAsyn(final String apSsid, final String apBssid,
        final String apPassword, final boolean isSsidHidden, boolean requiredActivate)
    {
        log.debug("doEsptouchTaskSynAddDeviceAsyn entrance");
        List<IEsptouchResult> esptouchResultList =
            mActionDeviceEsptouch.doActionDeviceEsptouch(0, apSsid, apBssid, apPassword, isSsidHidden);
        // no matter whether requiredActivate is true or false, we should discover sta devices both
        log.debug("doEsptouchTaskSynAddDeviceAsyn requiredActivate = true");
        if (requiredActivate)
        {
            log.debug("doEsptouchTaskSynAddDeviceAsyn add sta device list last discovered");
            if (!mActionDeviceEsptouch.isCancelled())
            {
                // clear the interrupted by esptouchResultList
                log.debug("doEsptouchTaskSynAddDeviceAsyn clear the interrupted set by esptouch");
                Thread.interrupted();
            }
            else
            {
                // for esptouch configured is cancelled, make first result fail
                log.debug("doEsptouchTaskSynAddDeviceAsyn mActionDeviceEsptouch is cancelled");
                esptouchResultList.clear();
                IEsptouchResult failResult = new EsptouchResult(false, null, null);
                esptouchResultList.add(failResult);
                return esptouchResultList;
            }
            // add sta devices
//            List<IEspDeviceSSS> staDeviceList = getStaDeviceList();
//            for (IEspDeviceSSS staDevice : staDeviceList)
//            {
//                // remove the relevant esptouchResult
//                for (int index = 0; index < esptouchResultList.size(); ++index)
//                {
//                    IEsptouchResult esptouchResult = esptouchResultList.get(index);
//                    // check whether the task is executed suc
//                    if (!esptouchResult.isSuc())
//                    {
//                        break;
//                    }
//                    String bssid1 = staDevice.getBssid();
//                    String bssid2 = BSSIDUtil.restoreBSSID(esptouchResult.getBssid());
//                    log.error("doEsptouchTaskSynAddDeviceAsyn bssid1: " + bssid1 + ",bssid2: " + bssid2);
//                    if (bssid1.equals(bssid2))
//                    {
//                        esptouchResultList.remove(index--);
//                        break;
//                    }
//                }
//                addDeviceAsyn(staDevice);
//            }
            
            // add the esptouch devices
            log.debug("doEsptouchTaskSynAddDeviceAsyn add the remainder esptouchResultList");
            for (IEsptouchResult esptouchResult : esptouchResultList)
            {
                // check whether the task is executed suc
                if (!esptouchResult.isSuc())
                {
                    break;
                }
                // for doActionRefreshStaDevices() can't find them,
                // so we can't get the info like deviceType, etc.
                // thus we can't make them added into staDeviceList
                String bssid = BSSIDUtil.restoreBSSID(esptouchResult.getBssid());
                IOTAddress iotAddress = new IOTAddress(bssid, esptouchResult.getInetAddress());
                iotAddress.setEspDeviceTypeEnum(EspDeviceType.NEW);
                IEspDeviceSSS iotAddressDevice = BEspDevice.createSSSDevice(iotAddress);
                iotAddressDevice.getDeviceState().clearState();
                iotAddressDevice.setName("Opps: It shouldn't be displayed 2");
                addDeviceAsyn(iotAddressDevice);
            }
        }
        else
        {
            doActionRefreshStaDevices(true);
        }
        return esptouchResultList;
    }
    
    private boolean doEsptouchTaskPrepare()
    {
        synchronized (mEsptouchLock)
        {
            if (mIsEsptouchCancelledCount > mIsEsptouchExecuteCount)
            {
                mIsEsptouchCancelledCount = 0;
                mIsEsptouchExecuteCount = 0;
                return false;
            }
            mActionDeviceEsptouch = new EspActionDeviceEsptouch();
            ++mIsEsptouchExecuteCount;
            // counteract
            int min = Math.min(mIsEsptouchCancelledCount, mIsEsptouchExecuteCount);
            mIsEsptouchCancelledCount -= min;
            mIsEsptouchExecuteCount -= min;
        }
        return true;
    }
    
    @Override
    public void cancelAllAddDevices()
    {
        synchronized (mEsptouchLock)
        {
            ++mIsEsptouchCancelledCount;
            if (mActionDeviceEsptouch != null)
            {
                mActionDeviceEsptouch.cancel();
            }
        }
        IEspDeviceStateMachineHandler handler = EspDeviceStateMachineHandler.getInstance();
        handler.cancelAllTasks();
    }
    
}
