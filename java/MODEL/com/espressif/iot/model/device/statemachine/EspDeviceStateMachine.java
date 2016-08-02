package com.espressif.iot.model.device.statemachine;

import java.net.InetAddress;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;

import com.espressif.iot.action.device.common.EspActionDeviceDeleteInternet;
import com.espressif.iot.action.device.common.EspActionDeviceRenameInternet;
import com.espressif.iot.action.device.common.IEspActionDeviceDeleteInternet;
import com.espressif.iot.action.device.common.IEspActionDeviceRenameInternet;
import com.espressif.iot.action.device.common.upgrade.EspActionDeviceUpgradeLocal;
import com.espressif.iot.action.device.common.upgrade.EspActionDeviceUpgradeOnline;
import com.espressif.iot.action.device.common.upgrade.IEspActionDeviceUpgradeLocal;
import com.espressif.iot.action.device.common.upgrade.IEspActionDeviceUpgradeOnline;
import com.espressif.iot.base.api.EspBaseApiUtil;
import com.espressif.iot.db.IOTApDBManager;
import com.espressif.iot.device.IEspDevice;
import com.espressif.iot.device.IEspDeviceConfigure;
import com.espressif.iot.device.IEspDeviceNew;
import com.espressif.iot.device.statemachine.IEspDeviceStateMachine;
import com.espressif.iot.model.device.cache.EspDeviceCache;
import com.espressif.iot.model.device.statemachine.IEspDeviceStateMachineHandler.ITaskActivateInternet;
import com.espressif.iot.model.device.statemachine.IEspDeviceStateMachineHandler.ITaskActivateLocal;
import com.espressif.iot.object.IEspSingletonObject;
import com.espressif.iot.type.device.IEspDeviceState;
import com.espressif.iot.type.device.state.EspDeviceState;
import com.espressif.iot.type.net.IOTAddress;
import com.espressif.iot.type.net.WifiCipherType;
import com.espressif.iot.user.IEspUser;
import com.espressif.iot.user.builder.BEspUser;
import com.espressif.iot.device.cache.IEspDeviceCache.NotifyType;

public class EspDeviceStateMachine implements IEspDeviceStateMachine, IEspSingletonObject
{
    private final static Logger log = Logger.getLogger(EspDeviceStateMachine.class);
    
    /*
     * Singleton lazy initialization start
     */
    private EspDeviceStateMachine()
    {
        _bssidCallableFutureMap = new ConcurrentHashMap<String, CallableFuture>();
    }
    
    private static class InstanceHolder
    {
        static EspDeviceStateMachine instance = new EspDeviceStateMachine();
    }
    
    public static EspDeviceStateMachine getInstance()
    {
        return InstanceHolder.instance;
    }
    
    /*
     * Singleton lazy initialization end
     */
    
    // store device's bssid and its task
    private final Map<String, CallableFuture> _bssidCallableFutureMap;
    
    private class CallableFuture
    {
        public Callable<?> _callable;
        
        public Future<?> _future;
        
        CallableFuture(Callable<?> callable, Future<?> future)
        {
            _callable = callable;
            _future = future;
        }
    }
    
    private Future<?> __submitTask(final Callable<?> task, final Runnable taskSuc, final Runnable taskFail,
        final Runnable taskCancel)
    {
        return EspBaseApiUtil.submit(task, taskSuc, taskFail, taskCancel);
    }
    
    private void __resumeAllTasks()
    {
        Set<String> bssidSet = _bssidCallableFutureMap.keySet();
        CallableFuture callableFuture = null;
        for (String bssidInSet : bssidSet)
        {
            callableFuture = _bssidCallableFutureMap.get(bssidInSet);
            // if the future isDone() means the action fail, it needs redoing
            if (callableFuture._future.isDone())
            {
                // execute the old callable
                __submitTask(callableFuture._callable, null, null, null);
                log.info(Thread.currentThread().toString()
                    + "##__resumeOldTasks(): old task is submitted again.(bssid=" + bssidInSet + ")");
            }
        }
    }
    
