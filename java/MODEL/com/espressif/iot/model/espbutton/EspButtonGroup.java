package com.espressif.iot.model.espbutton;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.espressif.iot.espbutton.IEspButtonGroup;

public class EspButtonGroup implements IEspButtonGroup
{
    private long mId;
    private Set<String> mDeviceBssids;
    
    public EspButtonGroup()
    {
        mDeviceBssids = new HashSet<String>();
    }
    
    @Override
    public void setId(long id)
    {
        mId = id;
    }

    @Override
    public long getId()
    {
        return mId;
    }

    @Override
    public List<String> getDevicesBssid()
    {
        List<String> result = new ArrayList<String>();
        result.addAll(mDeviceBssids);
        return result;
    }

    @Override
    public void setDevicesBssid(List<String> bssids)
    {
        mDeviceBssids.clear();
        mDeviceBssids.addAll(bssids);
    }

    @Override
    public void addDevicesBssid(List<String> bssids)
    {
        mDeviceBssids.addAll(bssids);
    }

    @Override
    public void removeDevicesBssid(List<String> bssids)
    {
        mDeviceBssids.removeAll(bssids);
    }

    @Override
    public void addDeviceBssid(String bssid)
    {
        mDeviceBssids.add(bssid);
    }

    @Override
    public void removeDeviceBssid(String bssid)
    {
        mDeviceBssids.remove(bssid);
    }
    
}
