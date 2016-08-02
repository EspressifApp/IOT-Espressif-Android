package com.espressif.iot.device.builder;

import com.espressif.iot.user.IEspUser;
import com.espressif.iot.device.IEspDevice;
import com.espressif.iot.device.IEspDeviceConfigure;
import com.espressif.iot.device.array.IEspDeviceArray;
import com.espressif.iot.model.device.array.EspDeviceLightArray;
import com.espressif.iot.model.device.array.EspDevicePlugArray;
import com.espressif.iot.object.db.IDeviceDB;
import com.espressif.iot.type.device.EspDeviceType;
import com.espressif.iot.type.device.IEspDeviceState;
import com.espressif.iot.type.device.state.EspDeviceState;
import com.espressif.iot.type.net.IOTAddress;
import com.espressif.iot.util.RandomUtil;

public class BEspDevice implements IBEspDevice
{
    /*
     * Singleton lazy initialization start
     */
    private BEspDevice()
    {
    }
    
    private static class InstanceHolder
    {
        static BEspDevice instance = new BEspDevice();
    }
    
    public static BEspDevice getInstance()
    {
        return InstanceHolder.instance;
    }
    
    /*
     * Singleton lazy initialization end
     */
    
    @Override
    public IEspDevice alloc(String deviceName, long deviceId, String deviceKey, boolean isOwner, String bssid,
        int state, int ptype, String rom_version, String latest_rom_version, long userId, long... timestamp)
    {
        IEspDevice device = null;
        IEspDeviceState deviceState = new EspDeviceState(state);
        EspDeviceType deviceType = EspDeviceType.getEspTypeEnumBySerial(ptype);
        switch (deviceType)
        {
            case NEW:
                // if we get device from local, we don't care the Activating device's ssid and wifiCipher type
                device = BEspDeviceNew.getInstance().alloc(null, bssid, null, 0);
                break;
            case PLUG:
                device = BEspDevicePlug.getInstance().alloc();
                break;
            case LIGHT:
                device = BEspDeviceLight.getInstance().alloc();
                break;
            case FLAMMABLE:
                device = BEspDeviceFlammable.getInstance().alloc();
                break;
            case HUMITURE:
                device = BEspDeviceHumiture.getInstance().alloc();
                break;
            case VOLTAGE:
                device = BEspDeviceVoltage.getInstance().alloc();
                break;
            case REMOTE:
                device = BEspDeviceRemote.getInstance().alloc();
                break;
            case PLUGS:
                device = BEspDevicePlugs.getInstance().alloc();
                break;
            case SOUNDBOX:
                device = BEspDeviceSoundbox.getInstance().alloc();
                break;
            case ROOT:
                throw new IllegalArgumentException("Not support alloc ROOT device");
        }
        device.setId(deviceId);
        device.setKey(deviceKey);
        device.setIsOwner(isOwner);
        device.setBssid(bssid);
        device.setDeviceState(deviceState);
        device.setDeviceType(deviceType);
        device.setRom_version(rom_version);
        device.setLatest_rom_version(latest_rom_version);
        device.setUserId(userId);
        device.setName(deviceName);
        if (timestamp.length != 0)
        {
            device.setTimestamp(timestamp[0]);
        }
        return device;
    }
    
    @Override
    public IEspDevice alloc(IDeviceDB deviceDB)
    {
        String deviceName = deviceDB.getName();
        long deviceId = deviceDB.getId();
        String deviceKey = deviceDB.getKey();
        boolean isOwner = deviceDB.getIsOwner();
        String bssid = deviceDB.getBssid();
        int state = deviceDB.getState();
        int ptype = deviceDB.getType();
        String rom_version = deviceDB.getRom_version();
        String latest_rom_version = deviceDB.getLatest_rom_version();
        long userId = deviceDB.getUserId();
        long timestamp = deviceDB.getTimestamp();
        long activatedTime = deviceDB.getActivatedTime();
        IEspDevice device =
            alloc(deviceName,
                deviceId,
                deviceKey,
                isOwner,
                bssid,
                state,
                ptype,
                rom_version,
                latest_rom_version,
                userId,
                timestamp);
        device.setActivatedTime(activatedTime);
        
        return device;
    }
    