    private void __cancelAllTasks()
    {
        Set<String> bssidSet = _bssidCallableFutureMap.keySet();
        CallableFuture callableFuture = null;
        for (String bssidInSet : bssidSet)
        {
            callableFuture = _bssidCallableFutureMap.get(bssidInSet);
            if (callableFuture._future != null)
            {
                callableFuture._future.cancel(true);
                log.info(Thread.currentThread().toString()
                    + "##__cancelOldTasks(): old task is submitted again.(bssid=" + bssidInSet + ")");
            }
        }
    }
    
    private void __addBssidTask(final String bssid, final Callable<?> task)
    {
        // cancel the executing old task
        __cancelAllTasks();
        Future<?> future = __submitTask(task, null, null, null);
        log.info(Thread.currentThread().toString() + "##__addBssidTask(bssid=[" + bssid
            + "]): task is submitted.(bssid=" + bssid + ")");
        CallableFuture callableFuture = new CallableFuture(task, future);
        // put into _bssidCallableFutureMap
        _bssidCallableFutureMap.put(bssid, callableFuture);
        // execute all of the old task and new task
        __resumeAllTasks();
    }
    
    private class DefaultTaskFail implements Runnable
    {
        private final IEspDevice _device;
        
        DefaultTaskFail(final IEspDevice device)
        {
            _device = device;
        }
        
        @Override
        public void run()
        {
            transformState(_device, Direction.FAIL);
        }
    }
    
    private void __activate(final IEspDevice device, final IEspDevice deviceStateMachine)
    {
        if (device instanceof IEspDeviceConfigure)
        {
            log.debug(Thread.currentThread().toString() + "##__activate IEspDeviceConfigure");
            IEspDeviceStateMachineHandler handler = EspDeviceStateMachineHandler.getInstance();
            ITaskActivateInternet task = handler.createTaskActivateInternet((IEspDeviceConfigure)device);
            handler.addTask(task);
            return;
        }
        log.debug(Thread.currentThread().toString() + "##__activate(deviceStateMachine=[" + deviceStateMachine + "])");
        Callable<?> task = new Callable<IEspDevice>()
        {
            @Override
            public IEspDevice call()
                throws Exception
            {
                // do activate device command(connect to the device and configure it)
                IEspDeviceNew deviceNew = (IEspDeviceNew)deviceStateMachine;
                IEspUser user = BEspUser.getBuilder().getInstance();
                long userId = user.getUserId();
                String userKey = user.getUserKey();
                String randomToken = deviceNew.getKey();
                long negativeDeviceId = deviceNew.getId();
                String deviceName = deviceNew.getName();
                IEspDevice result =
                    deviceNew.doActionDeviceNewActivateInternet(userId, userKey, randomToken, negativeDeviceId);
                if (!result.getName().equals(deviceName))
                {
                    result.setName(deviceName);
                    user.doActionRename(result, deviceName);
                }
                IOTApDBManager apDBManager = IOTApDBManager.getInstance();
                if (result != null)
                {
                    apDBManager.updateApInfo(deviceNew.getBssid(), true);
                    
                    user.saveNewActivatedDevice(result.getKey());
                    transformState(result, Direction.SUC);
                    return result;
                }
                // note: if fail, must return null instead of False
                else
                {
                    apDBManager.updateApInfo(deviceNew.getBssid(), false);
                    return null;
                }
            }
        };
        Runnable taskSuc = null;
        Runnable taskFail = new DefaultTaskFail(deviceStateMachine);
        Future<?> future = __submitTask(task, taskSuc, taskFail, null);
        IEspDeviceNew deviceNew = (IEspDeviceNew)device;
        deviceNew.setFuture(future);
    }
    
