package com.espressif.iot.action.device.common;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;

import android.util.Log;

import com.espressif.iot.base.api.EspBaseApiUtil;
import com.espressif.iot.base.application.EspApplication;
import com.espressif.iot.base.net.udp.LightUdpClient;
import com.espressif.iot.command.device.common.EspCommandDeviceDiscoverLocal;
import com.espressif.iot.command.device.common.EspCommandDeviceSynchronizeInternet;
import com.espressif.iot.command.device.common.IEspCommandDeviceDiscoverLocal;
import com.espressif.iot.command.device.common.IEspCommandDeviceSynchronizeInternet;
import com.espressif.iot.device.IEspDevice;
import com.espressif.iot.device.cache.IEspDeviceCache;
import com.espressif.iot.device.cache.IEspDeviceCache.NotifyType;
import com.espressif.iot.group.IEspGroup;
import com.espressif.iot.model.device.EspDevice;
import com.espressif.iot.model.device.cache.EspDeviceCache;
import com.espressif.iot.model.group.EspGroupHandler;
import com.espressif.iot.type.device.IEspDeviceState;
import com.espressif.iot.type.net.IOTAddress;
import com.espressif.iot.user.IEspUser;
import com.espressif.iot.user.builder.BEspUser;

public class EspActionDeviceSynchronizeInterentDiscoverLocal implements
    IEspActionDeviceSynchronizeInterentDiscoverLocal
{
    private final static Logger log = Logger.getLogger(EspActionDeviceSynchronizeInterentDiscoverLocal.class);
    
    private List<IOTAddress> doCommandDeviceDiscoverLocal()
    {
        IEspCommandDeviceDiscoverLocal command = new EspCommandDeviceDiscoverLocal();
        return command.doCommandDeviceDiscoverLocal();
    }
    
    private List<IEspGroup> doCommandGroupSynchronizeInternet(String userKey)
    {
        IEspCommandDeviceSynchronizeInternet action = new EspCommandDeviceSynchronizeInternet();
        return action.doCommandGroupSynchronizeInternet(userKey);
    }
    
    private List<IEspDevice> getDevicesFromGroupList(List<IEspGroup> groupList)
    {
        List<IEspDevice> deviceList = new ArrayList<IEspDevice>();
        for (IEspGroup group : groupList)
        {
            List<IEspDevice> deviceListInGroup = group.getDeviceList();
            for (IEspDevice deviceInList : deviceListInGroup)
            {
                if (!deviceList.contains(deviceInList))
                {
                    deviceList.add(deviceInList);
                }
            }
        }
        return deviceList;
    }
    
    private void __doActionDeviceSynchronizeInterentDiscoverLocal(final String userKey, boolean serverRequired,
        boolean localRequired)
    {
        // internet variables
        Callable<List<IEspGroup>> taskInternet = null;
        Future<List<IEspGroup>> futureInternet = null;
        // local variables
        List<IOTAddress> iotAddressList = new ArrayList<IOTAddress>();
        Callable<List<IOTAddress>> taskLocal = null;
        Future<List<IOTAddress>> futureLocal = null;
        
        // task Internet
        if (serverRequired)
        {
            taskInternet = new Callable<List<IEspGroup>>()
            {
                
                @Override
                public List<IEspGroup> call()
                    throws Exception
                {
                    log.debug(Thread.currentThread().toString()
                        + "##__doActionDeviceSynchronizeInterentDiscoverLocal(userKey=[" + userKey
                        + "]): doCommandDeviceSynchronizeInternet()");
                    return doCommandGroupSynchronizeInternet(userKey);
                }
                
            };
            futureInternet = EspBaseApiUtil.submit(taskInternet);
        }
        
        // task Local
        if (localRequired)
        {
            taskLocal = new Callable<List<IOTAddress>>()
            {
                
                @Override
                public List<IOTAddress> call()
                    throws Exception
                {
                    log.debug(Thread.currentThread().toString()
                        + "##__doActionDeviceSynchronizeInterentDiscoverLocal(userKey=[" + userKey
                        + "]): doCommandDeviceDiscoverLocal()");
                    return doCommandDeviceDiscoverLocal();
                }
                
            };
        }
        
        // clear temp sta device list
        final IEspUser user = BEspUser.getBuilder().getInstance();
        user.__clearTempStaDeviceList();
        
        if (localRequired && serverRequired)
        {
            for (int executeTime = 0; executeTime < UDP_EXECUTE_MAX_TIMES; executeTime++)
            {
                if (executeTime >= UDP_EXECUTE_MIN_TIMES && futureInternet.isDone())
                {
                    break;
                }
                futureLocal = EspBaseApiUtil.submit(taskLocal);
                // get local result
                try
                {
                    List<IOTAddress> localResult = futureLocal.get();
                    for (IOTAddress iotAddress : localResult)
                    {
                        // add iotAddress if iotAddressList doesn't have
                        if (!iotAddressList.contains(iotAddress))
                        {
                            iotAddressList.add(iotAddress);
                        }
                    }
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
                catch (ExecutionException e)
                {
                    e.printStackTrace();
                }
            }
        }
        
        // only localRequired, do discover local only once
        else if (localRequired)
        {
            futureLocal = EspBaseApiUtil.submit(taskLocal);
        }
        
        if (serverRequired)
        {
            // wait futureInternet finished
            while (!futureInternet.isDone())
            {
                try
                {
                    Thread.sleep(500);
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
            }
        }
        else if (localRequired)
        {
            // wait local finished
            while (!futureLocal.isDone())
            {
                try
                {
                    Thread.sleep(500);
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
            }
            try
            {
                // get local result
                List<IOTAddress> localResult = futureLocal.get();
                for (IOTAddress iotAddress : localResult)
                {
                    // add iotAddress if iotAddressList doesn't have
                    if (!iotAddressList.contains(iotAddress))
                    {
                        iotAddressList.add(iotAddress);
                    }
                }
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
            catch (ExecutionException e)
            {
                e.printStackTrace();
            }
        }
        else
        {
            throw new IllegalArgumentException("serverRequired = false, localRequired = false");
        }
        
        IEspDeviceCache deviceCache = EspDeviceCache.getInstance();
        try
        {
            // add sta device list
            if (localRequired)
            {
                // add the discover local result
                if (iotAddressList.isEmpty())
                {
                    // add EmptyIOTAddress to distinguish from localRequired is false
                    deviceCache.addStaDeviceCache(IOTAddress.EmptyIOTAddress);
                }
                else
                {
                    deviceCache.addStaDeviceCacheList(iotAddressList);
                }
            }
            
            List<IEspGroup> internetResult0 = null;
            List<IEspDevice> internetResult = null;
            if (serverRequired)
            {
                internetResult0 = futureInternet.get();
            }
            if (internetResult0 != null)
            {
                // update cloud groups
                EspGroupHandler.getInstance().updateSynchronizeCloudGroups(internetResult0);
                internetResult = getDevicesFromGroupList(internetResult0);
            }
            
            // Internet unaccessible or serverRequired = false
            if (internetResult == null)
            {
                if (serverRequired)
                {
                    log.error("Internet unaccessible");
                    deviceCache.addLocalDeviceCacheList(iotAddressList);
                }
                else
                {
                    log.error("add EmptyDevice 1");
                    // add EmptyDevice to distinguish from Internet unaccessible
                    deviceCache.addServerLocalDeviceCache(EspDevice.EmptyDevice1);
                }
            }
            // Internet accessible
            else
            {
                if (!internetResult.isEmpty())
                {
                    
                    for (IEspDevice device : internetResult)
                    {
                        for (IOTAddress iotAddress : iotAddressList)
                        {
                            String bssid1 = device.getBssid();
                            String bssid2 = iotAddress.getBSSID();
                            if (bssid1.equals(bssid2))
                            {
                                // deviceState could be isStateOffline() or isStateInternet()
                                IEspDeviceState deviceState = device.getDeviceState();
                                if (deviceState.isStateOffline())
                                {
                                    deviceState.clearStateOffline();
                                }
                                device.setInetAddress(iotAddress.getInetAddress());
                                device.setIsMeshDevice(iotAddress.isMeshDevice());
                                device.setParentDeviceBssid(iotAddress.getParentBssid());
                                deviceState.addStateLocal();
                                // update rom version by local if necessary
                                String romVersionLocal = iotAddress.getRomVersion();
                                String romVersionInternet = device.getRom_version();
                                if (romVersionLocal != null && !romVersionLocal.equals(romVersionInternet))
                                {
                                    Log.w("EspActionDeviceSynchronizeInterentDiscoverLocal", "romVersionLocal="
                                        + romVersionLocal + ",romVersionInternet=" + romVersionInternet
                                        + " is different");
                                    device.setRom_version(romVersionLocal);
                                }
                                break;
                            }
                        }
                    }
                    deviceCache.addServerLocalDeviceCacheList(internetResult);
                }
                else
                {
                    log.error("add EmptyDevice 2");
                    // add EmptyDevice to distinguish from Internet unaccessible
                    deviceCache.addServerLocalDeviceCache(EspDevice.EmptyDevice2);
                }
            }
            deviceCache.notifyIUser(NotifyType.PULL_REFRESH);
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
        catch (ExecutionException e)
        {
            e.printStackTrace();
        }
    }
    
    @Override
    public void doActionDeviceSynchronizeInterentDiscoverLocal(final String userKey)
    {
        // do it in background thread
        EspBaseApiUtil.submit(new Runnable()
        {
            
            @Override
            public void run()
            {
                broadcastPhoneAddress();
                __doActionDeviceSynchronizeInterentDiscoverLocal(userKey, true, true);
            }
            
        });
    }
    
    @Override
    public void doActionDeviceSynchronizeDiscoverLocal(boolean isSyn)
    {
        if (isSyn)
        {
            __doActionDeviceSynchronizeInterentDiscoverLocal(null, false, true);
        }
        else
        {
            EspBaseApiUtil.submit(new Runnable()
            {
                
                @Override
                public void run()
                {
                    broadcastPhoneAddress();
                    __doActionDeviceSynchronizeInterentDiscoverLocal(null, false, true);
                }
                
            });
        }
    }
    
    private void broadcastPhoneAddress() {
        LightUdpClient client = new LightUdpClient(EspApplication.sharedInstance());
        for (int i = 0; i < 3; i++) {
            client.notifyAddress();
        }
        client.close();
    }
    
    @Override
    public void doActionDeviceSynchronizeInternet(final String userKey)
    {
        EspBaseApiUtil.submit(new Runnable()
        {
            
            @Override
            public void run()
            {
                __doActionDeviceSynchronizeInterentDiscoverLocal(userKey, true, false);
            }
            
        });
    }
    
}
