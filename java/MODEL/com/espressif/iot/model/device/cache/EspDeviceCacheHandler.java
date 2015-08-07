package com.espressif.iot.model.device.cache;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;

import com.espressif.iot.base.api.EspBaseApiUtil;
import com.espressif.iot.device.IEspDevice;
import com.espressif.iot.device.IEspDeviceNew;
import com.espressif.iot.device.IEspDeviceSSS;
import com.espressif.iot.device.builder.BEspDevice;
import com.espressif.iot.device.cache.IEspDeviceCacheHandler;
import com.espressif.iot.model.device.EspDevice;
import com.espressif.iot.object.IEspSingletonObject;
import com.espressif.iot.type.device.IEspDeviceState;
import com.espressif.iot.type.device.state.EspDeviceState;
import com.espressif.iot.type.net.IOTAddress;
import com.espressif.iot.user.IEspUser;
import com.espressif.iot.user.builder.BEspUser;
import com.espressif.iot.util.BSSIDUtil;

public class EspDeviceCacheHandler implements IEspSingletonObject, IEspDeviceCacheHandler
{
    private final static Logger log = Logger.getLogger(EspDeviceCacheHandler.class);
    
    /*
     * Singleton lazy initialization start
     */
    private EspDeviceCacheHandler()
    {
        mDeviceDBList = new LinkedBlockingQueue<IEspDevice>();
        __executeInsertDeviceListAsyn();
    }
    
    private static class InstanceHolder
    {
        static EspDeviceCacheHandler instance = new EspDeviceCacheHandler();
    }
    
    public static EspDeviceCacheHandler getInstance()
    {
        return InstanceHolder.instance;
    }
    
    /*
     * Singleton lazy initialization end
     */
    
    private BlockingQueue<IEspDevice> mDeviceDBList;
    
    private void __executeInsertDeviceListAsyn()
    {
        EspBaseApiUtil.submit(new Runnable()
        {
            
            @Override
            public void run()
            {
                while (true)
                {
                    try
                    {
                        IEspDevice device = mDeviceDBList.take();
                        if (device.getDeviceState().isStateClear())
                        {
                            device.deleteInDB();
                        }
                        else
                        {
                            device.saveInDB();
                        }
                    }
                    catch (InterruptedException e)
                    {
                        e.printStackTrace();
                    }
                }
            }
            
        });
    }
    
    private void __putDeviceInDBList(IEspDevice device)
    {
        mDeviceDBList.add(device);
    }
    
    private void __handleClearList(List<IEspDevice> userDeviceList)
    {
        for (int i = 0; i < userDeviceList.size(); i++)
        {
            IEspDevice device = userDeviceList.get(i);
            if (device.getDeviceState().isStateClear())
            {
                log.info(Thread.currentThread().toString() + "##__handleClearList device:[" + device
                    + "] is removed from local db");
                __putDeviceInDBList(device);
                log.info(Thread.currentThread().toString() + "##__handleClearList device:[" + device
                    + "] is removed from IUser list");
                userDeviceList.remove(i--);
            }
        }
    }
    
