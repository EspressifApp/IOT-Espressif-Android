package com.espressif.iot.command.device.common;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpStatus;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.espressif.iot.base.api.EspBaseApiUtil;
import com.espressif.iot.command.group.IEspCommandGroup;
import com.espressif.iot.device.IEspDevice;
import com.espressif.iot.device.builder.BEspDevice;
import com.espressif.iot.group.IEspGroup;
import com.espressif.iot.group.builder.BEspGroup;
import com.espressif.iot.type.device.EspDeviceType;
import com.espressif.iot.type.device.state.EspDeviceState;
import com.espressif.iot.type.net.HeaderPair;
import com.espressif.iot.user.builder.BEspUser;
import com.espressif.iot.util.BSSIDUtil;
import com.espressif.iot.util.MeshUtil;
import com.espressif.iot.util.TimeUtil;

import android.text.TextUtils;

public class EspCommandDeviceSynchronizeInternet implements IEspCommandDeviceSynchronizeInternet
{
    private final static Logger log = Logger.getLogger(EspCommandDeviceSynchronizeInternet.class);
    
    private static final long ONLINE_TIMEOUT_PLUG = 60 * TimeUtil.ONE_SECOND_LONG_VALUE;
    private static final long ONLINE_TIMEOUT_PLUG_MESH = 3 * 60 * TimeUtil.ONE_SECOND_LONG_VALUE;
    
    private static final long ONLINE_TIMEOUT_LIGHT = 60 * TimeUtil.ONE_SECOND_LONG_VALUE;
    private static final long ONLINE_TIMEOUT_LIGHT_MESH = 3 * 60 * TimeUtil.ONE_SECOND_LONG_VALUE;
    
    private static final long ONLINE_TIMEOUT_TEMPERATURE = 5 * TimeUtil.ONE_MINUTE_LONG_VALUE;
    
    private static final long ONLINE_TIMEOUT_GAS_SIREN = 5 * TimeUtil.ONE_MINUTE_LONG_VALUE;
    
    private static final long ONLINE_TIMEOUT_REMOTE = 60 * TimeUtil.ONE_SECOND_LONG_VALUE;
    private static final long ONLINE_TIMEOUT_REMOTE_MESH = 3 * 60 * TimeUtil.ONE_SECOND_LONG_VALUE;
    
    private static final long ONLINE_TIMEOUT_PLUGS = 60 * TimeUtil.ONE_SECOND_LONG_VALUE;
    private static final long ONLINE_TIMEOUT_PLUGS_MESH = 3 * 60 * TimeUtil.ONE_SECOND_LONG_VALUE;
    
    private static final long ONLINE_TIMEOUT_VOLTAGE = 60 * TimeUtil.ONE_SECOND_LONG_VALUE;
    private static final long ONLINE_TIMEOUT_VOLTAGE_MESH = 3 * 60 * TimeUtil.ONE_SECOND_LONG_VALUE;
    
    private static final long ONLINE_TIME_SOUNDBOX = TimeUtil.ONE_MINUTE_LONG_VALUE;
    private static final long ONLINE_TIME_SOUNDBOX_MESH = 3 * TimeUtil.ONE_MINUTE_LONG_VALUE;
    
