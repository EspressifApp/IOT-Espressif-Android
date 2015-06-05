package com.espressif.iot.base.net.wifi;

import java.util.List;

import com.espressif.iot.type.net.WifiCipherType;

import android.net.wifi.ScanResult;

public interface IWifiAdmin
{
    /**
     * connect timeout of milliseconds
     */
    static final long CONNECT_TIMEOUT = 30 * 1000;
    
    /**
     * connect suc checking interval of milliseconds
     */
    static final long CONNECT_CHECK_INTERVAL = 200;
    
    /**
     * scan timeout of milliseconds
     */
    static final long SCAN_TIMEOUT = 3 * 1000;
    
    /**
     * scan suc checking interval of milliseconds
     */
    static final long SCAN_CHECK_INTERVAL = 100;
    
    /**
     * 
     * @return whether the wifi is enabled
     */
    boolean isWifiEnabled();
    
    /**
     * 
     * @return whether the wifi is available(available don't mean connected)
     */
    boolean isWifiAvailable();
    
    /**
     * 
     * @return whether the wifi is connected to some AP
     */
    boolean isWifiConnected();
    
    /**
     * 
     * @return whether the mobile is available
     */
    boolean isMobileAvailable();
    
    /**
     * 
     * @return whether the wifi or mobile is available
     */
    boolean isNetworkAvailable();
    
    /**
     * 
     * @param ssid the AP's ssid
     * @return whether the wifi is connected to the AP
     */
    boolean isWifiConnected(final String ssid);
    
    /**
     * 
     * @return whether the wifi is connected or is connecting
     */
    boolean isConnectedOrConnecting();
    
    /**
     * 
     * open the wifi automatically
     * 
     * @return whether the wifi is opened
     */
    boolean setWifiEnabled();
    
    /**
     * 
     * if the wifi is conneted to AP return true directly, otherwise enable the wifi connect to AP with
     * WifiConfiguration saved by Android System the result only indicate whether the connection is started
     * 
     * @param ssid the AP's ssid
     * @return whether the connection is enabled
     */
    boolean enableConnected(final String ssid);
    
    /**
     * 
     * if the wifi is conneted to AP return true directly, otherwise connect to AP with WifiConfiguration saved by
     * Android System the result indicate whether the connection is suc
     * 
     * @param ssid the AP's ssid
     * @return whether the connection is suc
     * @throws InterruptedException when connect is interrupted
     */
    boolean connect(final String ssid)
        throws InterruptedException;
    
    /**
     * @deprecated Use {@link ##enableConnected(String, boolean)} instead of it,
     * and the isSsidHidden=false when you call the method
     * 
     * if the wifi is conneted to AP return true directly, otherwise enable the wifi connect to AP with the parameters
     * 
     * @param ssid the AP's ssid
     * @param type the wifi cipher type
     * @param password the password of the AP,if WifiCipherType isn't OPEN
     * @return whether the connection is started
     */
    boolean enableConnected(final String ssid, WifiCipherType type, String... password);
    
    /**
     * 
     * if the wifi is conneted to AP return true directly, otherwise enable the wifi connect to AP with the parameters
     * 
     * @param ssid the AP's ssid
     * @param type the wifi cipher type
     * @param isSsidHidden whether the Ap's ssid is hidden
     * @param password the password of the AP,if WifiCipherType isn't OPEN
     * @return whether the connection is started
     */
    boolean enableConnected(final String ssid, WifiCipherType type, boolean isSsidHidden, String... password);
    
    /**
     * @deprecated Use {@link #connect(String, WifiCipherType, boolean, String...)} instead of it,
     * and the isSsidHidden=false when you call the method
     * 
     * if the wifi is conneted to AP return true directly, otherwise connect to AP with WifiConfiguration saved by
     * Android System firstly, if the connection is fail, connect to AP with the parameters
     * 
     * @param ssid the AP's ssid
     * @param type the wifi cipher type
     * @param password the password of the AP,if WifiCipherType isn't OPEN
     * @return whether the connection is suc
     * @throws InterruptedException when connect is interrupted
     */
    boolean connect(final String ssid, WifiCipherType type, String... password)
        throws InterruptedException;
    
    /**
     * 
     * if the wifi is conneted to AP return true directly, otherwise connect to AP with WifiConfiguration saved by
     * Android System firstly, if the connection is fail, connect to AP with the parameters
     * 
     * @param ssid the AP's ssid
     * @param type the wifi cipher type
     * @param isSsidHidden whether the Ap's ssid is hidden
     * @param password the password of the AP,if WifiCipherType isn't OPEN
     * @return whether the connection is suc
     * @throws InterruptedException when connect is interrupted
     */
    boolean connect(final String ssid, WifiCipherType type, boolean isSsidHidden, String... password)
        throws InterruptedException;
    
    /**
     * 
     * @return the AP's ssid if the wifi is connected to some AP, otherwise return null
     */
    String getWifiConnectedSsid();
    
    /**
     * 1. start scan 2. wait Android System Broadcast to get scan list
     * 
     * @return wifi ScanResult list or Empty list
     */
    List<ScanResult> scan();
}