    private boolean handleServerLocal(List<IEspDevice> userDeviceList)
    {
        // poll devices from EspDeviceCache, the state should be OFFLINE,LOCAL,INTERNET or LOCAL,INTERNET coexist
        List<IEspDevice> serverLocaldeviceList = EspDeviceCache.getInstance().pollServerLocalDeviceCacheList();
        boolean isEmptyDevice1Exist = false;
        if (serverLocaldeviceList.isEmpty())
        {
            // Internet unaccessible, don't handleServerLocal
            return false;
        }
        // delete the EmptyDevice1 and EmptyDevice2 from serverLocaldeviceList
        for (int i = 0; i < serverLocaldeviceList.size(); i++)
        {
            if (serverLocaldeviceList.get(i) == EspDevice.EmptyDevice1)
            {
                isEmptyDevice1Exist = true;
                serverLocaldeviceList.remove(i--);
            }
            else if (serverLocaldeviceList.get(i) == EspDevice.EmptyDevice2)
            {
                serverLocaldeviceList.remove(i--);
            }
        }
        
        if (serverLocaldeviceList.isEmpty() && isEmptyDevice1Exist)
        {
            // Keep the userDeviceList in the device list
            return true;
        }
        
        // 1. IUser has, ServerLocal don't have, make IUser's state CLEAR(delete from IUser and delete from local db)
        // 2. IUser doesn't have, ServerLocal have, add into IUser's list(if it doesn't need ignoring)
        // 3. both of IUser and ServerLocal have, but IUser need to ignore(e.g. device is DELETED or ACTIVATING state
        // etc.)
        // 4. both of IUser and ServerLocal have, and IUser don't need to ignore
        for (IEspDevice deviceInUser : userDeviceList)
        {
            IEspDeviceState deviceInUserState = deviceInUser.getDeviceState();
            // 1. IUser has, ServerLocal don't have, make IUser's state CLEAR(delete from IUser and delete from local
            // db)
            if (!serverLocaldeviceList.contains(deviceInUser))
            {
                // if device is configuring, activating, new, don't clear the state
                if ((!deviceInUserState.isStateConfiguring()) && (!deviceInUserState.isStateActivating())
                    && (!deviceInUserState.isStateNew()))
                {
                    log.info(Thread.currentThread().toString() + "##handleServerLocal1 deviceInUser:[" + deviceInUser
                        + "] is clearState");
                    deviceInUserState.clearState();
                }
            }
            else
            {
                // 3. both of IUser and ServerLocal have, but IUser need to ignore(e.g. device is DELETED or ACTIVATING
                // state etc.)
                if (deviceInUserState.isStateActivating() || deviceInUserState.isStateClear()
                    || deviceInUserState.isStateConfiguring() || deviceInUserState.isStateDeleted()
                    || deviceInUserState.isStateUpgradingInternet() || deviceInUserState.isStateUpgradingLocal())
                {
                    // ignore
                    log.info(Thread.currentThread().toString() + "##handleServerLocal3 deviceInUser:[" + deviceInUser
                        + "] is ignored");
                }
                // 4. both of IUser and ServerLocal have, and IUser don't need to ignore
                else
                {
                    IEspDevice serverLocalDevice =
                        serverLocaldeviceList.get(serverLocaldeviceList.indexOf(deviceInUser));
                    log.info(Thread.currentThread().toString() + "##handleServerLocal4 deviceInUser:[" + deviceInUser
                        + "] is update(including name), serverLocalDevice:[" + serverLocalDevice + "]");
                    deviceInUser.copyInetAddress(serverLocalDevice);
                    deviceInUser.copyIsMeshDevice(serverLocalDevice);
                    deviceInUser.copyParentDeviceBssid(serverLocalDevice);
                    
                    // it must before deviceInUser.copyDeviceState, or the Renamed state will be cleared
                    boolean isRenamed = deviceInUserState.isStateRenamed();
                    if (isRenamed || deviceInUser.__isDeviceRefreshed())
                    {
                        deviceInUser.__clearDeviceRefreshed();
                    }
                    else
                    {
                        deviceInUser.copyDeviceName(serverLocalDevice);
                    }
                    deviceInUser.copyDeviceState(serverLocalDevice);
                    // don't forget to add Renamed State
                    if (isRenamed)
                    {
                        deviceInUser.getDeviceState().addStateRenamed();
                    }
                    deviceInUser.copyDeviceRomVersion(serverLocalDevice);
                    __putDeviceInDBList(deviceInUser);
                }
            }
        }
        // 2. IUser doesn't have, ServerLocal have, add into IUser's list(if it doesn't need ignoring)
        for (IEspDevice serverLocalUser : serverLocaldeviceList)
        {
            if (!userDeviceList.contains(serverLocalUser))
            {
                // if the device is activating or activating fail, don't add it into IUser
                // activating device's state: ACTIVATING
                // activating fail device's state: ACTIVATING DELETED
                IEspDevice similarDevice = null;
                for (IEspDevice device2 : userDeviceList)
                {
                    if (device2.isSimilar(serverLocalUser))
                    {
                        similarDevice = device2;
                        log.info(Thread.currentThread().toString() + "##handleServerLocal2 serverLocalUser:["
                            + serverLocalUser + "] is ignored");
                        break;
                    }
                }
                if (similarDevice == null)
                {
                    log.info(Thread.currentThread().toString() + "##handleServerLocal2 serverLocalUser:["
                        + serverLocalUser + "] is added in IUser list and local db");
                    userDeviceList.add(serverLocalUser);
                    __putDeviceInDBList(serverLocalUser);
                }
            }
        }
        return true;
    }
    
