package com.espressif.iot.base.net.rest.mesh;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.espressif.iot.base.api.EspBaseApiUtil;
import com.espressif.iot.base.net.udp.UdpBroadcastUtil;
import com.espressif.iot.type.device.EspDeviceType;
import com.espressif.iot.type.net.IOTAddress;
import com.espressif.iot.util.MeshUtil;

public class EspMeshDiscoverUtil
{
	private static final Logger log = Logger.getLogger(EspMeshDiscoverUtil.class);
	
    private static final int UDP_RETRY_TIME = 3;
    private static final String ROUTER = "router";
    private static final String TOPOLOGY = "topology";
    
    private static Set<IOTAddress> __discoverIOTMeshDevicesOnRoot(IOTAddress rootIOTAddress)
    {
        String url = "http:/" + rootIOTAddress.getInetAddress();
        String routerReq = "00000000";
        String deviceBssid = "00:00:00:00:00:00";
        JSONObject jsonReq = new JSONObject();
        try
        {
            jsonReq.put(TOPOLOGY, MeshUtil.getMacAddressForMesh(deviceBssid));
        }
        catch (JSONException e1)
        {
            e1.printStackTrace();
        }
        JSONArray jsonArray = EspMeshNetUtil.PostForJsonArray(url, routerReq, deviceBssid, jsonReq);
        Set<IOTAddress> iotMeshAddressSet = new HashSet<IOTAddress>();
        for (int i = 0; jsonArray!=null && i < jsonArray.length(); i++)
        {
            try
            {
                JSONObject json = (JSONObject)jsonArray.get(i);
                String bssid = MeshUtil.getRawMacAddress(json.getString(TOPOLOGY));
                String router = json.getString(ROUTER);
                if(router.equals("00000000"))
                {
                	log.warn("router equals 00000000, it shouldn't happen");
                	continue;
                }
                // the root node and its children nodes has the same InetAddress.
                // the router is used to distinguish them
                InetAddress inetAddress = rootIOTAddress.getInetAddress();
                IOTAddress iotMeshAddress = new IOTAddress(bssid, inetAddress, router, true);
                //TODO parse real device type by mesh
                iotMeshAddress.setEspDeviceTypeEnum(EspDeviceType.LIGHT);
				log.debug("__discoverIOTMeshDevicesOnRoot(): iotMeshAddress=["
						+ iotMeshAddress + "] is added into iotMeshAddressSet");
				iotMeshAddressSet.add(iotMeshAddress);
            }
            catch (JSONException e)
            {
                e.printStackTrace();
            }
        }
        return iotMeshAddressSet;
    }
    
    /**
     * @see IOTAddress discover IOT devices in the same AP by UDP broadcast
     * 
     * @return the List of IOTAddress
     */
    private static Set<IOTAddress> discoverIOTMeshDevices()
    {
        // discover IOT Root Devices by UDP Broadcast
		final Set<IOTAddress> rootDeviceSet = new HashSet<IOTAddress>();
		// send udp broadcast each 1000 ms no matter how much is udp broadcast SOTIMEOUT
		List<Future<?>> _futureList = new ArrayList<Future<?>>();
		for (int i = 0; i < UDP_RETRY_TIME; i++) {
			Future<?> _future = EspBaseApiUtil.submit(new Runnable() {
				@Override
				public void run() {
					List<IOTAddress> rootDeviceList = UdpBroadcastUtil
							.discoverIOTDevices();
					rootDeviceSet.addAll(rootDeviceList);
				}
			});
			_futureList.add(_future);
			if (i < UDP_RETRY_TIME - 1) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		for (Future<?> future : _futureList) {
			try {
				future.get();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
		}
        log.debug("discoverIOTMeshDevices(): rootDeviceSet=" + rootDeviceSet);
        // discover IOT Devices by IOT Root Devices
        List<Future<Set<IOTAddress>>> futureList = new ArrayList<Future<Set<IOTAddress>>>();
        for (final IOTAddress iotAddress : rootDeviceSet)
        {
            // if the device isn't mesh device ignore it, 
            if(!iotAddress.isMeshDevice())
            {
                continue;
            }
            Future<Set<IOTAddress>> future = EspBaseApiUtil.submit(new Callable<Set<IOTAddress>>()
            {
                
                @Override
                public Set<IOTAddress> call()
                    throws Exception
                {
                	log.debug("__discoverIOTMeshDevicesOnRoot(): iotAddress=[" + iotAddress +"]" );
                    return __discoverIOTMeshDevicesOnRoot(iotAddress);
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
        // add all device which isn't belong to mesh device into allDeviceSet directly
        for (final IOTAddress iotAddress : rootDeviceSet)
        {
            if(!iotAddress.isMeshDevice())
            {
                allDeviceSet.add(iotAddress);
            }
        }
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
        Set<IOTAddress> iotAddressSet = discoverIOTMeshDevices();
        List<IOTAddress> iotAddressList = new ArrayList<IOTAddress>();
        iotAddressList.addAll(iotAddressSet);
        return iotAddressList;
    }
}
