package com.espressif.iot.base.net.rest.mesh;

import java.net.InetAddress;
import java.net.UnknownHostException;
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

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import com.espressif.iot.base.api.EspBaseApiUtil;
import com.espressif.iot.base.application.EspApplication;
import com.espressif.iot.base.net.mdns.MdnsDiscoverUtil;
import com.espressif.iot.base.net.udp.UdpBroadcastUtil;
import com.espressif.iot.type.device.EspDeviceType;
import com.espressif.iot.type.net.IOTAddress;
import com.espressif.iot.util.BSSIDUtil;
import com.espressif.iot.util.MeshUtil;

public class EspMeshDiscoverUtil
{
	private static final Logger log = Logger.getLogger(EspMeshDiscoverUtil.class);
	private static final boolean IS_MDNS_ON = false;
	
    private static final int UDP_RETRY_TIME = 3;
    private static final String ROUTER = "router";
    private static final String TOPOLOGY = "topology";
    private static final String DEV_TYPE = "dev_type";
    
    private static Set<IOTAddress> __discoverIOTMeshDevicesOnRoot(IOTAddress rootIOTAddress, String deviceBssid)
    {
        log.error("__discoverIOTMeshDevicesOnRoot(): deviceBssid:" + deviceBssid);
        String url = "http:/" + rootIOTAddress.getInetAddress();
        String routerReq = "00000000";
        if (deviceBssid == null)
        {
            deviceBssid = "00:00:00:00:00:00";
        }
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
            EspDeviceType deviceType = null;
            String deviceTypeStr = null;
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
                if (json.has(DEV_TYPE))
                {
                    deviceTypeStr = json.getString(DEV_TYPE);
                    deviceType = EspDeviceType.getEspTypeEnumByString(deviceTypeStr);
                }
                // the root node and its children nodes has the same InetAddress.
                // the router is used to distinguish them
                InetAddress inetAddress = rootIOTAddress.getInetAddress();
                IOTAddress iotMeshAddress = new IOTAddress(bssid, inetAddress, router, true);
                // parse real device type by mesh
                if (deviceType == null)
                {
                    // the default deviceType is LIGHT
                    iotMeshAddress.setEspDeviceTypeEnum(EspDeviceType.LIGHT);
                }
                else
                {
                    iotMeshAddress.setEspDeviceTypeEnum(deviceType);
                }
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
    
    // extract IOTAddress with the specific bssid
    private static Set<IOTAddress> extractIOTAddress(String bssid, IOTAddress iotAddress)
    {
        if (bssid == null)
        {
            return null;
        }
        if (iotAddress.getBSSID().equals(bssid))
        {
            Set<IOTAddress> result = new HashSet<IOTAddress>();
            result.add(iotAddress);
            return result;
        }
        return null;
    }
    
    // extract IOTAddress with the specific bssid
    private static Set<IOTAddress> extractIOTAddress(String bssid, Set<IOTAddress> iotAddressSet)
    {
        if (bssid == null)
        {
            return null;
        }
        for (IOTAddress iotAddress : iotAddressSet)
        {
            if (iotAddress.getBSSID().equals(bssid))
            {
                Set<IOTAddress> result = new HashSet<IOTAddress>();
                result.add(iotAddress);
                return result;
            }
        }
        return null;
    }
    
    /**
     * @see IOTAddress discover IOT devices in the same AP by UDP broadcast
     * 
     * @param deviceBssid the device's bssid which is to be found, null means find all devices
     * @return
     */
    private static Set<IOTAddress> discoverIOTMeshDevices(final String deviceBssid)
    {
        // store the targetDevice set with only one element whose bssid is deviceBssid
        Set<IOTAddress> targetDeviceSet = null;
        // discover IOT Root Devices by UDP Broadcast
		final Set<IOTAddress> rootDeviceSet = new HashSet<IOTAddress>();
		// if phone is connected to device, skip sending udp broadcast
		String gateway = EspApplication.sharedInstance().getGateway();
		if(MeshUtil.isGatewayMesh(gateway))
		{
		    Context context = EspApplication.sharedInstance().getBaseContext();
	        WifiManager mWifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
	        WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
	        String bssid = BSSIDUtil.restoreStaBSSID(wifiInfo.getBSSID());
	        InetAddress inetAddr = null;
            try
            {
                inetAddr = InetAddress.getByName(gateway);
            }
            catch (UnknownHostException e)
            {
                e.printStackTrace();
            }
		    IOTAddress meshDevice = new IOTAddress(bssid, inetAddr, true);
		    // the default deviceType is LIGHT
		    meshDevice.setEspDeviceTypeEnum(EspDeviceType.LIGHT);
		    // check whether the device is find already
            targetDeviceSet = extractIOTAddress(deviceBssid, meshDevice);
            if (targetDeviceSet != null)
            {
                log.debug("discoverIOTMeshDevices(): targetDeviceSet=" + targetDeviceSet);
                return targetDeviceSet;
            }
            rootDeviceSet.add(meshDevice);
		}
		else
		{
		    // send udp broadcast each 1000 ms no matter how much is udp broadcast SOTIMEOUT
	        List<Future<?>> _futureList = new ArrayList<Future<?>>();
	        for (int i = 0; i < UDP_RETRY_TIME; i++) {
	            Future<?> _future = EspBaseApiUtil.submit(new Runnable() {
	                @Override
	                public void run() {
	                    List<IOTAddress> rootDeviceList = null;
	                    if (IS_MDNS_ON)
	                    {
	                        rootDeviceList = MdnsDiscoverUtil.discoverIOTDevices();
	                    }
	                    else
	                    {
	                        rootDeviceList = UdpBroadcastUtil.discoverIOTDevices();
	                    }
	                    
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
		}
        log.debug("discoverIOTMeshDevices(): rootDeviceSet=" + rootDeviceSet);
        // check whether the device is find already
        targetDeviceSet = extractIOTAddress(deviceBssid, rootDeviceSet);
        if (targetDeviceSet != null)
        {
            log.debug("discoverIOTMeshDevices(): targetDeviceSet=" + targetDeviceSet);
            return targetDeviceSet;
        }
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
                    return __discoverIOTMeshDevicesOnRoot(iotAddress, deviceBssid);
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
                log.warn("discoverIOTMeshDevices():: more than one device in allDeviceSet," +
                		" but we just trust the first one");
            }
            log.debug("discoverIOTMeshDevices(): allDeviceSet(targetDeviceSet)=" + allDeviceSet);
            return allDeviceSet;
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