    public static IEspDeviceArray createDeviceArray(EspDeviceType deviceType)
    {
        switch(deviceType)
        {
            case LIGHT:
                EspDeviceLightArray lightArray = new EspDeviceLightArray();
                lightArray.setName(deviceType.toString());
                lightArray.setDeviceType(deviceType);
                lightArray.setKey(RandomUtil.randomString(41));
                lightArray.setIsMeshDevice(false);
                IEspDeviceState lightState = new EspDeviceState();
                lightState.addStateInternet();
                lightState.addStateLocal();
                lightArray.setDeviceState(lightState);
                return lightArray;
            case PLUG:
                EspDevicePlugArray plugArray = new EspDevicePlugArray();
                plugArray.setName(deviceType.toString());
                plugArray.setDeviceType(deviceType);
                plugArray.setKey(RandomUtil.randomString(41));
                plugArray.setIsMeshDevice(false);
                IEspDeviceState plugState = new EspDeviceState();
                plugState.addStateInternet();
                plugState.addStateLocal();
                plugArray.setDeviceState(plugState);
                return plugArray;
                
            case FLAMMABLE:
            case HUMITURE:
            case NEW:
            case PLUGS:
            case REMOTE:
            case ROOT:
            case VOLTAGE:
            case SOUNDBOX:
                break;
        }
        
        return null;
    }

    // -1, -2, ... is used to activate softap device by direct connect,
    // 1, 2, ... is used by server
    private static long mIdCreator = -Long.MAX_VALUE / 2;

    private synchronized long getNextId() {
        return --mIdCreator;
    }

    public IEspDevice createStaDevice(IOTAddress iotAddress) {
        IEspDevice device = null;
        switch (iotAddress.getDeviceTypeEnum()) {
            case LIGHT:
                device = BEspDeviceLight.getInstance().alloc();
                break;
            case PLUG:
                device = BEspDevicePlug.getInstance().alloc();
                break;
            case PLUGS:
                device = BEspDevicePlugs.getInstance().alloc();
                break;
            case REMOTE:
                device = BEspDeviceRemote.getInstance().alloc();
                break;
            case SOUNDBOX:
                device = BEspDeviceSoundbox.getInstance().alloc();
                break;
            case FLAMMABLE:
                device = BEspDeviceFlammable.getInstance().alloc();
                break;
            case HUMITURE:
                device = BEspDeviceHumiture.getInstance().alloc();
                break;
            case VOLTAGE:
                device = BEspDeviceVoltage.getInstance().alloc();
                break;
            case NEW:
                device = BEspDeviceNew.getInstance().alloc(null, iotAddress.getBSSID(), null, 0);
                break;
            case ROOT:
                break;
        }

        if (device != null) {
            EspDeviceState stateLocal = new EspDeviceState();
            stateLocal.addStateLocal();
            device.setDeviceState(stateLocal);
            device.setName(iotAddress.getSSID());
            device.setBssid(iotAddress.getBSSID());
            device.setInetAddress(iotAddress.getInetAddress());
            device.setDeviceType(iotAddress.getDeviceTypeEnum());
            device.setParentDeviceBssid(iotAddress.getParentBssid());
            device.setIsMeshDevice(iotAddress.isMeshDevice());
            device.setKey(iotAddress.getBSSID());
            device.setId(getNextId());
            device.setIsOwner(false);
            device.setRom_version(iotAddress.getRomVersion());
            device.setRssi(iotAddress.getRssi());
            device.setInfo(iotAddress.getInfo());
            device.setUserId(IEspUser.GUEST_USER_ID);
        }

        return device;
    }
    
    public IEspDeviceConfigure createConfiguringDevice(IEspDevice staDevice, String random40) {
        IEspDeviceConfigure device = BEspDeviceConfigure.getInstance().alloc(staDevice.getBssid(), random40);
        device.setInetAddress(staDevice.getInetAddress());
        device.setIsMeshDevice(staDevice.getIsMeshDevice());
        device.setParentDeviceBssid(staDevice.getParentDeviceBssid());
        device.setName(staDevice.getName());
        return device;
    }
}