    private void __handleLocal(List<IEspDevice> userDeviceList, List<IOTAddress> localIOTAddressList,
        boolean clearInternetState)
    {
        // make userDeviceList device state OFFLINE if LOCAL or INTERNET
        for (IEspDevice userDevice : userDeviceList)
        {
            IEspDeviceState deviceState = userDevice.getDeviceState();
            // only process INTERNET and LOCAL
            if (deviceState.isStateLocal() || deviceState.isStateInternet())
            {
                if (clearInternetState)
                {
                    deviceState.clearStateInternet();
                    deviceState.clearStateLocal();
                    deviceState.addStateOffline();
                }
                else
                {
                    deviceState.clearStateLocal();
                    if (!deviceState.isStateInternet())
                    {
                        deviceState.addStateOffline();
                    }
                }
            }
        }
        // only process OFFLINE(when clearInternetState is true)
        // only process OFFLINE and INTERNET(when clearInternetState is false)
        for (IOTAddress localIOTAddress : localIOTAddressList)
        {
            for (IEspDevice userDevice : userDeviceList)
            {
                IEspDeviceState deviceState = userDevice.getDeviceState();
                if (clearInternetState)
                {
                    if (!deviceState.isStateOffline())
                    {
                        // ignore
                        continue;
                    }
                }
                else
                {
                    if (!deviceState.isStateOffline() && !deviceState.isStateInternet())
                    {
                        // ignore
                        continue;
                    }
                }
                
                String bssid1 = localIOTAddress.getBSSID();
                String bssid2 = userDevice.getBssid();
                if (bssid1.equals(bssid2))
                {
                    deviceState.clearStateOffline();
                    deviceState.addStateLocal();
                    // fix the bug when device is only local, the apk will crash
                    userDevice.setInetAddress(localIOTAddress.getInetAddress());
                    userDevice.setIsMeshDevice(localIOTAddress.isMeshDevice());
                    userDevice.setParentDeviceBssid(localIOTAddress.getParentBssid());
                }
            }
        }
    }
    
    private void handleLocal(List<IEspDevice> userDeviceList)
    {
        log.debug(Thread.currentThread().toString() + "##handleLocal()");
        List<IOTAddress> localIOTAddressList = EspDeviceCache.getInstance().pollLocalDeviceCacheList();
        __handleLocal(userDeviceList, localIOTAddressList, true);
    }
    
    private void handleUpgradeLocalSuc(List<IEspDevice> userDeviceList)
    {
        log.debug(Thread.currentThread().toString() + "##handleUpgradeLocalSuc()");
        List<IOTAddress> localUpgradeSucIOTAddressList =
            EspDeviceCache.getInstance().pollUpgradeSucLocalDeviceCacheList();
        if (localUpgradeSucIOTAddressList.isEmpty())
        {
            return;
        }
        IEspDeviceState deviceState;
        // clear all devices of userDeviceList local state
        for (IEspDevice userDevice : userDeviceList)
        {
            deviceState = userDevice.getDeviceState();
            if (deviceState.isStateLocal())
            {
                deviceState.clearStateLocal();
                if (!deviceState.isStateInternet())
                {
                    // don't forget to make device offline
                    deviceState.addStateOffline();
                }
            }
        }
        // add local state to the devices of userDeviceList, which device is in localUpgradeSucIOTAddressList
        for (IOTAddress localUpgradeSucIOTAddress : localUpgradeSucIOTAddressList)
        {
            for (IEspDevice userDevice : userDeviceList)
            {
                deviceState = userDevice.getDeviceState();
                // only process OFFLINE or INTERNET
                if (!(deviceState.isStateOffline() || deviceState.isStateInternet()))
                {
                    // ignore
                    continue;
                }
                String bssid1 = localUpgradeSucIOTAddress.getBSSID();
                String bssid2 = userDevice.getBssid();
                if (bssid1.equals(bssid2))
                {
                    deviceState.clearStateOffline();
                    deviceState.addStateLocal();
                    userDevice.setInetAddress(localUpgradeSucIOTAddress.getInetAddress());
                    userDevice.setIsMeshDevice(localUpgradeSucIOTAddress.isMeshDevice());
                    userDevice.setParentDeviceBssid(localUpgradeSucIOTAddress.getParentBssid());
                    break;
                }
            }
        }
    }
    
