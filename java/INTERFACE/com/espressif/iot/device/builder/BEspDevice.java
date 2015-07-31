package com.espressif.iot.device.builder;

import com.espressif.iot.device.IEspDevice;
import com.espressif.iot.device.IEspDeviceSSS;
import com.espressif.iot.model.device.EspDeviceSSS;
import com.espressif.iot.object.db.IDeviceDB;
import com.espressif.iot.type.device.EspDeviceType;
import com.espressif.iot.type.device.IEspDeviceState;
import com.espressif.iot.type.device.state.EspDeviceState;
import com.espressif.iot.type.net.IOTAddress;

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
        return alloc(deviceName,
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
    }
    
    public static IEspDeviceSSS createSSSDevice(IOTAddress iotAddress)
    {
        EspDeviceSSS device = new EspDeviceSSS(iotAddress);
        
        return device;
    }
    
    public static IEspDevice convertSSSToTypeDevice(IEspDeviceSSS deviceSSS)
    {
        IEspDevice device = null;
        switch (deviceSSS.getDeviceType())
        {
            case LIGHT:
                device = BEspDeviceLight.getInstance().alloc();
                break;
            case PLUG:
                device = BEspDevicePlug.getInstance().alloc();
                break;
            case FLAMMABLE:
                break;
            case HUMITURE:
                break;
            case NEW:
                break;
            case PLUGS:
                device = BEspDevicePlugs.getInstance().alloc();
                break;
            case REMOTE:
                device = BEspDeviceRemote.getInstance().alloc();
                break;
            case ROOT:
                break;
            case VOLTAGE:
                break;
        }
        
        if (device != null)
        {
            device.setId(deviceSSS.getId());
            device.setKey(deviceSSS.getKey());
            device.setIsOwner(false);
            device.setBssid(deviceSSS.getBssid());
            device.setDeviceState(deviceSSS.getDeviceState());
            device.setDeviceType(deviceSSS.getDeviceType());
            device.setName(deviceSSS.getName());
            device.setInetAddress(deviceSSS.getInetAddress());
            device.setRouter(deviceSSS.getRouter());
            device.setRootDeviceBssid(deviceSSS.getRootDeviceBssid());
            device.setIsMeshDevice(deviceSSS.getIsMeshDevice());
        }
        
        return device;
    }
}
