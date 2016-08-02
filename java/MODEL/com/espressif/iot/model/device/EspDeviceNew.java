package com.espressif.iot.model.device;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;

import android.text.TextUtils;

import com.espressif.iot.action.device.New.EspActionDeviceNewActivateInternet;
import com.espressif.iot.action.device.New.EspActionDeviceNewConfigureLocal;
import com.espressif.iot.action.device.New.IEspActionDeviceNewActivateInternet;
import com.espressif.iot.action.device.New.IEspActionDeviceNewConfigureLocal;
import com.espressif.iot.base.api.EspBaseApiUtil;
import com.espressif.iot.device.IEspDevice;
import com.espressif.iot.device.IEspDeviceNew;
import com.espressif.iot.device.statemachine.IEspDeviceStateMachine;
import com.espressif.iot.device.statemachine.IEspDeviceStateMachine.Direction;
import com.espressif.iot.model.device.statemachine.EspDeviceStateMachine;
import com.espressif.iot.type.device.EspDeviceType;
import com.espressif.iot.type.device.state.EspDeviceState;
import com.espressif.iot.type.net.WifiCipherType;
import com.espressif.iot.user.IEspUser;
import com.espressif.iot.user.builder.BEspUser;
import com.espressif.iot.util.BSSIDUtil;

public class EspDeviceNew extends EspDevice implements IEspDeviceNew
{
    private final static Logger log = Logger.getLogger(EspDeviceNew.class);
    
    private int mRssi;
    
    private WifiCipherType mWifiCipherType;
    
    private String mSsid;
    
    private String mApSsid;
    
    private WifiCipherType mApWifiCipherType;
    
    private String mApPassword;
    
    private Future<?> mFuture;
    
    public EspDeviceNew(String ssid, String bssid, WifiCipherType wifiCipherType, int rssi)
    {
        // EspDeviceNew's deviceId should be 0
        // although the default value is 0, assign the value here just to make it significant
        this.mDeviceId = 0;
        this.mSsid = ssid;
        this.mBssid = bssid;
        this.mWifiCipherType = wifiCipherType;
        this.mRssi = rssi;
        this.mDeviceName = BSSIDUtil.genDeviceNameByBSSID(bssid);
        this.mDeviceType = EspDeviceType.NEW;
    }
    
    public EspDeviceNew(String ssid, String bssid, WifiCipherType wifiCipherType, int rssi, int state)
    {
        // EspDeviceNew's deviceId should be 0
        // although the default value is 0, assign the value here just to make it significant
        this.mDeviceId = 0;
        this.mDeviceState = new EspDeviceState(state);
        this.mSsid = ssid;
        this.mBssid = bssid;
        this.mWifiCipherType = wifiCipherType;
        this.mRssi = rssi;
        this.mDeviceName = BSSIDUtil.genDeviceNameByBSSID(bssid);
        this.mDeviceType = EspDeviceType.NEW;
    }
    
    @Override
    public void setFuture(Future<?> future)
    {
        this.mFuture = future;
    }
    