    private void handleStatemachine(List<IEspDevice> userDeviceList)
    {
        // poll devices from EspDeviceCache
        List<IEspDevice> stateMachineDeviceList = new ArrayList<IEspDevice>();
        IEspDevice device = EspDeviceCache.getInstance().pollStatemachineDeviceCache();
        while (device != null)
        {
            stateMachineDeviceList.add(device);
            device = EspDeviceCache.getInstance().pollStatemachineDeviceCache();
        }
        boolean isExecuted = false;
        // a. handle DELETED(&&(CONFIGURING||ACTIVATING)), CONFIGURING, ACTIVATING
        // b. handle IUser activating
        // c. handle others (just copy device state, rom version and device name)
        
        // a. handle DELETED(&&(CONFIGURING||ACTIVATING)), CONFIGURING, ACTIVATING
        for (int i = 0; i < stateMachineDeviceList.size(); i++)
        {
            isExecuted = false;
            IEspDevice stateMachineDevice = stateMachineDeviceList.get(i);
            IEspDeviceState deviceStateMachineState = stateMachineDevice.getDeviceState();
            if (EspDeviceState.checkValidWithSpecificStates(deviceStateMachineState,
                EspDeviceState.DELETED,
                EspDeviceState.CONFIGURING)
                || EspDeviceState.checkValidWithSpecificStates(deviceStateMachineState,
                    EspDeviceState.DELETED,
                    EspDeviceState.ACTIVATING))
            {
                log.debug("handleStatemachine() deviceStateMachineState.isStateDeleted() and (isStateConfiguring() or isStateActivating()");
                isExecuted = true;
                // configuring or activating fail
                for (IEspDevice deviceInUser : userDeviceList)
                {
                    // if (deviceInUser.getId() == stateMachineDevice.getId())
                    if (deviceInUser.equals(stateMachineDevice))
                    {
                        deviceInUser.copyDeviceState(stateMachineDevice);
                        break;
                    }
                }
            }
            else if (EspDeviceState.checkValidWithSpecificStates(deviceStateMachineState, EspDeviceState.CONFIGURING))
            {
                log.debug("handleStatemachine() deviceStateMachineState.isStateConfiguring()");
                isExecuted = true;
                // clear device if the similar but not equal device is exist in IUser
                // clear the device which isn't activating
                for (IEspDevice deviceInUser : userDeviceList)
                {
                    if (deviceInUser.isSimilar(stateMachineDevice) && !deviceInUser.equals(stateMachineDevice))
                    {
                        deviceInUser.getDeviceState().clearState();
                    }
                }
                // stop all activating tasks
                for (IEspDevice deviceInUser : userDeviceList)
                {
                    if (EspDeviceState.checkValidWithSpecificStates(deviceInUser.getDeviceState(),
                        EspDeviceState.ACTIVATING))
                    {
                        // when adding device by esptouch, deviceInUser is IEspDeviceConfigure
                        if(deviceInUser instanceof IEspDeviceNew)
                        {
                            IEspDeviceNew deviceNew = (IEspDeviceNew)deviceInUser;
                            deviceNew.cancel(true);
                        }
                    }
                }
                // delete the same activating device from userDeviceList
                for (int j = 0; j < userDeviceList.size(); j++)
                {
                    IEspDevice deviceInUser = userDeviceList.get(j);
                    if (deviceInUser.equals(stateMachineDevice))
                    {
                        userDeviceList.remove(j--);
                        break;
                    }
                }
                // add device in IUser
                userDeviceList.add(stateMachineDevice);
            }
            else if (EspDeviceState.checkValidWithSpecificStates(deviceStateMachineState, EspDeviceState.ACTIVATING))
            {
                log.debug("handleStatemachine() deviceStateMachineState.isStateActivating()");
                isExecuted = true;
                // change CONFIRURING to ACTIVATING
                for (IEspDevice deviceInUser : userDeviceList)
                {
                    // IEspDeviceNew support only one CONFIGURING device,
                    // IEspDeviceConfigure support more than one CONFIGURING device
                    if (EspDeviceState.checkValidWithSpecificStates(deviceInUser.getDeviceState(),
                        EspDeviceState.CONFIGURING) && deviceInUser.isSimilar(stateMachineDevice))
                    {
                        deviceInUser.copyDeviceState(stateMachineDevice);
                        deviceInUser.copyTimestamp(stateMachineDevice);
                        break;
                    }
                }
                // resume all activating tasks
                for (IEspDevice deviceInUser : userDeviceList)
                {
                    // only IEspDeviceNew require resume
                    if (deviceInUser instanceof IEspDeviceNew)
                    {
                        if (EspDeviceState.checkValidWithSpecificStates(deviceInUser.getDeviceState(),
                            EspDeviceState.ACTIVATING) && deviceInUser.isSimilar(stateMachineDevice))
                        {
                            IEspDeviceNew deviceNew = (IEspDeviceNew)deviceInUser;
                            deviceNew.resume();
                        }
                    }
                }
                
            }
            if (isExecuted)
            {
                stateMachineDeviceList.remove(i--);
            }
        }
        
        // b. handle IUser activating
        for (int i = 0; i < userDeviceList.size(); i++)
        {
            IEspDevice deviceInUser = userDeviceList.get(i);
            if (EspDeviceState.checkValidWithSpecificStates(deviceInUser.getDeviceState(), EspDeviceState.ACTIVATING))
            {
                for (int j = 0; j < stateMachineDeviceList.size(); j++)
                {
                    IEspDevice deviceInStateMachine = stateMachineDeviceList.get(j);
                    if (deviceInUser.isSimilar(deviceInStateMachine))
                    {
                        // replace deviceInUser with deviceInStateMachine
                        userDeviceList.remove(i--);
                        userDeviceList.add(deviceInStateMachine);
                        // delete the device from deviceInStateMachine
                        stateMachineDeviceList.remove(j--);
                        // don't forget to save in db,
                        deviceInStateMachine.saveInDB();
                    }
                }
            }
        }
        
        // c. handle others (just copy device state, rom version and device name)
        for (IEspDevice stateMachineDevice : stateMachineDeviceList)
        {
            System.out.println("bh c. stateMachineDevice: " + stateMachineDevice + " handle others");
            if (userDeviceList.contains(stateMachineDevice))
            {
                System.out.println("bh c. stateMachineDevice: " + stateMachineDevice + " handle others contained");
                IEspDevice userDevice = userDeviceList.get(userDeviceList.indexOf(stateMachineDevice));
                userDevice.copyDeviceState(stateMachineDevice);
                userDevice.copyDeviceRomVersion(stateMachineDevice);
                userDevice.copyDeviceName(stateMachineDevice);
                if (!stateMachineDevice.getDeviceState().isStateClear())
                {
                    System.out.println("bh c. stateMachineDevice: " + stateMachineDevice + " handle others saveInDB");
                    log.error("userDevice: " + userDevice + ",stateMachineDevice: " + stateMachineDevice);
                    // don't forget to save in db
                    userDevice.saveInDB();
                }
            }
        }
    }
    
