package com.espressif.iot.command.device.common;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpStatus;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.espressif.iot.base.api.EspBaseApiUtil;
import com.espressif.iot.device.IEspDevice;
import com.espressif.iot.device.builder.BEspDevice;
import com.espressif.iot.type.device.EspDeviceType;
import com.espressif.iot.type.device.state.EspDeviceState;
import com.espressif.iot.type.net.HeaderPair;
import com.espressif.iot.user.builder.BEspUser;
import com.espressif.iot.util.BSSIDUtil;
import com.espressif.iot.util.TimeUtil;

public class EspCommandDeviceSynchronizeInternet implements IEspCommandDeviceSynchronizeInternet
{
    private final static Logger log = Logger.getLogger(EspCommandDeviceSynchronizeInternet.class);
    
    private static final long ONLINE_TIMEOUT_PLUG = 60 * TimeUtil.ONE_SECOND_LONG_VALUE;
    private static final long ONLINE_TIMEOUT_LIGHT = 60 * TimeUtil.ONE_SECOND_LONG_VALUE;
    private static final long ONLINE_TIMEOUT_TEMPERATURE = 5 * TimeUtil.ONE_MINUTE_LONG_VALUE;
    private static final long ONLINE_TIMEOUT_GAS_SIREN = 5 * TimeUtil.ONE_MINUTE_LONG_VALUE;
    private static final long ONLINE_TIMEOUT_REMOTE = 60 * TimeUtil.ONE_SECOND_LONG_VALUE;
    private static final long ONLINE_TIMEOUT_PLUGS = 60 * TimeUtil.ONE_SECOND_LONG_VALUE;
    private static final long ONLINE_TIMEOUT_VOLTAGE = 60 * TimeUtil.ONE_SECOND_LONG_VALUE;
    
