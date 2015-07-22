package com.espressif.iot.device.upgrade;

import java.net.InetAddress;
import java.util.List;

import com.espressif.iot.type.net.IOTAddress;

public interface IEspDeviceDoUpgradeLocal extends IEspDeviceUpgrade
{
    
    static final long TIMEOUT_MILLISECONDS = 5 * 60 * 1000;
    
    final static int CONNECTION_TIMEOUT = 2 * 1000;
    
    final static int SO_TIMEOUT = 30 * 1000;
    
    final static String Authorization = "Authorization";
    
    final static String USER_BIN = "user_bin";
    
    final static String USER1_BIN = "user1.bin";
    
    final static String USER2_BIN = "user2.bin";
    
    final static String TOKEN = "token";
    
    final static String URL_DOWNLOAD_BIN = "https://iot.espressif.cn/v1/device/rom/";
    
    /**
     * and the router=null when you call the method
     * 
     * upgrade local by the latestRomVersion
     * 
     * @param inetAddress the inetAddress of the device
     * @param bssid the device's bssid
     * @param deviceKey the device's key
     * @param latestRomVersion the latestRomVersion
     * @return new InetAddress(if upgrade local suc) or null (if upgrade local fail)
     */
    IOTAddress doUpgradeLocal(InetAddress inetAddress, String bssid, String deviceKey, String latestRomVersion);
    
    /**
     * 
     * upgrade local by the latestRomVersion
     * 
     * @param inetAddress the inetAddress of the device
     * @param bssid the device's bssid
     * @param deviceKey the device's key
     * @param latestRomVersion the latestRomVersion
     * @param router the mesh device's router
     * @return the result from EspBaseUtilApi.discoverDevices() which contains the device itself
     */
    List<IOTAddress> doUpgradeMeshDeviceLocal(InetAddress inetAddress, String bssid, String deviceKey,
        String latestRomVersion, String router);
}
