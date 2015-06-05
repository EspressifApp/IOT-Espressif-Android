package com.espressif.iot.action.device.mesh;

import com.espressif.iot.action.IEspActionDB;
import com.espressif.iot.action.IEspActionLocal;
import com.espressif.iot.command.device.mesh.IEspCommandMeshConfigureLocal.MeshMode;
import com.espressif.iot.device.IEspDeviceNew;

public interface IEspActionMeshDeviceConfigureLocal extends IEspActionLocal, IEspActionDB
{
    public enum MeshDeviceConfigureLocalResult
    {
        SUC,
        FAIL,
    }
    
    /**
     * configure the Mesh Device
     * 
     * @param deviceNew the new device to be configured
     * @param apSsid the Ap's ssid or null
     * @param apPassword the Ap's password or null
     * @param meshMode the mesh device's mode
     * @param randomToken the randomToken
     * @return the MeshDeviceConfigureLocalResult
     */
    MeshDeviceConfigureLocalResult doActionMeshDeviceConfigureLocal(IEspDeviceNew deviceNew, MeshMode meshMode,
        String apSsid, String apPassword, String randomToken);
}
