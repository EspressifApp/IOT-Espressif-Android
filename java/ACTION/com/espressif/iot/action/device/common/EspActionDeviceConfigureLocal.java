package com.espressif.iot.action.device.common;

import java.net.InetAddress;

import org.apache.log4j.Logger;

import com.espressif.iot.base.api.EspBaseApiUtil;
import com.espressif.iot.command.device.common.EspCommandDeviceConfigureLocal;
import com.espressif.iot.command.device.common.IEspCommandDeviceConfigureLocal;
import com.espressif.iot.type.net.IOTAddress;

public class EspActionDeviceConfigureLocal implements IEspActionDeviceConfigureLocal
{
    
    private Logger log = Logger.getLogger(EspActionDeviceConfigureLocal.class);
    
    private InetAddress mInetAddress;
    
    private void __initInetAddrAndRouter2(boolean discoverRequired, InetAddress inetAddress, boolean isMeshDevice,
        String deviceBssid)
    {
        IOTAddress iotAddress = null;
        if (discoverRequired && deviceBssid != null)
        {
            iotAddress = EspBaseApiUtil.discoverDevice(deviceBssid);
        }
        if (iotAddress != null)
        {
            this.mInetAddress = iotAddress.getInetAddress();
        }
        else
        {
            this.mInetAddress = inetAddress;
        }
    }
    
    @Override
    public boolean doActionDeviceConfigureLocal(boolean discoverRequired, InetAddress inetAddress, String apSsid,
        String apPassword, String randomToken, String deviceBssid)
    {
        __initInetAddrAndRouter2(discoverRequired, inetAddress, false, deviceBssid);
        if (mInetAddress == null)
        {
            log.warn("doActionDeviceConfigureLocal fail for mInetAddress = null");
            return false;
        }
        IEspCommandDeviceConfigureLocal command = new EspCommandDeviceConfigureLocal();
        return command.doCommandDeviceConfigureLocal(mInetAddress, apSsid, apPassword, randomToken);
    }
    
    @Override
    public boolean doActionDeviceConfigureLocal(boolean discoverRequired, InetAddress inetAddress, String apSsid,
        String apPassword, String deviceBssid)
    {
        __initInetAddrAndRouter2(discoverRequired, inetAddress, false, deviceBssid);
        if (mInetAddress == null)
        {
            log.warn("doActionDeviceConfigureLocal fail for mInetAddress = null");
            return false;
        }
        IEspCommandDeviceConfigureLocal command = new EspCommandDeviceConfigureLocal();
        return command.doCommandDeviceConfigureLocal(mInetAddress, apSsid, apPassword);
    }
    
    @Override
    public boolean doActionDeviceConfigureLocal(boolean discoverRequired, InetAddress inetAddress, String randomToken,
        String deviceBssid)
    {
        __initInetAddrAndRouter2(discoverRequired, inetAddress, false, deviceBssid);
        if (mInetAddress == null)
        {
            log.warn("doActionDeviceConfigureLocal fail for mInetAddress = null");
            return false;
        }
        IEspCommandDeviceConfigureLocal command = new EspCommandDeviceConfigureLocal();
        return command.doCommandDeviceConfigureLocal(mInetAddress, randomToken);
    }
    
    @Override
    public boolean doActionMeshDeviceConfigureLocal(boolean discoverRequired, String deviceBssid,
        InetAddress inetAddress, String apSsid, String apPassword, String randomToken)
    {
        __initInetAddrAndRouter2(discoverRequired, inetAddress, true, deviceBssid);
        if (mInetAddress == null || deviceBssid == null)
        {
            log.warn("doActionMeshDeviceConfigureLocal 1 fail for mInetAddress = null or deviceBssid == null");
            return false;
        }
        IEspCommandDeviceConfigureLocal command = new EspCommandDeviceConfigureLocal();
        return command.doCommandMeshDeviceConfigureLocal(deviceBssid, mInetAddress, apSsid, apPassword, randomToken);
    }
    
    @Override
    public boolean doActionMeshDeviceConfigureLocal(boolean discoverRequired, String deviceBssid,
        InetAddress inetAddress, String apSsid, String apPassword)
    {
        __initInetAddrAndRouter2(discoverRequired, inetAddress, true, deviceBssid);
        if (mInetAddress == null || deviceBssid == null)
        {
            log.warn("doActionDeviceConfigureLocal 2 fail for mInetAddress = null or deviceBssid == null");
            return false;
        }
        IEspCommandDeviceConfigureLocal command = new EspCommandDeviceConfigureLocal();
        return command.doCommandMeshDeviceConfigureLocal(deviceBssid, mInetAddress, apSsid, apPassword);
    }
    
    @Override
    public boolean doActionMeshDeviceConfigureLocal(boolean discoverRequired, String deviceBssid,
        InetAddress inetAddress, String randomToken)
    {
        __initInetAddrAndRouter2(discoverRequired, inetAddress, true, deviceBssid);
        if (mInetAddress == null || deviceBssid == null)
        {
            log.warn("doActionDeviceConfigureLocal 3 fail for mInetAddress = null or deviceBssid == null");
            return false;
        }
        IEspCommandDeviceConfigureLocal command = new EspCommandDeviceConfigureLocal();
        return command.doCommandMeshDeviceConfigureLocal(deviceBssid, mInetAddress, randomToken);
    }
    
}