    private void __configure(final IEspDevice device, final IEspDevice deviceStateMachine)
    {
        if (device instanceof IEspDeviceConfigure)
        {
            log.debug(Thread.currentThread().toString() + "##__configure IEspDeviceConfigure");
            IEspDeviceStateMachineHandler handler = EspDeviceStateMachineHandler.getInstance();
            ITaskActivateLocal task = handler.createTaskActivateLocal((IEspDeviceConfigure)device);
            handler.addTask(task);
            return;
        }
        log.debug(Thread.currentThread().toString() + "##__configure(deviceStateMachine=[" + deviceStateMachine + "])");
        Callable<?> task = new Callable<Boolean>()
        {
            @Override
            public Boolean call()
                throws Exception
            {
                log.debug("__configure start");
                EspDeviceStateMachine.this.__cancelAllTasks();
                IEspDeviceNew deviceNew = (IEspDeviceNew)deviceStateMachine;
                // do configure device command(connect to the device and configure it)
                String deviceBssid = deviceNew.getBssid();
                String deviceSsid = deviceNew.getSsid();
                WifiCipherType deviceWifiCipherType = deviceNew.getWifiCipherType();
                String devicePassword = deviceNew.getDefaultPassword();
                String apSsid = deviceNew.getApSsid();
                WifiCipherType apWifiCipherType = deviceNew.getApWifiCipherType();
                String apPassword = deviceNew.getApPassword();
                String randomToken = deviceNew.getKey();
                long deviceId =
                    deviceNew.doActionDeviceNewConfigureLocal(deviceBssid,
                        deviceSsid,
                        deviceWifiCipherType,
                        devicePassword,
                        apSsid,
                        apWifiCipherType,
                        apPassword,
                        randomToken);
                if (deviceId < 0)
                {
                    log.info("__configure suc");
                    return true;
                }
                // note: if fail, must return null instead of False
                else
                {
                    log.warn("__configure fail");
                    return null;
                }
            }
        };
        Runnable taskSuc = new Runnable()
        {
            @Override
            public void run()
            {
                EspDeviceStateMachine.this.__resumeAllTasks();
                transformState(deviceStateMachine, Direction.ACTIVATE);
            }
        };
        Runnable taskFail = new Runnable()
        {
            @Override
            public void run()
            {
                EspDeviceStateMachine.this.__resumeAllTasks();
                transformState(deviceStateMachine, Direction.FAIL);
            }
        };
        Runnable taskCancel = new Runnable()
        {
            @Override
            public void run()
            {
                EspDeviceStateMachine.this.__resumeAllTasks();
                /**
                 * restore the device's state to the New State and remove it from IUser's device list, just like get it
                 * from @see EspUser's scanSoftapDeviceList()
                 */
                device.getDeviceState().clearState();
                device.getDeviceState().addStateNew();
                IEspUser user = BEspUser.getBuilder().getInstance();
                boolean suc = user.getDeviceList().remove(device);
                log.warn("cancel suc:" + suc);
            }
        };
        Future<?> future = __submitTask(task, taskSuc, taskFail, taskCancel);
        IEspDeviceNew deviceNew = (IEspDeviceNew)device;
        deviceNew.setFuture(future);
    }
    
    private void __delete(final IEspDevice device)
    {
        log.debug(Thread.currentThread().toString() + "##__delete(device=[" + device + "])");
        final String bssid = device.getBssid();
        Callable<?> task = new Callable<Boolean>()
        {
            @Override
            public Boolean call()
                throws Exception
            {
                boolean result = false;
                if(device.isActivated())
                {
                    // do delete internet command
                    IEspActionDeviceDeleteInternet command = new EspActionDeviceDeleteInternet();
                    String deviceKey = device.getKey();
                    result = command.doActionDeviceDeleteInternet(deviceKey);
                }
                else
                {
                    // when device isn't activated, it will never fail
                    result = true;
                }
                if (result)
                {
                    transformState(device, Direction.SUC, NotifyType.STATE_MACHINE_BACKSTATE);
                    _bssidCallableFutureMap.remove(bssid);
                    log.info(Thread.currentThread().toString() + "##__delete(device=[" + device + "]):"
                        + "_bssidCallableFutureMap.remove()");
                }
                if (result)
                {
                    return true;
                }
                // note: if fail, must return null instead of False
                else
                {
                    return null;
                }
            }
        };
        __addBssidTask(bssid, task);
    }
    
