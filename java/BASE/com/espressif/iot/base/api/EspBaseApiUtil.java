package com.espressif.iot.base.api;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.ExecutorService;

import org.json.JSONObject;

import android.net.wifi.ScanResult;

import com.espressif.iot.base.net.proxy.MeshCommunicationUtils;
import com.espressif.iot.base.net.rest2.EspHttpDownloadUtil;
import com.espressif.iot.base.net.rest2.EspHttpDownloadUtil.ProgressUpdateListener;
import com.espressif.iot.base.net.rest2.EspHttpUtil;
import com.espressif.iot.base.net.rest2.EspMeshDiscoverUtil;
import com.espressif.iot.base.net.wifi.WifiAdmin;
import com.espressif.iot.base.threadpool.CachedThreadPool;
import com.espressif.iot.base.time.EspTimeManager;
import com.espressif.iot.base.upgrade.UpgradeAdministrator;
import com.espressif.iot.type.net.HeaderPair;
import com.espressif.iot.type.net.IOTAddress;
import com.espressif.iot.type.net.WifiCipherType;
import com.espressif.iot.type.upgrade.EspUpgradeApkResult;

public class EspBaseApiUtil
{
    /**
     * @see ExecutorService
     * @param task
     * @return
     */
    public static <T> Future<T> submit(Callable<T> task)
    {
        return CachedThreadPool.getInstance().submit(task);
    }
    
    /**
     * @see ExecutorService
     * @param task
     * @return
     */
    public static Future<?> submit(Runnable task)
    {
        return CachedThreadPool.getInstance().submit(task);
    }
    
    /**
     * @param task the task to be computed
     * @param taskSuc to do task if task suc
     * @param taskFail to do task if task fail
     * @param taskCancel to do task if task is cancelled
     * @return
     */
    public static <T> Future<T> submit(final Callable<T> task, final Runnable taskSuc, final Runnable taskFail,
        final Runnable taskCancel)
    {
        return CachedThreadPool.getInstance().submit(task, taskSuc, taskFail, taskCancel);
    }
    
    /**
     * 
     * @param task the task to be executed
     * @param taskSuc to do task if task suc
     * @param taskFail to do task if task fail
     * @param taskCancel to do task if task is cancelled
     * @return T executed result
     */
    public static <T> T execute(final Callable<T> task, final Runnable taskSuc, final Runnable taskFail,
        final Runnable taskCancel)
    {
        return CachedThreadPool.getInstance().execute(task, taskSuc, taskFail, taskCancel);
    }
    
    /**
     * cancel all of the task in the threadpool
     */
    public static void cancelAllTask()
    {
        CachedThreadPool.getInstance().cancelAllTask();
    }
    
    /**
     * 
     * @param url the url string
     * @param headers the head-key and head-value pair
     * @return the JSONObject
     */
    public static JSONObject Get(String url, HeaderPair... headers)
    {
        return EspHttpUtil.Get(url, headers);
    }
    
    /**
     * 
     * @param url the url string
     * @param headers the head-key and head-value pair
     * @param json the JSONObject
     * @return the JSONObject
     */
    public static JSONObject Post(String url, JSONObject json, HeaderPair... headers)
    {
        return EspHttpUtil.Post(url, json, headers);
    }

    /**
     * post request instantly(without receiving response)
     * 
     * @param url the url string
     * @param headers the head-key and head-value pair
     * @param json the JSONObject
     * @param disconnectedCallback disconnected callback
     * @return the JSONObject
     */
    public static void PostInstantly(String url, JSONObject json, Runnable disconnectedCallback, HeaderPair... headers)
    {
        EspHttpUtil.PostInstantly(url, json, disconnectedCallback, headers);
    }
    