    @Override
    public boolean cancel(boolean mayInterruptIfRunning)
    {
        if (this.mFuture != null)
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
    public void resume()
    {
        // mFuture!=null means that the task is executing
        if (mFuture == null)
        {
            log.info(Thread.currentThread().toString() + "##resume(): start");
            final IEspDeviceStateMachine stateMachine = EspDeviceStateMachine.getInstance();
            Callable<?> task = new Callable<IEspDevice>()
            {
                @Override
                public IEspDevice call()
                    throws Exception
                {
                    IEspUser user = BEspUser.getBuilder().getInstance();
                    long userId = user.getUserId();
                    String userKey = user.getUserKey();
                    String randomToken = EspDeviceNew.this.getKey();
                    long negativeDeviceId = EspDeviceNew.this.getId();
                    IEspDevice result =
                        EspDeviceNew.this.doActionDeviceNewActivateInternet(userId,
                            userKey,
                            randomToken,
                            negativeDeviceId);
                    if (result != null)
                    {
                        stateMachine.transformState(result, Direction.SUC);
                        return result;
                    }
                    // note: if fail, must return null instead of False
                    else
                    {
                        return null;
                    }
                }
            };
            Runnable taskSuc = null;
            Runnable taskFail = new Runnable()
            {
                
                @Override
                public void run()
                {
                    log.info(Thread.currentThread().toString() + "##resume(): fail");
                    stateMachine.transformState(EspDeviceNew.this, Direction.FAIL);
                }
                
            };
            Future<?> future = EspBaseApiUtil.submit(task, taskSuc, taskFail, null);
            EspDeviceNew.this.setFuture(future);
        }
    }
    
    @Override
    public void setRssi(int rssi)
    {
        this.mRssi = rssi;
    }
    
    @Override
    public int getRssi()
    {
        return this.mRssi;
    }
    
    @Override
    public WifiCipherType getWifiCipherType()
    {
        return this.mWifiCipherType;
    }
    
    @Override
    public String getDefaultPassword()
    {
        if (getIsMeshDevice())
        {
            return "espressif";
        }
        else
        {
            final String softap_pwd_first_half = BSSIDUtil.restoreSoftApBSSID(mBssid);
            final String softap_pwd_second_half = "_v*%W>L<@i&Nxe!";
            return softap_pwd_first_half + softap_pwd_second_half;
        }
    }
    
    @Override
    public void setSsid(String ssid)
    {
        this.mSsid = ssid;
    }
    
    @Override
    public String getSsid()
    {
        return this.mSsid;
    }
    
    @Override
    public void setApSsid(String apSsid)
    {
        this.mApSsid = apSsid;
    }
    
    @Override
    public String getApSsid()
    {
        return this.mApSsid;
    }
    
    @Override
    public void setApWifiCipherType(WifiCipherType apWifiCipherType)
    {
        this.mApWifiCipherType = apWifiCipherType;
    }
    
    @Override
    public WifiCipherType getApWifiCipherType()
    {
        return this.mApWifiCipherType;
    }
    
    @Override
    public void setApPassword(String apPassword)
    {
        this.mApPassword = apPassword;
    }
    
    @Override
    public String getApPassword()
    {
        return this.mApPassword;
    }
    
    @Override
    public void saveInDB()
    {
        // ignore
    }
    
    @Override
    public long doActionDeviceNewConfigureLocal(String deviceBssid, String deviceSsid,
        WifiCipherType deviceWifiCipherType, String devicePassword, String apSsid, WifiCipherType apWifiCipherType,
        String apPassword, String randomToken)
        throws InterruptedException
    {
        IEspActionDeviceNewConfigureLocal action = new EspActionDeviceNewConfigureLocal();
        this.mDeviceId =
            action.doActionDeviceNewConfigureLocal(deviceBssid,
                deviceSsid,
                deviceWifiCipherType,
                devicePassword,
                apSsid,
                apWifiCipherType,
                apPassword,
                randomToken);
        // if device configure suc, save random token as device key
        if (this.mDeviceId != 0)
        {
            this.mDeviceKey = randomToken;
        }
        return this.mDeviceId;
    }
    
    @Override
    public IEspDevice doActionDeviceNewActivateInternet(long userId, String userKey, String randomToken,
        long negativeDeviceId)
        throws InterruptedException
    {
        String deviceName = mDeviceName;
        IEspActionDeviceNewActivateInternet action = new EspActionDeviceNewActivateInternet();
        IEspDevice device = action.doActionDeviceNewActivateInternet(userId, userKey, randomToken, this.mDeviceId);
        if (!device.getName().equals(mDeviceName))
        {
            device.setName(deviceName);
            IEspUser user = BEspUser.getBuilder().getInstance();
            user.doActionRename(device, deviceName);
        }
        return device;
    }
    
    @Override
    public boolean getIsMeshDevice()
    {
        if (TextUtils.isEmpty(mSsid))
        {
            return false;
        }
        
        for (String meshPrefix : IEspUser.MESH_DEVICE_SSID_PREFIX)
        {
            if (mSsid.startsWith(meshPrefix))
            {
                return true;
            }
        }
        
        return false;
    }
    
}
