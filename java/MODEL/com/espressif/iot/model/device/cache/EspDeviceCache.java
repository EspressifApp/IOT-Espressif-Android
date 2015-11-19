package com.espressif.iot.model.device.cache;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.log4j.Logger;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.espressif.iot.base.application.EspApplication;
import com.espressif.iot.device.IEspDevice;
import com.espressif.iot.device.cache.IEspDeviceCache;
import com.espressif.iot.object.IEspSingletonObject;
import com.espressif.iot.type.net.IOTAddress;
import com.espressif.iot.user.IEspUser;
import com.espressif.iot.user.builder.BEspUser;
import com.espressif.iot.util.EspStrings;

public class EspDeviceCache implements IEspDeviceCache, IEspSingletonObject
{
    private final static Logger log = Logger.getLogger(EspDeviceCache.class);
    
    private Queue<IEspDevice> mTransformedDeviceCacheQueue;
    
    private Queue<IEspDevice> mServerLocalDeviceCacheQueue;
    
    private Queue<IOTAddress> mLocalDeviceIOTAddressQueue;
    
    private Queue<IEspDevice> mStatemachineDeviceCacheQueue;
    
    private Queue<IEspDevice> mSharedDeviceCacheQueue;
    
    private Queue<IOTAddress> mUpgradeSucLocalDeviceCacheQueue;
    
    private Queue<IOTAddress> mStaDeviceIOTAddressQueue;
    
    /*
     * Singleton lazy initialization start
     */
    private EspDeviceCache()
    {
        mTransformedDeviceCacheQueue = new ConcurrentLinkedQueue<IEspDevice>();
        mServerLocalDeviceCacheQueue = new ConcurrentLinkedQueue<IEspDevice>();
        mLocalDeviceIOTAddressQueue = new ConcurrentLinkedQueue<IOTAddress>();
        mStatemachineDeviceCacheQueue = new ConcurrentLinkedQueue<IEspDevice>();
        mSharedDeviceCacheQueue = new ConcurrentLinkedQueue<IEspDevice>();
        mUpgradeSucLocalDeviceCacheQueue = new ConcurrentLinkedQueue<IOTAddress>();
        mStaDeviceIOTAddressQueue = new ConcurrentLinkedQueue<IOTAddress>();
    }
    
    private static class InstanceHolder
    {
        static EspDeviceCache instance = new EspDeviceCache();
    }
    
    public static EspDeviceCache getInstance()
    {
        return InstanceHolder.instance;
    }
    
    /*
     * Singleton lazy initialization end
     */
    

    @Override
    public void clear()
    {
        synchronized (mTransformedDeviceCacheQueue)
        {
            mTransformedDeviceCacheQueue.clear();
        }
        synchronized (mServerLocalDeviceCacheQueue)
        {
            mServerLocalDeviceCacheQueue.clear();
        }
        synchronized (mLocalDeviceIOTAddressQueue)
        {
            mLocalDeviceIOTAddressQueue.clear();
        }
        synchronized (mStatemachineDeviceCacheQueue)
        {
            mStatemachineDeviceCacheQueue.clear();
        }
        synchronized (mSharedDeviceCacheQueue)
        {
            mSharedDeviceCacheQueue.clear();
        }
        synchronized (mUpgradeSucLocalDeviceCacheQueue)
        {
            mUpgradeSucLocalDeviceCacheQueue.clear();
        }
        synchronized (mStaDeviceIOTAddressQueue)
        {
            mStaDeviceIOTAddressQueue.clear();
        }
    }
    
    @Override
    public boolean addTransformedDeviceCache(IEspDevice device)
    {
        boolean result;
        synchronized (mTransformedDeviceCacheQueue)
        {
            result = mTransformedDeviceCacheQueue.add(device);
        }
        log.info(Thread.currentThread().toString() + "##addTransformedDeviceCache(device=[" + device + "]): " + result);
        return result;
    }
    
    @Override
    public boolean addTransformedDeviceCacheList(List<IEspDevice> deviceList)
    {
        boolean result;
        synchronized (mTransformedDeviceCacheQueue)
        {
            result = mTransformedDeviceCacheQueue.addAll(deviceList);
        }
        log.info(Thread.currentThread().toString() + "##addTransformedDeviceCacheList(deviceList=[" + deviceList
            + "]): " + result);
        return result;
    }

