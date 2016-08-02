package com.espressif.iot.model.device;

import java.net.InetAddress;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;

import com.espressif.iot.action.device.common.EspActionDeviceConfigureLocal;
import com.espressif.iot.action.device.common.IEspActionDeviceConfigureLocal;
import com.espressif.iot.action.device.configure.EspActionDeviceConfigureActivateInternet;
import com.espressif.iot.action.device.configure.IEspActionDeviceConfigureActivateInternet;
import com.espressif.iot.device.IEspDevice;
import com.espressif.iot.device.IEspDeviceConfigure;
import com.espressif.iot.type.device.EspDeviceType;
import com.espressif.iot.type.device.state.EspDeviceState;
import com.espressif.iot.user.IEspUser;
import com.espressif.iot.user.builder.BEspUser;

public class EspDeviceConfigure extends EspDevice implements IEspDeviceConfigure
{
    
    private final static Logger log = Logger.getLogger(EspDeviceConfigure.class);
    
    private Future<?> mFuture;
    
    public EspDeviceConfigure(String bssid, String randomToken)
    {
        // EspDeviceConfigure's deviceId should be 0
        // although the default value is 0, assign the value here just to make it significant
        this.mDeviceId = 0;
        this.mBssid = bssid;
        this.mDeviceType = EspDeviceType.NEW;
        this.mDeviceState = new EspDeviceState();
        this.mDeviceState.addStateNew();
        setKey(randomToken);
    }
    
    @Override
    public boolean cancel(boolean mayInterruptIfRunning)
    {
        if (mFuture != null)
        {
            log.info(Thread.currentThread().toString() + "##cancel(mayInterruptIfRunning=[" + mayInterruptIfRunning
                + "])");
            boolean result = this.mFuture.cancel(true);
            this.mFuture = null;
            return result;
        }
        return true;
    }
    
    @Override
    public void setFuture(Future<?> future)
    {
        this.mFuture = future;
    }
    
    @Override
    public IEspDevice doActionDeviceConfigureActivateInternet(long userId, String userKey, String randomToken)
    {
        String deviceName = mDeviceName;
        IEspActionDeviceConfigureActivateInternet action = new EspActionDeviceConfigureActivateInternet();
        IEspDevice newDevice = action.doActionDeviceConfigureActivateInternet(userId, userKey, randomToken);
        if (newDevice != null && !newDevice.getName().equals(deviceName))
        {
            newDevice.setName(deviceName);
            IEspUser user = BEspUser.getBuilder().getInstance();
            user.doActionRename(newDevice, deviceName);
        }
        return newDevice;
    }
    
    @Override
    public boolean doActionDeviceConfigureLocal(boolean discoverRequired, InetAddress inetAddress, String apSsid,
        String apPassword, String randomToken, String deviceBssid)
    {
        IEspActionDeviceConfigureLocal action = new EspActionDeviceConfigureLocal();
        return action.doActionDeviceConfigureLocal(discoverRequired,
            inetAddress,
            apSsid,
            apPassword,
            randomToken,
            deviceBssid);
    }
    
    @Override
    public boolean doActionDeviceConfigureLocal(boolean discoverRequired, InetAddress inetAddress, String apSsid,
        String apPassword, String deviceBssid)
    {
        IEspActionDeviceConfigureLocal action = new EspActionDeviceConfigureLocal();
        return action.doActionDeviceConfigureLocal(discoverRequired, inetAddress, apSsid, apPassword, deviceBssid);
    }
    
    @Override
    public boolean doActionDeviceConfigureLocal(boolean discoverRequired, InetAddress inetAddress, String randomToken,
        String deviceBssid)
    {
        IEspActionDeviceConfigureLocal action = new EspActionDeviceConfigureLocal();
        return action.doActionDeviceConfigureLocal(discoverRequired, inetAddress, randomToken, deviceBssid);
    }
    
    @Override
    public boolean doActionMeshDeviceConfigureLocal(boolean discoverRequired, String deviceBssid,
        InetAddress inetAddress, String apSsid, String apPassword, String randomToken)
    {
        IEspActionDeviceConfigureLocal action = new EspActionDeviceConfigureLocal();
        return action.doActionMeshDeviceConfigureLocal(discoverRequired,
            deviceBssid,
            inetAddress,
            apSsid,
            apPassword,
            randomToken);
    }
    
    @Override
    public boolean doActionMeshDeviceConfigureLocal(boolean discoverRequired, String deviceBssid,
        InetAddress inetAddress, String apSsid, String apPassword)
    {
        IEspActionDeviceConfigureLocal action = new EspActionDeviceConfigureLocal();
        return action.doActionMeshDeviceConfigureLocal(discoverRequired, deviceBssid, inetAddress, apSsid, apPassword);
    }
    
    @Override
    public boolean doActionMeshDeviceConfigureLocal(boolean discoverRequired, String deviceBssid,
        InetAddress inetAddress, String randomToken)
    {
        IEspActionDeviceConfigureLocal action = new EspActionDeviceConfigureLocal();
        return action.doActionMeshDeviceConfigureLocal(discoverRequired, deviceBssid, inetAddress, randomToken);
    }
    
}
