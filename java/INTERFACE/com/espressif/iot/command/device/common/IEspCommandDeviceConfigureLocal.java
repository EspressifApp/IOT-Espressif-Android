package com.espressif.iot.command.device.common;

import java.net.InetAddress;

import com.espressif.iot.command.IEspCommandLocal;
import com.espressif.iot.command.device.IEspCommandActivated;
import com.espressif.iot.command.device.IEspCommandUnactivated;

public interface IEspCommandDeviceConfigureLocal extends IEspCommandUnactivated, IEspCommandActivated, IEspCommandLocal
{
    /**
     * configure the device to an AP accessible to Internet, and make the device activate on Server
     * 
     * @param inetAddress the device's ip address
     * @param apSsid the Ap's ssid
     * @param apPassword the Ap's password
     * @param randomToken 40 randomToken
     * @return whether the command executed suc
     */
    boolean doCommandDeviceConfigureLocal(InetAddress inetAddress, String apSsid, String apPassword, String randomToken);
    
    /**
     * configure the device to an AP
     * 
     * @param inetAddress the device's ip address
     * @param apSsid the Ap's ssid
     * @param apPassword the Ap's password
     * @return whether the command executed suc
     */
    boolean doCommandDeviceConfigureLocal(InetAddress inetAddress, String apSsid, String apPassword);
    
    /**
     * make the device activate on Server
     * 
     * @param inetAddress the device's ip address
     * @param randomToken 40 randomToken
     * @return whether the command executed suc
     */
    boolean doCommandDeviceConfigureLocal(InetAddress inetAddress, String randomToken);
    
    /**
     * configure the mesh device to an AP accessible to Internet, and make the mesh device activate on Server
     * 
     * @param deviceBssid the mesh device's bssid
     * @param inetAddress the mesh device's ip address
     * @param apSsid the Ap's ssid
     * @param apPassword the Ap's password
     * @param randomToken 40 randomToken
     * @return whether the command executed suc
     */
    boolean doCommandMeshDeviceConfigureLocal(String deviceBssid, InetAddress inetAddress, String apSsid,
        String apPassword, String randomToken);
    
    /**
     * configure the mesh device to an AP
     * 
     * @param deviceBssid the mesh device's bssid
     * @param inetAddress the device's ip address
     * @param apSsid the Ap's ssid
     * @param apPassword the Ap's password
     * @return whether the command executed suc
     */
    boolean doCommandMeshDeviceConfigureLocal(String deviceBssid, InetAddress inetAddress, String apSsid,
        String apPassword);
    
    /**
     * make the mesh device activate on Server
     * 
     * @param deviceBssid the mesh device's bssid
     * @param inetAddress the mesh device's ip address
     * @param randomToken 40 randomToken
     * @return whether the command executed suc
     */
    boolean doCommandMeshDeviceConfigureLocal(String deviceBssid, InetAddress inetAddress, String randomToken);
}