    private void __rename(final IEspDevice device)
    {
        log.debug(Thread.currentThread().toString() + "##__rename(device=[" + device + "])");
        final String bssid = device.getBssid();
        Callable<?> task = new Callable<Boolean>()
        {
            @Override
            public Boolean call()
                throws Exception
            {
                log.error("__rename: " + device.getName());
                boolean result = false;
                if(device.isActivated())
                {
                    // do rename device internet command
                    IEspActionDeviceRenameInternet command = new EspActionDeviceRenameInternet();
                    String deviceKey = device.getKey();
                    String deviceName = device.getName();
                    result = command.doActionDeviceRenameInternet(deviceKey, deviceName);
                }
                else
                {
//                    device.saveInDB();
                    // when device isn't activated, it will never fail
                    result = true;
                }
                
                if (result)
                {
                    transformState(device, Direction.SUC, NotifyType.STATE_MACHINE_BACKSTATE);
                    _bssidCallableFutureMap.remove(bssid);
                    log.info(Thread.currentThread().toString() + "##__rename(device=[" + device + "]):"
                        + "_bssidCallableFutureMap.remove()");
                }
                if (result)
                {
                    return true;
                }
                // note: if fail, must return null instead of False
                else
                {
                    return null;
                }
            }
        };
        __addBssidTask(bssid, task);
    }
    
    private void __upgradeInternet(final IEspDevice device)
    {
        log.debug(Thread.currentThread().toString() + "##__upgradeInternet(device=[" + device + "])");
        Callable<?> task = new Callable<IEspDevice>()
        {
            @Override
            public IEspDevice call()
                throws Exception
            {
                // do upgrade device online action(tell the Server to upgrade the device, and wait device upgrade suc)
                IEspActionDeviceUpgradeOnline action = new EspActionDeviceUpgradeOnline();
                String deviceKey = device.getKey();
                String latestRomVersion = device.getLatest_rom_version();
                int rssi = device.getRssi();
                String info = device.getInfo();
                IEspDevice result = action.doUpgradeOnline(deviceKey, latestRomVersion);
                if (result != null)
                {
                    result.setRssi(rssi);
                    result.setInfo(info);
                    // don't discard the rename state and __isDeviceRefreshed
                    if (device.getDeviceState().isStateRenamed())
                    {
                        result.getDeviceState().addStateRenamed();
                    }
                    if (device.__isDeviceRefreshed())
                    {
                        result.__setDeviceRefreshed();
                    }
                    log.debug(Thread.currentThread().toString() + "##__upgradeInternet(result=[" + result + "])");
                    transformState(result, Direction.SUC);
                    return result;
                }
                // note: if fail, must return null instead of False
                else
                {
                    log.debug(Thread.currentThread().toString() + "##__upgradeInternet(result=[" + result + "])");
                    return null;
                }
            }
        };
        Runnable taskSuc = null;
        Runnable taskFail = new DefaultTaskFail(device);
        __submitTask(task, taskSuc, taskFail, null);
    }
    
