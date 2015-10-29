package com.espressif.iot.device.cache;

import java.util.List;

import com.espressif.iot.device.IEspDevice;
import com.espressif.iot.type.net.IOTAddress;

/**
 * the cache to store devices to be processed by others
 * 
 * onhand: indicate that the device require handling, which is used by @see IEspDeviceCacheHandler. worked: indicate
 * that the device has been handled. which is used by @see IUser to diplay on UI
 * 
 * @author afunx
 * 
 */
public interface IEspDeviceCache
{
    static enum NotifyType
    {
        PULL_REFRESH, STATE_MACHINE_BACKSTATE, STATE_MACHINE_UI
    }

    void clear();
    
    boolean addTransformedDeviceCache(IEspDevice device);
    
    boolean addTransformedDeviceCacheList(List<IEspDevice> devieList);

    List<IEspDevice> pollTransformedDeviceCacheList();
    
    boolean addServerLocalDeviceCache(IEspDevice device);
    
    boolean addServerLocalDeviceCacheList(List<IEspDevice> deviceList);

    List<IEspDevice> pollServerLocalDeviceCacheList();
    
    boolean addLocalDeviceCacheList(List<IOTAddress> deviceIOTAddressList);
    
    List<IOTAddress> pollLocalDeviceCacheList();
    
    boolean addStatemahchineDeviceCache(IEspDevice device);
    
    IEspDevice pollStatemachineDeviceCache();
    
    boolean addSharedDeviceCache(IEspDevice device);
    
    IEspDevice pollSharedDeviceCache();
    
    boolean addUpgradeSucLocalDeviceCacheList(List<IOTAddress> deviceIOTAddressList);
    
    List<IOTAddress> pollUpgradeSucLocalDeviceCacheList();
    
    boolean addStaDeviceCache(IOTAddress deviceStaDevice);
    
    boolean addStaDeviceCacheList(List<IOTAddress> deviceStaDeviceList);

    List<IOTAddress> pollStaDeviceCacheList();
    /**
     * notify the user that device cache has been changed
     */
    void notifyIUser(NotifyType type);
}
