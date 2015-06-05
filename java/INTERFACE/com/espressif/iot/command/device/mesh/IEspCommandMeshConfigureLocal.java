package com.espressif.iot.command.device.mesh;

import java.net.InetAddress;

import com.espressif.iot.command.IEspCommandLocal;
import com.espressif.iot.command.device.IEspCommandActivated;
import com.espressif.iot.command.device.IEspCommandUnactivated;

public interface IEspCommandMeshConfigureLocal extends IEspCommandUnactivated, IEspCommandActivated, IEspCommandLocal
{
    public enum MeshMode
    {
        // 0 means close all mesh
        MESH_OFF,
        // 1 means make mesh local
        MESH_LOCAL,
        // 2 means make mesh online
        MESH_ONLINE,
    }
    
    /**
     * configure the Mesh device
     * 
     * @param router the mesh device's router
     * @param deviceBssid the mesh device's router
     * @param meshMode the mesh device's mode
     * @param inetAddress the inetAddress of the device
     * @param apSsid the Ap's ssid or null
     * @param apPassword the Ap's password or null
     * @param randomToken 40 randomToken
     * @return whether the command executed suc
     */
    boolean doCommandMeshConfigureLocal(String router, String deviceBssid, MeshMode meshMode, InetAddress inetAddress,
        String apSsid, String apPassword, String randomToken);
}