    private void __upgradeLocal(final IEspDevice device)
    {
        log.debug(Thread.currentThread().toString() + "##__upgradeLocal(device=[" + device + "])");
        Callable<?> task = new Callable<Boolean>()
        {
            @Override
            public Boolean call()
                throws Exception
            {
                // do upgrade device online action(tell the Server to upgrade the device, and wait device upgrade suc)
                IEspActionDeviceUpgradeLocal action = new EspActionDeviceUpgradeLocal();
                InetAddress inetAddress = device.getInetAddress();
                String bssid = device.getBssid();
                String deviceKey = device.getKey();
                String latestRomVersion = device.getLatest_rom_version();
                int rssi = device.getRssi();
                String info = device.getInfo();
                boolean isMeshDevice = device.getIsMeshDevice();
                boolean isSuc;
                IOTAddress iotAddressResult = null;
                List<IOTAddress> localDeviceList = null;
                if (isMeshDevice)
                {
                    // for the mesh device upgrade local suc, it will be reset, not only itself will be changed,
                    // but also other device's local state will be changed
                    localDeviceList =
                        action.doUpgradeMeshDeviceLocal(inetAddress, bssid, deviceKey, latestRomVersion);
                    isSuc = localDeviceList != null;
                    // get iotAddressResult from the list result
                    if (localDeviceList != null)
                    {
                        for (IOTAddress iotAddress : localDeviceList)
                        {
                            // the localDeviceList contains the result certainly
                            if(iotAddress.getBSSID().equals(bssid))
                            {
                                iotAddressResult = iotAddress;
                                break;
                            }
                        }
                    }
                }
                else
                {
                    // for the device upgrade local suc, it will be reset, the ip address may be changed
                    iotAddressResult = action.doUpgradeLocal(inetAddress, bssid, deviceKey, latestRomVersion);
                    isSuc = iotAddressResult != null;
                }
                if (isSuc)
                {
                    log.debug(Thread.currentThread().toString() + "##__upgradeLocal(device=[" + device + "]): suc");
                    device.setRom_version(latestRomVersion);
                    device.setRssi(rssi);
                    device.setInfo(info);
                    device.setInetAddress(iotAddressResult.getInetAddress());
                    device.setIsMeshDevice(iotAddressResult.isMeshDevice());
                    device.setParentDeviceBssid(iotAddressResult.getParentBssid());
                    if (device.getIsMeshDevice())
                    {
                        __transformStateMeshUpgradeLocalSuc(device, localDeviceList, Direction.SUC);
                    }
                    else
                    {
                        transformState(device, Direction.SUC);
                    }
                    return isSuc;
                }
                // note: if fail, must return null instead of False
                else
                {
                    log.debug(Thread.currentThread().toString() + "##__upgradeLocal(device=[" + device + "]): fail");
                    return null;
                }
            }
        };
        Runnable taskSuc = null;
        Runnable taskFail = new DefaultTaskFail(device);
        __submitTask(task, taskSuc, taskFail, null);
    }
    
    private void __transformStateMeshUpgradeLocalSuc(final IEspDevice upgradeLoalSucDevice,
        final Collection<IOTAddress> localDeviceList, final Direction direction)
    {
        EspDeviceCache deviceCache = EspDeviceCache.getInstance();
        deviceCache.addUpgradeSucLocalDeviceCacheList((List<IOTAddress>)localDeviceList);
        transformState(upgradeLoalSucDevice, direction);
    }
    
    @Override
    public void transformState(final Collection<IEspDevice> deviceList, final Direction direction)
    {
        EspDeviceCache deviceCache = EspDeviceCache.getInstance();
        for (IEspDevice device : deviceList)
        {
            transformState(device, direction, NotifyType.STATE_MACHINE_BACKSTATE);
        }
        
        deviceCache.notifyIUser(NotifyType.STATE_MACHINE_UI);
    }
    
    @Override
    public void transformState(final IEspDevice device, final Direction direction)
    {
        transformState(device, direction, NotifyType.STATE_MACHINE_UI);
    }
    
