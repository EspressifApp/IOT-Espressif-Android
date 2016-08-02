package com.espressif.iot.action.device.common.upgrade;

import org.apache.http.HttpStatus;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;

import com.espressif.iot.base.api.EspBaseApiUtil;
import com.espressif.iot.base.application.EspApplication;
import com.espressif.iot.device.IEspDevice;
import com.espressif.iot.device.builder.BEspDevice;
import com.espressif.iot.type.device.state.EspDeviceState;
import com.espressif.iot.type.net.HeaderPair;
import com.espressif.iot.user.builder.BEspUser;
import com.espressif.iot.util.BSSIDUtil;
import com.espressif.iot.util.EspStrings;
import com.espressif.iot.util.MeshUtil;

public class EspActionDeviceUpgradeOnline implements IEspActionDeviceUpgradeOnline
{
    private final static Logger log = Logger.getLogger(EspActionDeviceUpgradeOnline.class);
    
    private static boolean __isHttpsSupported()
    {
        SharedPreferences sp =
            EspApplication.sharedInstance().getSharedPreferences(EspStrings.Key.SYSTEM_CONFIG, Context.MODE_PRIVATE);
        return sp.getBoolean(EspStrings.Key.HTTPS_SUPPORT, true);
    }
    
    /**
     * get current device from server
     * 
     * @param deviceKey the device's key
     * @return the IEspDevice or null
     */
    private IEspDevice __getCurrentDevice(String deviceKey)
    {
        String headerKey = "Authorization";
        String headerValue = "token " + deviceKey;
        HeaderPair header = new HeaderPair(headerKey, headerValue);
        String url = URL_GET_DEVICE;
        if (__isHttpsSupported())
        {
            url = url.replace("https", "http");
        }
        JSONObject jsonObjectResult = EspBaseApiUtil.Get(url, header);
        int status = -1;
        try
        {
            if (jsonObjectResult != null)
                status = Integer.parseInt(jsonObjectResult.getString("status"));
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        if (HttpStatus.SC_OK == status)
        {
            log.debug("getDevice() ok");
            try
            {
                // JSONObject key = jsonObjectResult.getJSONObject("key");
                String token = deviceKey;
                long userId = BEspUser.getBuilder().getInstance().getUserId();
                boolean is_owner_key = false;
                // if (Integer.parseInt(key.getString("is_owner_key")) == 1)
                is_owner_key = true;
                // deviceJson
                JSONObject deviceJson = jsonObjectResult.getJSONObject("device");
                long id = deviceJson.getLong("id");
                String BSSID = deviceJson.getString("bssid");
                String deviceName = deviceJson.getString("name");
                // filter "device-name-"
                if (deviceName.length() > "device-name-".length())
                {
                    String deviceNamePre = deviceName.substring(0, "device-name-".length());
                    if (deviceNamePre.equals("device-name-"))
                    {
                        deviceName = BSSIDUtil.genDeviceNameByBSSID(BSSID);
                    }
                }
                int ptype = deviceJson.getInt("ptype");
                String rom_version = deviceJson.getString("rom_version");
                String latest_rom_version = deviceJson.getString("latest_rom_version");
                EspDeviceState state = new EspDeviceState();
                state.addStateInternet();
                IEspDevice device =
                    BEspDevice.getInstance().alloc(deviceName,
                        id,
                        token,
                        is_owner_key,
                        BSSID,
                        state.getStateValue(),
                        ptype,
                        rom_version,
                        latest_rom_version,
                        userId);
                boolean isParentMdevMacValid = !deviceJson.isNull(Parent_Mdev_Mac);
                String parentBssid = null;
                if (isParentMdevMacValid)
                {
                    // parent device bssid
                    String parentDeviceBssid = deviceJson.getString(Parent_Mdev_Mac);
                    if (!parentDeviceBssid.equals("null"))
                    {
                        parentBssid = MeshUtil.getRawMacAddress(parentDeviceBssid);
                    }
                }
                // synchronize parent device bssid, filter the AP
                if (parentBssid != null && BSSIDUtil.isEspDevice(parentBssid))
                {
                    device.setParentDeviceBssid(parentBssid);
                }
                else
                {
                    device.setParentDeviceBssid(null);
                }
                device.setIsMeshDevice(parentBssid != null);
                // device payload
                JSONObject devicePayload = deviceJson.optJSONObject("payload");
                if (devicePayload != null)
                {
                    String deviceInfo = devicePayload.optString("info", null);
                    int deviceRssi = devicePayload.optInt("rssi", IEspDevice.RSSI_NULL);
                    device.setInfo(deviceInfo);
                    device.setRssi(deviceRssi);
                }
                log.debug(Thread.currentThread().toString() + "##__getCurrentDevice(deviceKey=[" + deviceKey + "]): "
                    + device);
                return device;
            }
            catch (JSONException e)
            {
                e.printStackTrace();
            }
        }
        log.debug(Thread.currentThread().toString() + "##__getCurrentDevice(deviceKey=[" + deviceKey + "]): " + null);
        return null;
    }
    
    /**
     * get the current device, and check whether its romVersion==LatestRomVersion which indicate the upgrade is finished
     * 
     * @param deviceKey the device key
     * @return upgrade finished device or null
     */
    private IEspDevice __checkDeviceUpgradeOnlineSuc(String deviceKey)
    {
        IEspDevice device = __getCurrentDevice(deviceKey);
        if (device != null)
        {
            String romVersion = device.getRom_version();
            String latestRomVersion = device.getLatest_rom_version();
            if (romVersion != null && latestRomVersion != null && romVersion.equals(latestRomVersion))
            {
                return device;
            }
            else
            {
                return null;
            }
        }
        else
        {
            return null;
        }
    }
    
    private IEspDevice checkDeviceUpgradeOnlineSuc(String deviceKey)
    {
        IEspDevice device = __checkDeviceUpgradeOnlineSuc(deviceKey);
        long startTime = System.currentTimeMillis();
        long currentTime = startTime;
        while (device == null)
        {
            try
            {
                log.debug(Thread.currentThread().toString() + "##checkDeviceUpgradeOnlineSuc(deviceKey=[" + deviceKey
                    + "]): sleep " + RETRY_TIME_MILLISECONDS + " milliseconds");
                Thread.sleep(RETRY_TIME_MILLISECONDS);
            }
            catch (InterruptedException e)
            {
                return null;
            }
            device = __checkDeviceUpgradeOnlineSuc(deviceKey);
            currentTime = System.currentTimeMillis();
            if (currentTime - startTime > TIMEOUT_MILLISECONDS)
            {
                log.debug(Thread.currentThread().toString() + "##checkDeviceUpgradeOnlineSuc(deviceKey=[" + deviceKey
                    + "]): timeout return null");
                return null;
            }
        }
        return device;
    }
    
    private boolean deviceUpgradeOnline(String deviceKey, String latestRomVersion)
    {
        String headerKey = "Authorization";
        String headerValue = "token " + deviceKey;
        HeaderPair header = new HeaderPair(headerKey, headerValue);
        String url = URL_UPGRADE_ONLINE + "&version=" + latestRomVersion;
        if (__isHttpsSupported())
        {
            url = url.replace("https", "http");
        }
        JSONObject result = EspBaseApiUtil.Get(url, header);
        int status = -1;
        try
        {
            if (result != null)
                status = Integer.parseInt(result.getString("status"));
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        if (HttpStatus.SC_OK == status)
        {
            log.debug(Thread.currentThread().toString() + "##deviceUpgradeOnline(deviceKey=[" + deviceKey
                + "],latestRomVersion=[" + latestRomVersion + "]): " + "true");
            return true;
        }
        else
        {
            log.debug(Thread.currentThread().toString() + "##deviceUpgradeOnline(deviceKey=[" + deviceKey
                + "],latestRomVersion=[" + latestRomVersion + "]): " + "false");
            return false;
        }
    }
    
    private void rebootDeviceOnline(String deviceKey)
    {
        String headerKey = "Authorization";
        String headerValue = "token " + deviceKey;
        HeaderPair header = new HeaderPair(headerKey, headerValue);
        String url = URL_REBOOT_DEVICE;
        if (!__isHttpsSupported())
        {
            url = url.replace("https", "http");
        }
        
        JSONObject result = EspBaseApiUtil.Get(URL_REBOOT_DEVICE, header);
        int status = -1;
        try
        {
            if (result != null)
                status = Integer.parseInt(result.getString("status"));
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        if (HttpStatus.SC_OK == status)
        {
            log.info(Thread.currentThread().toString() + "##deviceRebootOnline(deviceKey=[" + deviceKey + "]) ok");
            // return true;
        }
        else
        {
            log.warn(Thread.currentThread().toString() + "##deviceRebootOnline(deviceKey=[" + deviceKey + "]) fail");
        }
        // return false;
    }
    
    @Override
    public IEspDevice doUpgradeOnline(String deviceKey, String latestRomVersion)
    {
        boolean isUpgradeSuc = deviceUpgradeOnline(deviceKey, latestRomVersion);
        if (isUpgradeSuc)
        {
            IEspDevice device = checkDeviceUpgradeOnlineSuc(deviceKey);
            if (device != null)
            {
                rebootDeviceOnline(deviceKey);
                return device;
            }
            else
            {
                return null;
            }
        }
        else
        {
            return null;
        }
    }
    
}