    private void handleShared(List<IEspDevice> userDeviceList)
    {
        log.debug(Thread.currentThread().toString() + "##handlerShared()");
        IEspDevice device = EspDeviceCache.getInstance().pollSharedDeviceCache();
        while (device != null)
        {
            for (int i = 0; i < userDeviceList.size(); i++)
            {
                IEspDevice deviceInUser = userDeviceList.get(i);
                if (deviceInUser.equals(device))
                {
                    device = null;
                    break;
                }
            }
            
            if (device != null)
            {
                userDeviceList.add(device);
                __putDeviceInDBList(device);
            }
            device = EspDeviceCache.getInstance().pollSharedDeviceCache();
        }
    }
    
    private void handleTransformed(List<IEspDevice> userDeviceList)
    {
        log.debug(Thread.currentThread().toString() + "##handleTransformed()");
        List<IEspDevice> deviceList = EspDeviceCache.getInstance().pollTransformedDeviceCacheList();
        for (IEspDevice deviceInList : deviceList)
        {
            for (IEspDevice deviceInUserList : userDeviceList)
            {
                if (deviceInUserList.equals(userDeviceList))
                {
                    deviceInUserList = deviceInList;
                    break;
                }
            }
        }
    }
    
    private void handleSta(List<IEspDevice> userDeviceList, List<IEspDeviceSSS> userStaDeviceList)
    {
        log.debug(Thread.currentThread().toString() + "##handleSta()");
        List<IOTAddress> staDeviceList = EspDeviceCache.getInstance().pollStaDeviceCacheList();
        // refresh local device state
        __handleLocal(userDeviceList, staDeviceList, false);
        // check whether it's necessary to handle sta
        if (staDeviceList.isEmpty())
        {
            log.info("##handleSta() staDeviceList is empty, return");
            return;
        }
        
        userStaDeviceList.clear();
        boolean isExist;
        for (int index = 0;index < staDeviceList.size(); ++index)
        {
            IOTAddress staDevice = staDeviceList.get(index);
            // when receive IOTAddress.EmptyIOTAddress means that the lately discover local result is empty,
            // so we should clear the added device in userStaDeviceList
            if (staDevice == IOTAddress.EmptyIOTAddress)
            {
                userStaDeviceList.clear();
                staDeviceList.remove(index--);
                continue;
            }
            isExist = false;
            for (IEspDevice userDevice : userDeviceList)
            {
                if (userDevice.getDeviceState().isStateDeleted())
                {
                    log.debug("#handleSta() ignore deleted userDevice: " + userDevice.getBssid());
                    continue;
                }
                if (userDevice.getBssid().equals(staDevice.getBSSID()))
                {
                    log.debug("##handleSta() device: " + userDevice.getBssid() + " is exist already");
                    isExist = true;
                    break;
                }
            }
            if (!isExist)
            {
                // generate ssid by bssid and whether the device is mesh
                String bssid = staDevice.getBSSID();
                String prefix = staDevice.isMeshDevice() ? "espressif_" : "ESP_";
                String ssid = BSSIDUtil.genDeviceNameByBSSID(prefix, bssid);
                staDevice.setSSID(ssid);
                // generate IEspDeviceSSS and add it into userStaDeviceList
                IEspDeviceSSS userStaDevice = BEspDevice.createSSSDevice(staDevice);
                log.info("##handleSta() add device: " + userStaDevice);
                userStaDeviceList.add(userStaDevice);
            }
        }
    }
    
