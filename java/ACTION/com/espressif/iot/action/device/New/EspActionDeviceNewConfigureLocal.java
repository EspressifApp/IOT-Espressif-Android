package com.espressif.iot.action.device.New;

import org.apache.log4j.Logger;

import com.espressif.iot.base.api.EspBaseApiUtil;
import com.espressif.iot.command.device.New.EspCommandDeviceNewConfigureLocal;
import com.espressif.iot.command.device.New.IEspCommandDeviceNewConfigureLocal;
import com.espressif.iot.db.IOTDeviceDBManager;
import com.espressif.iot.type.device.EspDeviceType;
import com.espressif.iot.type.device.state.EspDeviceState;
import com.espressif.iot.type.net.WifiCipherType;
import com.espressif.iot.user.builder.BEspUser;
import com.espressif.iot.util.BSSIDUtil;

public class EspActionDeviceNewConfigureLocal implements IEspActionDeviceNewConfigureLocal
{
    private final static Logger log = Logger.getLogger(EspActionDeviceNewConfigureLocal.class);
    
    @Override
    public long doActionDeviceNewConfigureLocal(String deviceBssid, String deviceSsid,
        WifiCipherType deviceWifiCipherType, String devicePassword, String apSsid, WifiCipherType apWifiCipherType,
        String apPassword, String randomToken)
        throws InterruptedException
    {
        boolean result = false;
        long deviceId = 0;
        // 1. connect to device
        boolean connectDeviceResult = EspBaseApiUtil.connect(deviceSsid, deviceWifiCipherType, devicePassword);
        // 2. post configure info(do EspCommandDeviceNewConfigureLocal)
        if (connectDeviceResult)
        {
            IEspCommandDeviceNewConfigureLocal command = new EspCommandDeviceNewConfigureLocal();
            result =
                command.doCommandDeviceNewConfigureLocal(deviceSsid,
                    deviceWifiCipherType,
                    devicePassword,
                    apSsid,
                    apWifiCipherType,
                    apPassword,
                    randomToken);
        }
        
        if (result)
        {
            String key = randomToken;
            String bssid = deviceBssid;
            int type = EspDeviceType.NEW.getSerial();
            EspDeviceState deviceState = new EspDeviceState();
            deviceState.addStateActivating();
            int state = deviceState.getStateValue();
            String name = BSSIDUtil.genDeviceNameByBSSID(deviceBssid);
            String rom_version = "";
            String latest_rom_version = "";
            long timestamp = System.currentTimeMillis();
            long userId = BEspUser.getBuilder().getInstance().getUserId();
            deviceId =
                IOTDeviceDBManager.getInstance().insertActivatingDevice(key,
                    bssid,
                    type,
                    state,
                    name,
                    rom_version,
                    latest_rom_version,
                    timestamp,
                    userId);
        }
//        Thread.sleep(500);
        log.debug(Thread.currentThread().toString() + "##doActionDeviceNewConfigureLocal(deviceBssid=[" + deviceBssid
            + "],deviceSsid=[" + deviceSsid + "],deviceWifiCipherType=[" + deviceWifiCipherType + "],devicePassword=["
            + devicePassword + "],apSsid=[" + apSsid + "],apWifiCipherType=[" + apWifiCipherType + "],apPassword=["
            + apPassword + "],randomToken=[" + randomToken + "]): " + deviceId);
        return deviceId;
    }
    
}