    private JSONArray getJSONArrayGroups(String userKey)
    {
        HeaderPair headerAuthorization = new HeaderPair(Authorization, Token + " " + userKey);
        HeaderPair headerTimeZone = new HeaderPair(Time_Zone, Epoch);
        JSONObject jsonObjectResult = EspBaseApiUtil.Get(URL, headerAuthorization, headerTimeZone);
        if (jsonObjectResult != null)
        {
            try
            {
                int status = jsonObjectResult.getInt(Status);
                if (status == HttpStatus.SC_OK)
                {
                    log.debug("getJSONArrayDeviceInfo() suc");
                    JSONArray jsonArray = jsonObjectResult.getJSONArray(Device_Groups);
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
    
    private boolean isDeviceOnline(boolean isMeshDevice, EspDeviceType deviceType, long last_active, long currentTime)
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
                if (isMeshDevice)
                {
                    timeout = ONLINE_TIMEOUT_VOLTAGE_MESH;
                }
                else
                {
                    timeout = ONLINE_TIMEOUT_VOLTAGE;
                }
                break;
            case LIGHT:
                if (isMeshDevice)
                {
                    timeout = ONLINE_TIMEOUT_LIGHT_MESH;
                }
                else
                {
                    timeout = ONLINE_TIMEOUT_LIGHT;
                }
                break;
            case PLUG:
                if (isMeshDevice)
                {
                    timeout = ONLINE_TIMEOUT_PLUG_MESH;
                }
                else
                {
                    timeout = ONLINE_TIMEOUT_PLUG;
                }
                break;
            case REMOTE:
                if (isMeshDevice)
                {
                    timeout = ONLINE_TIMEOUT_REMOTE_MESH;
                }
                else
                {
                    timeout = ONLINE_TIMEOUT_REMOTE;
                }
                break;
            case PLUGS:
                if (isMeshDevice)
                {
                    timeout = ONLINE_TIMEOUT_PLUGS_MESH;
                }
                else
                {
                    timeout = ONLINE_TIMEOUT_PLUGS;
                }
                break;
            case SOUNDBOX:
                if (isMeshDevice) {
                    timeout = ONLINE_TIME_SOUNDBOX_MESH;
                } else {
                    timeout = ONLINE_TIME_SOUNDBOX;
                }
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
    
    private List<IEspGroup> setUserServerSyncGroup(JSONArray groupsJsonArray, long currentTime)
    {
        List<IEspGroup> groupList = new ArrayList<IEspGroup>();
        List<IEspDevice> deviceList = new ArrayList<IEspDevice>();
        
        for (int gi = 0; gi < groupsJsonArray.length(); gi++)
        {
            try
            {
                JSONObject groupJSON = groupsJsonArray.getJSONObject(gi);
                
                IEspGroup espGroup = BEspGroup.getInstance().alloc();
                // Set group id
                long groupId = groupJSON.getLong(Id);
                espGroup.setId(groupId);
                // Set group name
                String groupName = groupJSON.getString(Name);
                espGroup.setName(groupName);
                // Parser group desc
                String desc = groupJSON.getString(IEspCommandGroup.KEY_GROUP_DESC);
                if (!TextUtils.isEmpty(desc)) {
                    try {
                        JSONObject descJSON = new JSONObject(desc);
                        int groupType = descJSON.optInt(IEspCommandGroup.KEY_GROUP_TYPE);
                        espGroup.setType(groupType);
                    } catch (JSONException je) {
                        je.printStackTrace();
                    }
                }
                groupList.add(espGroup);
                
                JSONArray devicesJsonArray = groupJSON.getJSONArray(Devices);
                for (int i = 0; i < devicesJsonArray.length(); i++)
                {
                    JSONObject deviceJsonObject = devicesJsonArray.getJSONObject(i);
                    // bssid
                    String bssid = deviceJsonObject.getString(Bssid);
                    if (bssid.equals("") || bssid.equals("12:34:56:78:91"))
                    {
                        continue;
                    }
                    // type
                    int ptype = deviceJsonObject.getInt(Ptype);
                    EspDeviceType deviceType = EspDeviceType.getEspTypeEnumBySerial(ptype);
                    if (deviceType == null)
                    {
                        // invalid ptype
                        continue;
                    }
                    
                    // userId
                    long userId = BEspUser.getBuilder().getInstance().getUserId();
                    
                    // deviceId
                    long deviceId = Long.parseLong(deviceJsonObject.getString(Id));
                    log.debug("deviceId=" + deviceId);
                    
                    // device name
                    String deviceName = deviceJsonObject.getString(Name);
                    
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
                    if (Integer.parseInt(keyJsonObject.getString(Is_Owner_Key)) == 1)
                    {
                        isOwner = true;
                    }
                    
                    // deviceKey
                    String deviceKey = keyJsonObject.getString(Token);
                    // rom_version and latest_rom_version
                    String rom_version = deviceJsonObject.getString(Rom_Version);
                    String latest_rom_version = deviceJsonObject.getString(Latest_Rom_Version);
                    // check isOnline
                    boolean isMeshDevice = !deviceJsonObject.isNull(Parent_Mdev_Mac);
                    long last_active = deviceJsonObject.getLong(Last_Active) * TimeUtil.ONE_SECOND_LONG_VALUE;
                    boolean isOnline = isDeviceOnline(isMeshDevice, deviceType, last_active, currentTime);
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
                    
                    long activatedTime = deviceJsonObject.getLong(Activated_At) * TimeUtil.ONE_SECOND_LONG_VALUE;
                    device.setActivatedTime(activatedTime);
                    
                    boolean isParentMdevMacValid = !deviceJsonObject.isNull(Parent_Mdev_Mac);
                    String parentBssid = null;
                    if (isParentMdevMacValid)
                    {
                        // parent device bssid
                        String parentDeviceBssid = deviceJsonObject.getString(Parent_Mdev_Mac);
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
                    JSONObject devicePayload = deviceJsonObject.optJSONObject(Payload);
                    if (devicePayload != null)
                    {
                        String deviceInfo = devicePayload.optString(Info, null);
                        int deviceRssi = devicePayload.optInt(Rssi, IEspDevice.RSSI_NULL);
                        device.setInfo(deviceInfo);
                        device.setRssi(deviceRssi);
                    }

                    espGroup.addDevice(device);
                    if (!deviceList.contains(device))
                    {
                        deviceList.add(device);
                    }
                 
//                    if (!deviceList.contains(device))
//                    {
//                        deviceList.add(device);
//                        espGroup.addDevice(device);
//                    }
//                    else
//                    {
//                        for (IEspDevice deviceInList : deviceList)
//                        {
//                            if (deviceInList.equals(device))
//                            {
//                                espGroup.addDevice(deviceInList);
//                                break;
//                            }
//                        }
//                    }
                }
            }
            catch (JSONException e)
            {
                e.printStackTrace();
                return groupList;
            }
        }
        
        return groupList;
    }
    
    private List<IEspDevice> getDevicesFromGroupList(List<IEspGroup> groupList)
    {
        List<IEspDevice> deviceList = new ArrayList<IEspDevice>();
        for (IEspGroup group : groupList)
        {
            List<IEspDevice> deviceListInGroup = group.getDeviceList();
            for (IEspDevice deviceInList : deviceListInGroup)
            {
                if (!deviceList.contains(deviceInList))
                {
                    deviceList.add(deviceInList);
                }
            }
        }
        return deviceList;
    }
    
    @Override
    public List<IEspDevice> doCommandDeviceSynchronizeInternet(String userKey)
    {
        List<IEspGroup> groupList = doCommandGroupSynchronizeInternet(userKey);
        if (groupList == null)
        {
            return null;
        }
        else
        {
            return getDevicesFromGroupList(groupList);
        }
    }

    @Override
    public List<IEspGroup> doCommandGroupSynchronizeInternet(String userKey)
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
            return setUserServerSyncGroup(groupsJsonArray, currentTime);
        }
    }
    
}
