package com.espressif.iot.action.device.common;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;

import com.espressif.iot.base.api.EspBaseApiUtil;
import com.espressif.iot.command.device.common.EspCommandDeviceDiscoverLocal;
import com.espressif.iot.command.device.common.EspCommandDeviceSynchronizeInternet;
import com.espressif.iot.command.device.common.IEspCommandDeviceDiscoverLocal;
import com.espressif.iot.command.device.common.IEspCommandDeviceSynchronizeInternet;
import com.espressif.iot.device.IEspDevice;
import com.espressif.iot.device.cache.IEspDeviceCache;
import com.espressif.iot.device.cache.IEspDeviceCache.NotifyType;
import com.espressif.iot.model.device.EspDevice;
import com.espressif.iot.model.device.cache.EspDeviceCache;
import com.espressif.iot.type.device.IEspDeviceState;
import com.espressif.iot.type.net.IOTAddress;

public class EspActionDeviceSynchronizeInterentDiscoverLocal implements
    IEspActionDeviceSynchronizeInterentDiscoverLocal
{
    private final static Logger log = Logger.getLogger(EspActionDeviceSynchronizeInterentDiscoverLocal.class);
    
    private List<IOTAddress> doCommandDeviceDiscoverLocal()
    {
        IEspCommandDeviceDiscoverLocal command = new EspCommandDeviceDiscoverLocal();
        return command.doCommandDeviceDiscoverLocal();
    }
    
    private List<IEspDevice> doCommandDeviceSynchronizeInternet(String userKey)
    {
        IEspCommandDeviceSynchronizeInternet action = new EspCommandDeviceSynchronizeInternet();
        return action.doCommandDeviceSynchronizeInternet(userKey);
    }
    
    private void __doActionDeviceSynchronizeInterentDiscoverLocal(final String userKey, boolean serverRequired,
        boolean localRequired)
    {
        // internet variables
        Callable<List<IEspDevice>> taskInternet = null;
        Future<List<IEspDevice>> futureInternet = null;
        // local variables
        List<IOTAddress> iotAddressList = new ArrayList<IOTAddress>();
        Callable<List<IOTAddress>> taskLocal = null;
        Future<List<IOTAddress>> futureLocal = null;
        
        // task Internet
        if (serverRequired)
        {
            taskInternet = new Callable<List<IEspDevice>>()
            {
                
                @Override
                public List<IEspDevice> call()
                    throws Exception
                {
                    log.debug(Thread.currentThread().toString()
                        + "##__doActionDeviceSynchronizeInterentDiscoverLocal(userKey=[" + userKey
                        + "]): doCommandDeviceSynchronizeInternet()");
                    return doCommandDeviceSynchronizeInternet(userKey);
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
            
            List<IEspDevice> internetResult = null;
            if (serverRequired)
            {
                internetResult = futureInternet.get();
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
                    __doActionDeviceSynchronizeInterentDiscoverLocal(null, false, true);
                }
                
            });
        }
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