    @Override
    public List<IEspDevice> pollTransformedDeviceCacheList()
    {
        List<IEspDevice> result = new ArrayList<IEspDevice>();
        IEspDevice device = null;
        synchronized (mTransformedDeviceCacheQueue)
        {
            device = mTransformedDeviceCacheQueue.poll();
            while (device != null)
            {
                result.add(device);
                device = mTransformedDeviceCacheQueue.poll();
            }
        }
        log.info(Thread.currentThread().toString() + "##pollTransformedDeviceCacheList(): " + result);
        return result;
    }
    
    @Override
    public boolean addServerLocalDeviceCache(IEspDevice device)
    {
        boolean result;
        synchronized (mServerLocalDeviceCacheQueue)
        {
            result = mServerLocalDeviceCacheQueue.add(device);
        }
        log.info(Thread.currentThread().toString() + "##addServerLocalDeviceCache(device=[" + device + "]): " + result);
        return result;
    }
    
    @Override
    public boolean addServerLocalDeviceCacheList(List<IEspDevice> deviceList)
    {
        boolean result;
        synchronized (mServerLocalDeviceCacheQueue)
        {
            result = mServerLocalDeviceCacheQueue.addAll(deviceList);
        }
        log.info(Thread.currentThread().toString() + "##addServerLocalDeviceCacheList(deviceList=[" + deviceList
            + "]): " + result);
        return result;
    }

    @Override
    public List<IEspDevice> pollServerLocalDeviceCacheList()
    {
        List<IEspDevice> result = new ArrayList<IEspDevice>();
        IEspDevice device = null;
        synchronized (mServerLocalDeviceCacheQueue)
        {
            device = mServerLocalDeviceCacheQueue.poll();
            while (device != null)
            {
                result.add(device);
                device = mServerLocalDeviceCacheQueue.poll();
            }
        }
        log.info(Thread.currentThread().toString() + "##pollServerLocalDeviceCacheList(): " + result);
        return result;
    }


    @Override
    public boolean addLocalDeviceCacheList(List<IOTAddress> deviceIOTAddressList)
    {
        boolean result;
        synchronized (mLocalDeviceIOTAddressQueue)
        {
            result = mLocalDeviceIOTAddressQueue.addAll(deviceIOTAddressList);
        }
        log.info(Thread.currentThread().toString() + "##addLocalDeviceCacheList(deviceIOTAddressList=["
            + deviceIOTAddressList + "]): " + result);
        return result;
    }
    
    @Override
    public List<IOTAddress> pollLocalDeviceCacheList()
    {
        List<IOTAddress> result = new ArrayList<IOTAddress>();
        IOTAddress iotAddress = null;
        synchronized (mLocalDeviceIOTAddressQueue)
        {
            iotAddress = mLocalDeviceIOTAddressQueue.poll();
            while (iotAddress != null)
            {
                result.add(iotAddress);
                iotAddress = mLocalDeviceIOTAddressQueue.poll();
            }
        }
        log.info(Thread.currentThread().toString() + "##pollLocalDeviceCacheList(): " + result);
        return result;
    }

    @Override
    public boolean addStatemahchineDeviceCache(IEspDevice device)
    {
        boolean result;
        synchronized (mStatemachineDeviceCacheQueue)
        {
            result = mStatemachineDeviceCacheQueue.add(device);
        }
        log.info(Thread.currentThread().toString() + "##addStatemahchineDeviceCache(device=[" + device + "]): "
            + result);
        return result;
    }
    
    @Override
    public IEspDevice pollStatemachineDeviceCache()
    {
        IEspDevice result = null;
        synchronized (mStatemachineDeviceCacheQueue)
        {
            result = mStatemachineDeviceCacheQueue.poll();
        }
        log.info(Thread.currentThread().toString() + "##pollStatemachineDeviceCache(): " + result);
        return result;
    }
    
    @Override
    public boolean addSharedDeviceCache(IEspDevice device)
    {
        boolean result;
        synchronized (mSharedDeviceCacheQueue)
        {
            result = mSharedDeviceCacheQueue.add(device);
        }
        log.info(Thread.currentThread().toString() + "##addSharedDeviceCache(device=[" + device + "]): " + result);
        return result;
    }
    
    @Override
    public IEspDevice pollSharedDeviceCache()
    {
        IEspDevice result = null;
        synchronized (mSharedDeviceCacheQueue)
        {
            result = mSharedDeviceCacheQueue.poll();
        }
        log.info(Thread.currentThread().toString() + "##pollSharedDeviceQueue(): " + result);
        return result;
    }
    