    private void transformState(final IEspDevice device, final Direction direction, NotifyType notifyType)
    {
        __checkValid(device, direction);
        log.debug(Thread.currentThread().toString() + "##transformState(): pass __checkValid");
        EspDeviceCache deviceCache = EspDeviceCache.getInstance();
        
        IEspDevice stateMachineDevice = null;
        if (direction == Direction.CONFIGURE)
        {
            // for Direction.CONFIGURE isn't in IUser's device list,
            // and the configure UI use it to check the device configuring status, so don't copy it
            stateMachineDevice = device;
        }
        else
        {
            // clone a device in statemachine
            // the life cycle of it is here and IEspDeviceCache
            stateMachineDevice = device.cloneDevice();
        }
        
        // the state of device in statemachine
        IEspDeviceState state = stateMachineDevice.getDeviceState();
        
        switch (direction)
        {
            case ACTIVATE:
                state.clearStateConfiguring();
                state.addStateActivating();
                if (!EspDeviceState.checkValidWithSpecificStates(state, EspDeviceState.ACTIVATING))
                {
                    throw new IllegalStateException("device: " + device + ",  case ACTIVATE");
                }
                __activate(device, stateMachineDevice);
                break;
            case CONFIGURE:
                state.clearStateNew();
                // if the device is configuring fail and try configure again, clear the DELETED state
                state.clearStateDeleted();
                state.addStateConfiguring();
                if (!EspDeviceState.checkValidWithSpecificStates(state, EspDeviceState.CONFIGURING))
                {
                    throw new IllegalStateException("device: " + device + ",  case CONFIGURE");
                }
                __configure(device, stateMachineDevice);
                break;
            case DELETE:
                state.clearState();
                state.addStateDeleted();
                // it is obvious the state is EspDeviceState.DELETED now
                __delete(stateMachineDevice);
                break;
            case FAIL:
                if (state.isStateConfiguring())
                {
                    // when configure using esptouch, clearState() instead of addStateDeleted()
                    if ((device instanceof IEspDeviceConfigure))
                    {
                        state.clearState();
                    }
                    else
                    {
                        state.addStateDeleted();
                    }
                }
                if (state.isStateActivating())
                {
                    state.addStateDeleted();
                }
                if (state.isStateUpgradingInternet())
                {
                    state.clearStateUpgradingInternet();
                    state.clearStateInternet();
                    if (!state.isStateLocal())
                    {
                        state.addStateOffline();
                    }
                }
                if (state.isStateUpgradingLocal())
                {
                    state.clearStateUpgradingLocal();
                    state.clearStateLocal();
                    if (!state.isStateInternet())
                    {
                        state.addStateOffline();
                    }
                }
                break;
            case RENAME:
                state.addStateRenamed();
                if (!EspDeviceState.checkValidWithPermittedStates(state,
                    EspDeviceState.RENAMED,
                    EspDeviceState.INTERNET,
                    EspDeviceState.LOCAL,
                    EspDeviceState.OFFLINE))
                {
                    throw new IllegalStateException("device: " + device + ",  case RENAME");
                }
                __rename(stateMachineDevice);
                break;
            case SUC:
                // activate suc
                if (EspDeviceState.checkValidWithSpecificStates(state, EspDeviceState.OFFLINE)
                    || EspDeviceState.checkValidWithSpecificStates(state, EspDeviceState.INTERNET))
                {
                    break;
                }
                // for upgradingInternet suc, it will return INTERNET state
                // it won't be executed now, for it will break in activate suc
                else if (EspDeviceState.checkValidWithNecessaryStates(state, EspDeviceState.INTERNET)
                    && EspDeviceState.checkValidWithPermittedStates(state,
                        EspDeviceState.INTERNET,
                        EspDeviceState.RENAMED))
                {
                    break;
                }
                // upgrading local suc
                else if (EspDeviceState.checkValidWithNecessaryStates(state,
                    EspDeviceState.UPGRADING_LOCAL,
                    EspDeviceState.LOCAL)
                    && EspDeviceState.checkValidWithPermittedStates(state,
                        EspDeviceState.UPGRADING_LOCAL,
                        EspDeviceState.LOCAL,
                        EspDeviceState.INTERNET,
                        EspDeviceState.RENAMED))
                {
                    state.clearStateUpgradingLocal();
                    break;
                }
                // rename suc
                else if (EspDeviceState.checkValidWithNecessaryStates(state, EspDeviceState.RENAMED)
                    && EspDeviceState.checkValidWithPermittedStates(state,
                        EspDeviceState.RENAMED,
                        EspDeviceState.LOCAL,
                        EspDeviceState.INTERNET,
                        EspDeviceState.OFFLINE,
                        EspDeviceState.UPGRADING_INTERNET,
                        EspDeviceState.UPGRADING_LOCAL))
                {
                    state.clearStateRenamed();
                    device.__setDeviceRefreshed();
                    log.info(Thread.currentThread().toString() + "##transformState device:[" + device
                        + "] __setDeviceRefreshed");
                    break;
                }
                // delete suc
                else if (EspDeviceState.checkValidWithSpecificStates(state, EspDeviceState.DELETED))
                {
                    state.clearStateDeleted();
                    break;
                }
                else
                {
                    throw new IllegalStateException("device: " + device + ",  case SUC");
                }
            case UPGRADE_INTERNET:
                state.addStateUpgradingInternet();
                if (!(EspDeviceState.checkValidWithPermittedStates(state,
                    EspDeviceState.UPGRADING_INTERNET,
                    EspDeviceState.INTERNET,
                    EspDeviceState.LOCAL) && EspDeviceState.checkValidWithNecessaryStates(state,
                    EspDeviceState.UPGRADING_INTERNET,
                    EspDeviceState.INTERNET)))
                {
                    throw new IllegalStateException("device: " + device + ",  case UPGRADE_INTERNET");
                }
                __upgradeInternet(stateMachineDevice);
                break;
            case UPGRADE_LOCAL:
                state.addStateUpgradingLocal();
                if (!(EspDeviceState.checkValidWithPermittedStates(state,
                    EspDeviceState.UPGRADING_LOCAL,
                    EspDeviceState.INTERNET,
                    EspDeviceState.LOCAL) && EspDeviceState.checkValidWithNecessaryStates(state,
                    EspDeviceState.UPGRADING_LOCAL,
                    EspDeviceState.LOCAL)))
                {
                    throw new IllegalStateException("device: " + device + ",  case UPGRADE_LOCAL");
                }
                __upgradeLocal(stateMachineDevice);
                break;
        }
        log.debug(Thread.currentThread().toString() + "##transformState(device=[" + device + "],stateMachineDevice=["
            + stateMachineDevice + "],direction=[" + direction + "])");
        // update device in local db
        // stateMachineDevice.saveInDB();
        // put the device into deviceCache
        log.error("stateMachineDevice name:" + stateMachineDevice.getName());
        deviceCache.addStatemahchineDeviceCache(stateMachineDevice);
        
        // notify the IUser
        deviceCache.notifyIUser(notifyType);
    }
    
