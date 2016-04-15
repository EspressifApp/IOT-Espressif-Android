package com.espressif.iot.model.device.array;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.espressif.iot.device.IEspDevice;
import com.espressif.iot.device.array.IEspDeviceArray;
import com.espressif.iot.model.device.EspDeviceLight;

public class EspDeviceLightArray extends EspDeviceLight implements IEspDeviceArray
{
    private Set<IEspDevice> mDeviceSet;
    
    public EspDeviceLightArray()
    {
        mDeviceSet = new HashSet<IEspDevice>();
    }
    
    @Override
    public synchronized void addDevice(IEspDevice device)
    {
        mDeviceSet.add(device);
    }

    @Override
    public synchronized void removeDevice(IEspDevice device)
    {
        if (mDeviceSet.contains(device))
        {
            mDeviceSet.remove(device);
        }
    }

    @Override
    public synchronized List<IEspDevice> getDeviceList()
    {
        List<IEspDevice> devices = new ArrayList<IEspDevice>();
        devices.addAll(mDeviceSet);
        return devices;
    }
}