    @Override
    public boolean addUpgradeSucLocalDeviceCacheList(List<IOTAddress> deviceIOTAddressList)
    {
        boolean result;
        synchronized (mUpgradeSucLocalDeviceCacheQueue)
        {
            result = mUpgradeSucLocalDeviceCacheQueue.addAll(deviceIOTAddressList);
        }
        log.info(Thread.currentThread().toString() + "##addUpgradeSucLocalDeviceCacheList(deviceIOTAddressList=["
            + deviceIOTAddressList + "]): " + result);
        return result;
    }

    @Override
    public List<IOTAddress> pollUpgradeSucLocalDeviceCacheList()
    {
        List<IOTAddress> result = new ArrayList<IOTAddress>();
        IOTAddress iotAddress = null;
        synchronized (mUpgradeSucLocalDeviceCacheQueue)
        {
            iotAddress = mUpgradeSucLocalDeviceCacheQueue.poll();
            while (iotAddress != null)
            {
                result.add(iotAddress);
                iotAddress = mUpgradeSucLocalDeviceCacheQueue.poll();
            }
        }
        log.info(Thread.currentThread().toString() + "##pollUpgradeSucLocalDeviceCacheList(): " + result);
        return result;
    }
    

    @Override
    public boolean addStaDeviceCache(IOTAddress deviceStaDevice)
    {
        boolean result;
        synchronized (mStaDeviceIOTAddressQueue)
        {
            result = mStaDeviceIOTAddressQueue.add(deviceStaDevice);
        }
        log.info(Thread.currentThread().toString() + "##addSharedDeviceCache(deviceStaDevice=[" + deviceStaDevice
            + "]): " + result);
        return result;
    }
    
    @Override
    public boolean addStaDeviceCacheList(List<IOTAddress> deviceStaDeviceList)
    {
        boolean result;
        synchronized (mStaDeviceIOTAddressQueue)
        {
            result = mStaDeviceIOTAddressQueue.addAll(deviceStaDeviceList);
        }
        log.info(Thread.currentThread().toString() + "##devieStaDeviceList(deviceStaDeviceList=[" + deviceStaDeviceList
            + "]): " + result);
        return result;
    }

    @Override
    public List<IOTAddress> pollStaDeviceCacheList()
    {
        List<IOTAddress> result = new ArrayList<IOTAddress>();
        IOTAddress iotAddress = null;
        synchronized (mStaDeviceIOTAddressQueue)
        {
            iotAddress = mStaDeviceIOTAddressQueue.poll();
            while (iotAddress != null)
            {
                result.add(iotAddress);
                iotAddress = mStaDeviceIOTAddressQueue.poll();
            }
        }
        log.info(Thread.currentThread().toString() + "##pollStaDeviceCacheList(): " + result);
        return result;
    }
    
    @Override
    public void notifyIUser(NotifyType type)
    {
        log.info(Thread.currentThread().toString() + "##notifyIUser(NofityType=[" + type + "])");
        Context context = EspApplication.sharedInstance().getApplicationContext();
        LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(context);
        Intent intent = null;
        switch (type)
        {
            case PULL_REFRESH:
                intent = new Intent(EspStrings.Action.DEVICES_ARRIVE_PULLREFRESH);
                broadcastManager.sendBroadcast(intent);
                break;
            case STATE_MACHINE_UI:
                intent = new Intent(EspStrings.Action.DEVICES_ARRIVE_STATEMACHINE);
                broadcastManager.sendBroadcast(intent);
                break;
            case STATE_MACHINE_BACKSTATE:
                IEspUser user = BEspUser.getBuilder().getInstance();
                user.doActionDevicesUpdated(true);
                break;
        }
        
    }
    
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("{mTransforedDeviceCacheQueue: " + mTransformedDeviceCacheQueue + "\n");
        sb.append("mServerLocalDeviceCacheQueue: " + mServerLocalDeviceCacheQueue + "\n");
        sb.append("mLocalDeviceIOTAddressQueue: " + mLocalDeviceIOTAddressQueue + "\n");
        sb.append("mStatemachineDeviceCacheQueue: " + mStatemachineDeviceCacheQueue + "\n");
        sb.append("mSharedDeviceCacheQueue: " + mSharedDeviceCacheQueue + "\n");
        sb.append("mUpgradeSucLocalDeviceCacheQueue: " + mUpgradeSucLocalDeviceCacheQueue + "\n");
        sb.append("mStaDeviceIOTAddressQueue: " + mStaDeviceIOTAddressQueue + "}\n");
        return sb.toString();
    }
    
}