    private void handleUserDevices(List<IEspDevice> userDeviceList)
    {
        log.debug(Thread.currentThread().toString() + "##handleUserDevices()");
        for (IEspDevice device : userDeviceList)
        {
            IEspDeviceState deviceState = device.getDeviceState();
            if ((!deviceState.isStateLocal()) && (!deviceState.isStateInternet()))
            {
                // clear parent device bssid
                device.setParentDeviceBssid(null);
                device.setIsMeshDevice(false);
            }
        }
    }
    
    @Override
    public synchronized Void handleUninterruptible(boolean isStateMachine)
    {
        IEspUser user = BEspUser.getBuilder().getInstance();
        user.lockUserDeviceLists();
        List<IEspDevice> userDeviceList = user.__getOriginDeviceList();
        List<IEspDeviceSSS> userStaDeviceList = user.__getOriginStaDeviceList();
        log.debug(Thread.currentThread().toString() + "##handleUninterruptible(userDeviceList=[" + userDeviceList
            + "])");
        if (isStateMachine)
        {
            handleStatemachine(userDeviceList);
        }
        else
        {
            boolean isExecuted = handleServerLocal(userDeviceList);
            if (!isExecuted)
            {
                handleLocal(userDeviceList);
            }
        }
        handleShared(userDeviceList);
        handleUpgradeLocalSuc(userDeviceList);
        handleTransformed(userDeviceList);
        
        // handle device in CLEAR state
        __handleClearList(userDeviceList);
        
        // handle user's sta device list
        handleSta(userDeviceList, userStaDeviceList);
        // clear parent device bssid if it isn't local or internet
        handleUserDevices(userDeviceList);
        user.unlockUserDeviceLists();
        return null;
    }
    
}
