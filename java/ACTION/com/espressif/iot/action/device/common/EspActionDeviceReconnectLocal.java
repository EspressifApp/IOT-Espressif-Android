package com.espressif.iot.action.device.common;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.espressif.iot.base.api.EspBaseApiUtil;
import com.espressif.iot.command.device.common.EspCommandDeviceReconnectLocal;
import com.espressif.iot.command.device.common.IEspCommandDeviceReconnectLocal;
import com.espressif.iot.device.IEspDevice;
import com.espressif.iot.device.cache.IEspDeviceCache;
import com.espressif.iot.device.cache.IEspDeviceCache.NotifyType;
import com.espressif.iot.model.device.cache.EspDeviceCache;
import com.espressif.iot.type.device.IEspDeviceState;
import com.espressif.iot.type.net.IOTAddress;
import com.espressif.iot.type.net.WifiCipherType;

public class EspActionDeviceReconnectLocal implements IEspActionDeviceReconnectLocal
{
    
    private final static Logger log = Logger.getLogger(EspActionDeviceReconnectLocal.class);
    
    private IEspDevice __getDeviceByBssid(List<IEspDevice> deviceList, String bssid)
    {
        for (IEspDevice device : deviceList)
        {
            if (device.getBssid().equals(bssid))
            {
                return device;
            }
        }
        return null;
    }
    
    private IOTAddress __getIOTAddressByBssid(List<IOTAddress> iotAddressList, String bssid)
    {
        for (IOTAddress iotAddress : iotAddressList)
        {
            if (iotAddress.getBSSID().equals(bssid))
            {
                return iotAddress;
            }
        }
        return null;
    }

    private boolean __doActionDeviceReconnectLocal(List<IEspDevice> currentDeviceList, String router,
        String deviceBssid, String apSsid, WifiCipherType type, String... apPassword)
    {
        boolean result = false;
        List<IEspDevice> localDeviceList = new ArrayList<IEspDevice>();
        // 1. do device reconnect command
        IEspCommandDeviceReconnectLocal command = new EspCommandDeviceReconnectLocal();
        result = command.doCommandReconnectLocal(router, deviceBssid, apSsid, type, apPassword);
        if (result)
        {
            // 2. update currentDeviceList's router list, only update device's router
            List<IOTAddress> iotAddressList = EspBaseApiUtil.discoverDevices();
            IEspDevice device;
            for (IOTAddress iotAddress : iotAddressList)
            {
                device = __getDeviceByBssid(currentDeviceList, iotAddress.getBSSID());
                if (device != null)
                {
                    device.setRouter(iotAddress.getRouter());
                    localDeviceList.add(device);
                }
            }
            // 3. add state changed device to EspDeviceHandler
            IEspDeviceCache deviceCache = EspDeviceCache.getInstance();
            boolean isChanged = false;
            String currentDeviceBssid;
            IEspDeviceState currentDeviceState;
            IOTAddress currentIOTAddress;
            for (IEspDevice currentDevice : currentDeviceList)
            {
                currentDeviceBssid = currentDevice.getBssid();
                // For device state machine will check device's state if it transform.
                // To avoid device state transform by valid device state,
                // don't change the current device's state directly
                if(currentDeviceBssid.equals(deviceBssid))
                {
                    IEspDevice deviceCloned = currentDevice.cloneDevice();
                    currentDevice = deviceCloned;
                }
                currentIOTAddress = __getIOTAddressByBssid(iotAddressList, currentDeviceBssid);
                currentDeviceState = currentDevice.getDeviceState();
                if (currentIOTAddress != null)
                {
                    // clear offline state and add local state
                    if (!currentDeviceState.isStateLocal())
                    {
                        isChanged = true;
                        currentDeviceState.clearStateOffline();
                        currentDeviceState.addStateLocal();
                        deviceCache.addTransformedDeviceCache(currentDevice);
                    }
                }
                else
                {
                    // clear local state and add offline state if necessary
                    if (currentDeviceState.isStateLocal())
                    {
                        isChanged = true;
                        currentDeviceState.clearStateLocal();
                        if (!currentDeviceState.isStateInternet())
                        {
                            currentDeviceState.addStateOffline();
                        }
                        deviceCache.addTransformedDeviceCache(currentDevice);
                    }
                }
            }
            if (isChanged)
            {
                // simulate the device pull refresh, only the local state related might be changed
                deviceCache.notifyIUser(NotifyType.PULL_REFRESH);
            }
        }
        
        log.debug(Thread.currentThread().toString() + "##__doActionDeviceReconnectLocal(currentDeviceList=["
            + currentDeviceList + "],router=[" + router + "],deviceBssid=[" + deviceBssid + "],apSsid=[" + apSsid
            + "],type=[" + type + "],apPassword=[" + apPassword + "]): " + result);
        return result;
    }
    
    @Override
    public boolean doActionDeviceReconnectLocal(List<IEspDevice> currentDeviceList, String router, String deviceBssid,
        String apSsid, WifiCipherType type, String... apPassword)
    {
        return __doActionDeviceReconnectLocal(currentDeviceList, router, deviceBssid, apSsid, type, apPassword);
    }
    
}