    /**
     * post request instantly(without receiving response) to mesh device
     * 
     * @param urlt the url string
     * @param deviceBssid the device's bssid
     * @param json the JSONObject
     * @param disconnectedCallback disconnected callback
     * @param headers the head-key and head-value pair
     */
    public static void PostForJsonInstantly(String url, String deviceBssid, JSONObject json,
        Runnable disconnectedCallback, HeaderPair... headers)
    {
        MeshCommunicationUtils.HttpNonResponsePost(url, deviceBssid, json, headers);
    }
    
    /**
     * execute GET to get JSONObject by Mesh Net
     * @param uriStr the uri String
     * @param deviceBssid the bssid of the device
     * @param headers the headers of the request
     * @return the JSONObject result
     */
    public static JSONObject GetForJson(String uriStr, String deviceBssid, HeaderPair... headers)
    {
        return MeshCommunicationUtils.HttpGet(uriStr, deviceBssid, headers);
    }
    
    /**
     * execute POST to get JSONObject by Mesh Net
     * @param uriStr the uri String
     * @param deviceBssid the bssid of the device
     * @param headers the headers of the request
     * @return the JSONObject result
     */
    public static JSONObject PostForJson(String uriStr, String deviceBssid, JSONObject json, HeaderPair... headers)
    {
        return MeshCommunicationUtils.HttpPost(uriStr, deviceBssid, json, headers);
    }
    
    /**
     * discover the devices on the same AP or in the mesh net which root mesh device is on the same AP
     * 
     * @return the list of @see IOTAddress
     */
    public static List<IOTAddress> discoverDevices()
    {
        return EspMeshDiscoverUtil.discoverIOTDevices();
    }
    
    /**
     * discover the specific device whether is on the same AP or in the mesh net which root mesh device is on the same
     * AP
     * 
     * @param bssid the device's bssid
     * @return @see IOTAddress
     */
    public static IOTAddress discoverDevice(String bssid)
    {
        return EspMeshDiscoverUtil.discoverIOTDevice(bssid);
    }
    
    /**
     * 
     * @return whether the wifi is enabled
     */
    public static boolean isWifiEnabled()
    {
        return WifiAdmin.getInstance().isWifiEnabled();
    }
    
    /**
     * 
     * @return whether the wifi is available(available don't mean connected)
     */
    public static boolean isWifiAvailable()
    {
        return WifiAdmin.getInstance().isWifiAvailable();
    }
    
    /**
     * 
     * @return whether the wifi is connected to some AP
     */
    public static boolean isWifiConnected()
    {
        return WifiAdmin.getInstance().isWifiConnected();
    }
    
    /**
     * 
     * @return whether the mobile is available
     */
    public static boolean isMobileAvailable()
    {
        return WifiAdmin.getInstance().isMobileAvailable();
    }
    
    /**
     * 
     * @return whether the wifi or mobile is available
     */
    public static boolean isNetworkAvailable()
    {
        return WifiAdmin.getInstance().isNetworkAvailable();
    }
    
    /**
     * 
     * @param ssid the AP's ssid
     * @return whether the wifi is connected to the AP
     */
    public static boolean isWifiConnected(final String ssid)
    {
        return WifiAdmin.getInstance().isWifiConnected(ssid);
    }
    
    /**
     * 
     * @return whether the wifi is connected or is connecting
     */
    public static boolean isConnectedOrConnecting()
    {
        return WifiAdmin.getInstance().isConnectedOrConnecting();
    }
    
    /**
     * 
     * open the wifi automatically
     * 
     * @return whether the wifi is opened
     */
    public static boolean setWifiEnabled()
    {
        return WifiAdmin.getInstance().setWifiEnabled();
    }
    
    /**
     * 
     * if the wifi is conneted to AP return true directly, otherwise enable the wifi connect to AP with
     * WifiConfiguration saved by Android System the result only indicate whether the connection is started
     * 
     * @param ssid the AP's ssid
     * @return whether the connection is enabled
     */
    public static boolean enableConnected(final String ssid)
    {
        return WifiAdmin.getInstance().enableConnected(ssid);
    }
    