    private void __checkValid(IEspDevice device, Direction direction)
    {
        IEspDeviceState currentState = device.getDeviceState();
        // graph 1
        // start configure
        if (EspDeviceState.checkValidWithSpecificStates(currentState, EspDeviceState.NEW))
        {
            if (direction == Direction.CONFIGURE)
            {
                return;
            }
        }
        // CONFIGURING and DELETED: means the device is configured fail last time, and device is configured again
        if (EspDeviceState.checkValidWithSpecificStates(currentState,
            EspDeviceState.CONFIGURING,
            EspDeviceState.DELETED))
        {
            if (direction == Direction.CONFIGURE)
            {
                return;
            }
        }
        
        // configure fail or configure suc(start activate)
        if (EspDeviceState.checkValidWithSpecificStates(currentState, EspDeviceState.CONFIGURING))
        {
            if (direction == Direction.ACTIVATE || direction == Direction.FAIL)
            {
                return;
            }
        }
        
        // activate fail
        if (EspDeviceState.checkValidWithSpecificStates(currentState, EspDeviceState.ACTIVATING))
        {
            if (direction == Direction.FAIL)
            {
                return;
            }
        }
        // activate suc
        if (EspDeviceState.checkValidWithNecessaryStates(currentState, EspDeviceState.OFFLINE)
            || EspDeviceState.checkValidWithPermittedStates(currentState,
                EspDeviceState.OFFLINE,
                EspDeviceState.INTERNET))
        {
            if (direction == Direction.SUC)
            {
                return;
            }
        }
        // start upgrade local
        if (EspDeviceState.checkValidWithNecessaryStates(currentState, EspDeviceState.LOCAL)
            && EspDeviceState.checkValidWithPermittedStates(currentState,
                EspDeviceState.LOCAL,
                EspDeviceState.INTERNET,
                EspDeviceState.RENAMED))
        {
            if (direction == Direction.UPGRADE_LOCAL)
            {
                return;
            }
        }
        // start upgrade internet
        if (EspDeviceState.checkValidWithNecessaryStates(currentState, EspDeviceState.INTERNET)
            && EspDeviceState.checkValidWithPermittedStates(currentState,
                EspDeviceState.INTERNET,
                EspDeviceState.LOCAL,
                EspDeviceState.RENAMED))
        {
            if (direction == Direction.UPGRADE_INTERNET)
            {
                return;
            }
        }
        // upgrade local suc or fail
        if (EspDeviceState.checkValidWithNecessaryStates(currentState, EspDeviceState.UPGRADING_LOCAL)
            && EspDeviceState.checkValidWithPermittedStates(currentState,
                EspDeviceState.UPGRADING_LOCAL,
                EspDeviceState.LOCAL,
                EspDeviceState.INTERNET,
                EspDeviceState.RENAMED))
        {
            if (direction == Direction.SUC || direction == Direction.FAIL)
            {
                return;
            }
        }
        // upgrade internet suc or fail
        if (EspDeviceState.checkValidWithNecessaryStates(currentState, EspDeviceState.UPGRADING_INTERNET)
            && EspDeviceState.checkValidWithPermittedStates(currentState,
                EspDeviceState.UPGRADING_INTERNET,
                EspDeviceState.LOCAL,
                EspDeviceState.INTERNET,
                EspDeviceState.RENAMED))
        {
            if (direction == Direction.SUC || direction == Direction.FAIL)
            {
                return;
            }
        }
        // graph 2
        // start rename or delete
        if (EspDeviceState.checkValidWithNecessaryStates(currentState, EspDeviceState.OFFLINE)
            || EspDeviceState.checkValidWithNecessaryStates(currentState, EspDeviceState.LOCAL)
            || EspDeviceState.checkValidWithNecessaryStates(currentState, EspDeviceState.INTERNET)
            && EspDeviceState.checkValidWithPermittedStates(currentState,
                EspDeviceState.OFFLINE,
                EspDeviceState.LOCAL,
                EspDeviceState.INTERNET,
                EspDeviceState.RENAMED))
        {
            if (direction == Direction.DELETE || direction == Direction.RENAME)
            {
                return;
            }
        }
        // rename suc or rename fail or delete suc or delete fail
        if (EspDeviceState.checkValidWithNecessaryStates(currentState, EspDeviceState.RENAMED)
            || EspDeviceState.checkValidWithNecessaryStates(currentState, EspDeviceState.DELETED)
            && EspDeviceState.checkValidWithPermittedStates(currentState,
                EspDeviceState.RENAMED,
                EspDeviceState.DELETED,
                EspDeviceState.CONFIGURING,
                EspDeviceState.ACTIVATING,
                EspDeviceState.OFFLINE,
                EspDeviceState.LOCAL,
                EspDeviceState.INTERNET))
        {
            if (direction == Direction.SUC)
            {
                return;
            }
        }
        // when login, resume delete action(the delete action is fail last time,but device's state is DELETED)
        if (EspDeviceState.checkValidWithSpecificStates(currentState, EspDeviceState.DELETED))
        {
            if (direction == Direction.DELETE)
            {
                return;
            }
        }
        log.error(Thread.currentThread().toString() + "##__checkValid(device=[" + device + "],direction=[" + direction
            + "]) IllegalStateException");
        throw new IllegalStateException("device: " + device + ",direction:" + direction);
    }
}
