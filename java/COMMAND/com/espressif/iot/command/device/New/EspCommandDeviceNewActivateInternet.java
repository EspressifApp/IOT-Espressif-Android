package com.espressif.iot.command.device.New;

import org.apache.http.HttpStatus;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.espressif.iot.base.api.EspBaseApiUtil;
import com.espressif.iot.device.IEspDevice;
import com.espressif.iot.device.builder.BEspDevice;
import com.espressif.iot.type.device.EspDeviceType;
import com.espressif.iot.type.device.state.EspDeviceState;
import com.espressif.iot.type.net.HeaderPair;
import com.espressif.iot.util.BSSIDUtil;
import com.espressif.iot.util.MeshUtil;
import com.espressif.iot.util.TimeUtil;

public class EspCommandDeviceNewActivateInternet implements IEspCommandDeviceNewActivateInternet
{
    private final static Logger log = Logger.getLogger(EspCommandDeviceNewActivateInternet.class);
    
    private static final long ONLINE_TIMEOUT_PLUG = 60 * TimeUtil.ONE_SECOND_LONG_VALUE;
    
    private static final long ONLINE_TIMEOUT_LIGHT = 60 * TimeUtil.ONE_SECOND_LONG_VALUE;
    
    private static final long ONLINE_TIMEOUT_TEMPERATURE = 5 * TimeUtil.ONE_MINUTE_LONG_VALUE;
    
    private static final long ONLINE_TIMEOUT_GAS_SIREN = 5 * TimeUtil.ONE_MINUTE_LONG_VALUE;
    
    private static final long ONLINE_TIMEOUT_REMOTE = 60 * TimeUtil.ONE_SECOND_LONG_VALUE;
    
    private static final long ONLINE_TIMEOUT_PLUGS = 60 * TimeUtil.ONE_SECOND_LONG_VALUE;
    
    private static final long ONLINE_TIMEOUT_VOLTAGE = 60 * TimeUtil.ONE_SECOND_LONG_VALUE;
    
    private static final long ONLINE_TIMEOUT_SOUNDBOX = TimeUtil.ONE_MINUTE_LONG_VALUE;
    
    private boolean isDeviceOnline(EspDeviceType deviceType, long last_active, long currentTime)
    {
        long timeout = 0;
        switch (deviceType)
        {
            case FLAMMABLE:
                timeout = ONLINE_TIMEOUT_GAS_SIREN;
                break;
            case HUMITURE:
                timeout = ONLINE_TIMEOUT_TEMPERATURE;
                break;
            case LIGHT:
                timeout = ONLINE_TIMEOUT_LIGHT;
                break;
            case VOLTAGE:
                timeout = ONLINE_TIMEOUT_VOLTAGE;
                break;
            case PLUG:
                timeout = ONLINE_TIMEOUT_PLUG;
                break;
            case REMOTE:
                timeout = ONLINE_TIMEOUT_REMOTE;
                break;
            case PLUGS:
                timeout = ONLINE_TIMEOUT_PLUGS;
                break;
            case SOUNDBOX:
                timeout = ONLINE_TIMEOUT_SOUNDBOX;
                break;
            case NEW:
                break;
            case ROOT:
                break;
        }
        /**
         * when last_active is after currentTime or currentTime - last_active <= timeout, the device is online
         */
        if (last_active >= currentTime || currentTime - last_active <= timeout)
        {
            return true;
        }
        else
        {
            return false;
        }
    }
    
    @Override
    public IEspDevice doCommandNewActivateInternet(long userId, String userKey, String randomToken)
    {
        
        JSONObject jsonObject = new JSONObject();
        try
        {
            jsonObject.put(Token, randomToken);
            HeaderPair header = new HeaderPair(Authorization, Token + " " + userKey);
            HeaderPair header2 = new HeaderPair(Time_Zone, Epoch);
            long start = System.currentTimeMillis();
            JSONObject jsonObjectResult = EspBaseApiUtil.Post(URL, jsonObject, header, header2);
            long consume = System.currentTimeMillis() - start;
            log.error("consume: " + consume);
            if (jsonObjectResult == null)
            {
                return null;
            }
            int status = jsonObjectResult.getInt(Status);
            if (status == HttpStatus.SC_OK)
            {
                // get current UTC time
                long currentTime = EspBaseApiUtil.getUTCTimeLong();
                // usually when the device could be activated suc, it won't get Long.MIN_VALUE,
                // check here just to prevent the exception happened
                if (currentTime == Long.MIN_VALUE)
                {
                    currentTime = System.currentTimeMillis();
                }
                JSONObject key = jsonObjectResult.getJSONObject(Key);
                String token = key.getString(Token);
                boolean is_owner_key = false;
                if (Integer.parseInt(key.getString(IsOwnerKey)) == 1)
                    is_owner_key = true;
                long id = Long.parseLong(key.getString("device_id"));
                // deviceJson
                JSONObject deviceJson = jsonObjectResult.getJSONObject(Device);
                String BSSID = deviceJson.getString(Bssid);
                String name = deviceJson.getString(Name);
                
                int ptype = deviceJson.getInt(Ptype);
                String rom_version = deviceJson.getString(Rom_Version);
                String latest_rom_version = deviceJson.getString(Latest_Rom_Version);
                // check whether the device is online or offline
                long last_active = deviceJson.getLong("last_active") * TimeUtil.ONE_SECOND_LONG_VALUE;
                boolean isOnline =
                    isDeviceOnline(EspDeviceType.getEspTypeEnumBySerial(ptype), last_active, currentTime);
                EspDeviceState state = new EspDeviceState();
                if (isOnline)
                {
                    state.addStateInternet();
                }
                else
                {
                    state.addStateOffline();
                }
                // filter "device-name-"
                if (name.length() > "device-name-".length())
                {
                    String deviceNamePre = name.substring(0, "device-name-".length());
                    if (deviceNamePre.equals("device-name-"))
                    {
                        name = BSSIDUtil.genDeviceNameByBSSID(BSSID);
                    }
                }
                IEspDevice device =
                    BEspDevice.getInstance().alloc(name,
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
                if(isParentMdevMacValid)
                {
                    // parent device bssid
                    String parentDeviceBssid = deviceJson.getString(Parent_Mdev_Mac);
                    if(!parentDeviceBssid.equals("null"))
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
                JSONObject devicePayload = deviceJson.optJSONObject(Payload);
                if (devicePayload != null)
                {
                    String deviceInfo = devicePayload.optString(Info, null);
                    int deviceRssi = devicePayload.optInt(Rssi, IEspDevice.RSSI_NULL);
                    device.setInfo(deviceInfo);
                    device.setRssi(deviceRssi);
                }

                log.debug(Thread.currentThread().toString() + "##doCommandNewActivateInternet(userId=[" + userId
                    + "],userKey=[" + userKey + "],randomToken=[" + randomToken + "]): " + device);
                return device;
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        log.warn(Thread.currentThread().toString() + "##doCommandNewActivateInternet(userId=[" + userId + "],userKey=["
            + userKey + "],randomToken=[" + randomToken + "]): " + null);
        return null;
    }
    
}