    /**
     * 
     * if the wifi is conneted to AP return true directly, otherwise connect to AP with WifiConfiguration saved by
     * Android System the result indicate whether the connection is suc
     * 
     * @param ssid the AP's ssid
     * @return whether the connection is suc
     * @throws InterruptedException when the connect is interrupted
     */
    public static boolean connect(final String ssid)
        throws InterruptedException
    {
        return WifiAdmin.getInstance().connect(ssid);
    }
    
    /**
     * 
     * if the wifi is conneted to AP return true directly, otherwise enable the wifi connect to AP with the parameters
     * 
     * @param ssid the AP's ssid
     * @param type the wifi cipher type
     * @param password the password of the AP,if WifiCipherType isn't OPEN
     * @return whether the connection is started
     */
    public static boolean enableConnected(final String ssid, WifiCipherType type, String... password)
    {
        return WifiAdmin.getInstance().enableConnected(ssid, type, password);
    }
    
    /**
     * 
     * if the wifi is conneted to AP return true directly, otherwise connect to AP with WifiConfiguration saved by
     * Android System firstly, if the connection is fail, connect to AP with the parameters
     * 
     * @param ssid the AP's ssid
     * @param type the wifi cipher type
     * @param password the password of the AP,if WifiCipherType isn't OPEN
     * @return whether the connection is suc
     * @throws InterruptedException when the connect is interrupted
     */
    public static boolean connect(final String ssid, WifiCipherType type, String... password)
        throws InterruptedException
    {
        return WifiAdmin.getInstance().connect(ssid, type, password);
    }
    
    /**
     * 1. start scan 2. wait Android System Broadcast to get scan list
     * 
     * @return wifi ScanResult list or Empty list
     */
    public static List<ScanResult> scan()
    {
        return WifiAdmin.getInstance().scan();
    }
    
    /**
     * 
     * @return the AP's ssid if the wifi is connected to some AP, otherwise return null
     */
    public static String getWifiConnectedSsid()
    {
        return WifiAdmin.getInstance().getWifiConnectedSsid();
    }
    
    /**
     * 
     * @param UTCTimeStamp the utc time to be compared
     * @return whether the UTCTimeStamp is the same date of today
     */
    public static boolean isDateToday(long UTCTimeStamp)
    {
        return EspTimeManager.getInstance().isDateToday(UTCTimeStamp);
    }
    
    /**
     * (it will get UTC time from Server first time, next time, it will use local time to calculate the UTC time from
     * Server, if User change the Time or Date in Android System, it will get UTC time from Server again)
     * 
     * @return the UTC time from server, if fail it will return Long.MIN_VALUE
     */
    public static long getUTCTimeLong()
    {
        return EspTimeManager.getInstance().getUTCTimeLong();
    }
    
    /**
     * Check whether there is a higher version apk on the server and upgrade
     * 
     * @return the result of upgrade
     */
    public static EspUpgradeApkResult upgradeApk()
    {
        return UpgradeAdministrator.getInstance().upgradeApk();
    }
    
    
    /**
     * Check whether there is a higher version apk on the server and upgrade
     * 
     * @param listener
     * @return the result of upgrade
     */
    public static EspUpgradeApkResult upgradeApk(ProgressUpdateListener listener)
    {
        return UpgradeAdministrator.getInstance().upgradeApk(listener);
    }
    
    /**
     * download the file from Internet by HTTP protocol
     * 
     * @param progressListener the progress update Listener
     * @param url the url of the file to be downloaded
     * @param folderPath the folderPath where to save the file
     * @param saveFileName the filename what the file to be saved
     * @param headers the head-key and head-value pair
     * @return whether the file is downloaded suc
     */
    public static boolean download(ProgressUpdateListener progressListener, String url, String folderPath,
        String saveFileName, HeaderPair... headers)
    {
        return EspHttpDownloadUtil.download(progressListener, url, folderPath, saveFileName, headers);
    }
}
