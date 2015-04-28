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
    
    private void __doActionDeviceSynchronizeInterentDiscoverLocal(final String userKey)
    {
        // task Internet
        Callable<List<IEspDevice>> taskInternet = new Callable<List<IEspDevice>>()
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
        Future<List<IEspDevice>> futureInternet = EspBaseApiUtil.submit(taskInternet);
        
        // task Local
        List<IOTAddress> iotAddressList = new ArrayList<IOTAddress>();
        Callable<List<IOTAddress>> taskLocal = new Callable<List<IOTAddress>>()
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
        for (int executeTime = 0; executeTime < UDP_EXECUTE_MAX_TIMES; executeTime++)
        {
            if (executeTime >= UDP_EXECUTE_MIN_TIMES && futureInternet.isDone())
            {
                break;
            }
            Future<List<IOTAddress>> futureLocal = EspBaseApiUtil.submit(taskLocal);
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
        
        IEspDeviceCache deviceCache = EspDeviceCache.getInstance();
        try
        {
            List<IEspDevice> internetResult = futureInternet.get();
            // Internet unaccessible
            if (internetResult == null)
            {
                log.error("Internet unaccessible");
                deviceCache.addLocalDeviceCacheList(iotAddressList);
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
                                device.setRouter(iotAddress.getRouter());
                                deviceState.addStateLocal();
                                break;
                            }
                        }
                    }
                    deviceCache.addServerLocalDeviceCacheList(internetResult);
                }
                else
                {
                    log.error("add EmptyDevice");
                    // add EmptyDevice to distinguish from Internet unaccessible
                    deviceCache.addServerLocalDeviceCache(EspDevice.EmptyDevice);
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
                __doActionDeviceSynchronizeInterentDiscoverLocal(userKey);
            }
            
        });
    }
    
}
