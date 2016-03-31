package com.espressif.iot.base.net.rest2;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;

import com.espressif.iot.base.api.EspBaseApiUtil;
import com.espressif.iot.base.net.mdns.MdnsDiscoverUtil;
import com.espressif.iot.base.net.udp.UdpBroadcastUtil;
import com.espressif.iot.type.net.IOTAddress;
import com.espressif.iot.user.IEspUser;
import com.espressif.iot.user.builder.BEspUser;

public class EspMeshDiscoverUtil
{
    private static final Logger log = Logger.getLogger(EspMeshDiscoverUtil.class);
    
    private static final boolean IS_MDNS_ON = false;
    
    private static final int UDP_RETRY_TIME = 3;
    
    private static Set<IOTAddress> __discoverIOTMeshDevicesOnRoot2(IOTAddress rootIOTAddress, String deviceBssid)
    {
        Set<IOTAddress> iotMeshAddressSet = new HashSet<IOTAddress>();
        InetAddress rootInetAddress = rootIOTAddress.getInetAddress();
        String rootBssid = rootIOTAddress.getBSSID();
        if (deviceBssid != null)
        {
            IOTAddress iotAddress = EspMeshNetUtil2.GetTopoIOTAddress5(rootInetAddress, deviceBssid);
            if (iotAddress != null)
            {
                iotMeshAddressSet.add(iotAddress);
            }
            return iotMeshAddressSet;
        }
        else
        {
            List<IOTAddress> iotAddressList = EspMeshNetUtil2.GetTopoIOTAddressList5(rootInetAddress, rootBssid);
            if (iotAddressList != null)
            {
                iotMeshAddressSet.addAll(iotAddressList);
            }
            return iotMeshAddressSet;
        }
    }
    
    /**
     * @see IOTAddress discover IOT devices in the same AP by UDP broadcast
     * 
     * @param deviceBssid the device's bssid which is to be found, null means find all devices
     * @return
     */
    private static Set<IOTAddress> discoverIOTMeshDevices(final String deviceBssid)
    {
        final IEspUser user = BEspUser.getBuilder().getInstance();
        // discover IOT Root Devices by UDP Broadcast
        final Set<IOTAddress> rootDeviceSet = new HashSet<IOTAddress>();
        // send udp broadcast each 1000 ms no matter how much is udp broadcast SOTIMEOUT
        List<Future<?>> _futureList = new ArrayList<Future<?>>();
        for (int i = 0; i < UDP_RETRY_TIME; i++)
        {
            Future<?> _future = EspBaseApiUtil.submit(new Runnable()
            {
                @Override
                public void run()
                {
                    List<IOTAddress> rootDeviceList = null;
                    if (IS_MDNS_ON)
                    {
                        rootDeviceList = MdnsDiscoverUtil.discoverIOTDevices();
                    }
                    else
                    {
                        rootDeviceList = UdpBroadcastUtil.discoverIOTDevices();
                    }

                    // only discover all device require __addTempStaDeviceList()
                    if (deviceBssid == null)
                    {
                        for (IOTAddress iotAddress : rootDeviceList)
                        {
                            iotAddress.setRootBssid(iotAddress.getBSSID());
                        }
                        user.__addTempStaDeviceList(rootDeviceList);
                    }
                    rootDeviceSet.addAll(rootDeviceList);
                }
            });
            _futureList.add(_future);
            if (i < UDP_RETRY_TIME - 1)
            {
                try
                {
                    Thread.sleep(1000);
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
            }
        }
        for (Future<?> future : _futureList)
        {
            try
            {
                future.get();
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
        log.debug("discoverIOTMeshDevices(): rootDeviceSet=" + rootDeviceSet);
        // discover IOT Devices by IOT Root Devices
        List<Future<Set<IOTAddress>>> futureList = new ArrayList<Future<Set<IOTAddress>>>();
        for (final IOTAddress rootIOTAddress : rootDeviceSet)
        {
            // if the device isn't mesh device ignore it,
            if (!rootIOTAddress.isMeshDevice())
            {
                continue;
            }
            Future<Set<IOTAddress>> future = EspBaseApiUtil.submit(new Callable<Set<IOTAddress>>()
            {
                
                @Override
                public Set<IOTAddress> call()
                    throws Exception
                {
                    log.debug("__discoverIOTMeshDevicesOnRoot2(): rootIOTAddress=[" + rootIOTAddress + "]");
                    return __discoverIOTMeshDevicesOnRoot2(rootIOTAddress, deviceBssid);
                }
                
            });
            futureList.add(future);
        }
        // add all device together into allDeviceSet
        Set<IOTAddress> allDeviceSet = new HashSet<IOTAddress>();
        for (Future<Set<IOTAddress>> future : futureList)
        {
            try
            {
                Set<IOTAddress> deviceSet = future.get();
                allDeviceSet.addAll(deviceSet);
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
        // only zero or one device should in the allDeviceSet
        if (deviceBssid != null)
        {
            if (allDeviceSet.size() > 1)
            {
                log.warn("discoverIOTMeshDevices():: more than one device in allDeviceSet,"
                    + " but we just trust the first one");
            }
            if (allDeviceSet.size() == 0)
            {
                for (IOTAddress rootDevice : rootDeviceSet)
                {
                    if (rootDevice.getBSSID().equals(deviceBssid))
                    {
                        allDeviceSet.add(rootDevice);
                        break;
                    }
                }
            }
            log.debug("discoverIOTMeshDevices(): allDeviceSet(targetDeviceSet)=" + allDeviceSet);
            return allDeviceSet;
        }
        // add all root device set
        allDeviceSet.addAll(rootDeviceSet);
        log.debug("discoverIOTMeshDevices(): allDeviceSet=" + allDeviceSet);
        return allDeviceSet;
    }
    
    /**
     * @see IOTAddress discover IOT devices in the same AP by UDP broadcast
     * 
     * @return the List of IOTAddress
     */
    public static List<IOTAddress> discoverIOTDevices()
    {
        Set<IOTAddress> iotAddressSet = discoverIOTMeshDevices(null);
        List<IOTAddress> iotAddressList = new ArrayList<IOTAddress>();
        iotAddressList.addAll(iotAddressSet);
        return iotAddressList;
    }
    
    /**
     * @see IOTAddress discover IOT device in the same AP by UDP broadcast or in the mesh net
     * @param deviceBssid the device's bssid
     * @return the IOTAddress
     */
    public static IOTAddress discoverIOTDevice(String deviceBssid)
    {
        Set<IOTAddress> iotAddressSet = discoverIOTMeshDevices(deviceBssid);
        if (iotAddressSet.size() > 0)
        {
            return iotAddressSet.iterator().next();
        }
        else
        {
            return null;
        }
    }
}