    private JSONArray getJSONArrayGroups(String userKey)
    {
        HeaderPair headerAuthorization = new HeaderPair(Authorization, Token + " " + userKey);
        HeaderPair headerTimeZone = new HeaderPair(Time_Zone, Epoch);
        JSONObject jsonObjectResult = EspBaseApiUtil.Get(URL, headerAuthorization, headerTimeZone);
        if (jsonObjectResult != null)
        {
            try
            {
                int status = Integer.parseInt(jsonObjectResult.getString("status"));
                if (status == HttpStatus.SC_OK)
                {
                    log.debug("getJSONArrayDeviceInfo() suc");
                    JSONArray jsonArray = jsonObjectResult.getJSONArray("deviceGroups");
                    return jsonArray;
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        log.warn("getJSONArrayGroups() fail");
        return null;
    }
    
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
            case VOLTAGE:
                timeout = ONLINE_TIMEOUT_VOLTAGE;
                break;
            case LIGHT:
                timeout = ONLINE_TIMEOUT_LIGHT;
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
    
    private List<IEspDevice> setUserServerSyncDeviceAndGroup(JSONArray groupsJsonArray, long currentTime)
    {
        List<IEspDevice> deviceList = new ArrayList<IEspDevice>();
        // List<EspGroup> groupList = new ArrayList<EspGroup>();
        
        for (int gi = 0; gi < groupsJsonArray.length(); gi++)
        {
            try
            {
                JSONObject groupJSON = groupsJsonArray.getJSONObject(gi);
                
                // final long groupId = groupJSON.getLong("id");
                // final String groupName = groupJSON.getString("name");
                // EspGroup espGroup = new EspGroup();
                // espGroup.setID(groupId);
                // espGroup.setName(groupName);
                // espGroup.saveInDB();
                // groupList.add(espGroup);
                
                JSONArray devicesJsonArray = groupJSON.getJSONArray("devices");
                for (int i = 0; i < devicesJsonArray.length(); i++)
                {
                    JSONObject deviceJsonObject = devicesJsonArray.getJSONObject(i);
                    // bssid
                    String bssid = deviceJsonObject.getString("bssid");
                    if (bssid.equals("") || bssid.equals("12:34:56:78:91"))
                    {
                        continue;
                    }
                    // type
                    int ptype = deviceJsonObject.getInt("ptype");
                    EspDeviceType deviceType = EspDeviceType.getEspTypeEnumBySerial(ptype);
                    if(deviceType == null)
                    {
                        // invalid ptype
                        continue;
                    }
                    
                    // userId
                    long userId = BEspUser.getBuilder().getInstance().getUserId();
                    
                    // deviceId
                    long deviceId = Long.parseLong(deviceJsonObject.getString("id"));
                    log.debug("deviceId=" + deviceId);
                    
                    // device name
                    String deviceName = deviceJsonObject.getString(Name);
                    
                    // router
                    String router = deviceJsonObject.getString(Router);
                    
                    // root device id
                    long rootDeviceId = -1;
                    
                    if (router.equals("null"))
                    {
                        router = null;
                    }
                    else
                    {
                        rootDeviceId = deviceJsonObject.getLong(Root_Device_Id);
                    }
                    
                    // filter "device-name-"
                    if (deviceName.length() > "device-name-".length())
                    {
                        String deviceNamePre = deviceName.substring(0, "device-name-".length());
                        if (deviceNamePre.equals("device-name-"))
                        {
                            deviceName = BSSIDUtil.genDeviceNameByBSSID(bssid);
                        }
                    }
                    
                    JSONObject keyJsonObject = deviceJsonObject.getJSONObject(Key);
                    
                    // isOwner
                    boolean isOwner = false;
                    if (Integer.parseInt(keyJsonObject.getString("is_owner_key")) == 1)
                    {
                        isOwner = true;
                    }
                    
                    // deviceKey
                    String deviceKey = keyJsonObject.getString(Token);
                    // rom_version and latest_rom_version
                    String rom_version = deviceJsonObject.getString("rom_version");
                    String latest_rom_version = deviceJsonObject.getString("latest_rom_version");
                    // check isOnline
                    long last_active = deviceJsonObject.getLong("last_active") * TimeUtil.ONE_SECOND_LONG_VALUE;
                    boolean isOnline = isDeviceOnline(deviceType, last_active, currentTime);
                    // set state
                    EspDeviceState deviceState = new EspDeviceState();
                    if (isOnline)
                    {
                        deviceState.addStateInternet();
                    }
                    else
                    {
                        deviceState.addStateOffline();
                    }
                    int state = deviceState.getStateValue();
                    // create device
                    IEspDevice device =
                        BEspDevice.getInstance().alloc(deviceName,
                            deviceId,
                            deviceKey,
                            isOwner,
                            bssid,
                            state,
                            ptype,
                            rom_version,
                            latest_rom_version,
                            userId);
                    
                    // synchronize router info from server
                    device.setRootDeviceId(rootDeviceId);
                    device.setRouter(router);
                    if (router != null)
                    {
                        device.setIsMeshDevice(true);
                    }
                    
                    // device.setGroupId(groupId);
                    
                    if (!deviceList.contains(device))
                    {
                        deviceList.add(device);
                    }

                }
            }
            catch (JSONException e)
            {
                e.printStackTrace();
                return deviceList;
            }
        }
        
        // User.getInstance().setServerSyncGroupList(groupList);
        // User.getInstance().setDeviceSynchronizedList(deviceList);
        return deviceList;
    }
    
    @Override
    public List<IEspDevice> doCommandDeviceSynchronizeInternet(String userKey)
    {
        long currentTime = EspBaseApiUtil.getUTCTimeLong();
        if (currentTime == Long.MIN_VALUE)
        {
            // the Internet is unaccessible
            return null;
        }
        JSONArray groupsJsonArray = getJSONArrayGroups(userKey);
        if (groupsJsonArray == null)
        {
            return null;
        }
        else
        {
            return setUserServerSyncDeviceAndGroup(groupsJsonArray, currentTime);
        }
        
    }
    
}
