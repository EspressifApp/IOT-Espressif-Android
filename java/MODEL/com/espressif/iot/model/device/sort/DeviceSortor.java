package com.espressif.iot.model.device.sort;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import com.espressif.iot.device.IEspDevice;

public class DeviceSortor
{
    public static enum DeviceSortType
    {
        DEVICE_NAME, ACTIVATED_TIME
    }
    
    public void sort(List<IEspDevice> deviceList, DeviceSortType sortType)
    {
        Comparator<IEspDevice> comparator = null;
        switch (sortType)
        {
            case DEVICE_NAME:
                comparator = mDeviceNameComparator;
                break;
            case ACTIVATED_TIME:
                comparator = mActivateTimeComparator;
                break;
        }
        Collections.sort(deviceList, comparator);
    }
    
    private int getStateCompareValue(IEspDevice device)
    {
        switch (device.getDeviceState().getDeviceState())
        {
            case INTERNET:
            case LOCAL:
                return 1;
                
            case ACTIVATING:
                return 2;
                
            case UPGRADING_INTERNET:
            case UPGRADING_LOCAL:
                return 3;
                
            case CONFIGURING:
                return 4;
                
            case OFFLINE:
                return 5;
                
            default:
                return 10;
        }
    }
    
    private Comparator<IEspDevice> mDeviceNameComparator = new Comparator<IEspDevice>()
    {
        
        @Override
        public int compare(IEspDevice lDevice, IEspDevice rDevice)
        {
            int result;
            // Sort by state
            result = compareState(lDevice, rDevice);
            
            if (result == 0)
            {
                // Same state, sort by device name
                result = compareName(lDevice, rDevice);
            }
            
            if (result == 0)
            {
                // Same device name, sort by bssid
                result = compareBssid(lDevice, rDevice);
            }
            
            return result;
        }
    };
    
    private Comparator<IEspDevice> mActivateTimeComparator = new Comparator<IEspDevice>()
    {
        
        @Override
        public int compare(IEspDevice lDevice, IEspDevice rDevice)
        {
            int result;
            // Sort by state
            result = compareState(lDevice, rDevice);
            
            if (result == 0)
            {
                // Same state, sort by activated time
                result = compareActivatedTime(lDevice, rDevice);
            }
            
            if (result == 0)
            {
                // Same activated time, sort by bssid
                result = compareBssid(lDevice, rDevice);
            }
            
            return result;
        }
    };
    
    private int compareState(IEspDevice lDevice, IEspDevice rDevice)
    {
        Integer lState = getStateCompareValue(lDevice);
        Integer rState = getStateCompareValue(rDevice);
        
        return lState.compareTo(rState);
    }
    
    private int compareBssid(IEspDevice lDevice, IEspDevice rDevice)
    {
        String lBssid = lDevice.getBssid();
        String rBssid = rDevice.getBssid();
        
        return lBssid.compareTo(rBssid);
    }
    
    private int compareName(IEspDevice lDevice, IEspDevice rDevice)
    {
        int result;
        
        String lName = lDevice.getName().toUpperCase(Locale.getDefault());
        String rName = rDevice.getName().toUpperCase(Locale.getDefault());
        if (lName.equals(rName))
        {
            result = 0;
        }
        else
        {
            // order device by its name
            List<String> lrNameList = new ArrayList<String>();
            lrNameList.add(lName);
            lrNameList.add(rName);
            // for Chinese can't be compared by its name directly,
            // but Chinese can be sorted by its name directly
            Collections.sort(lrNameList, Collator.getInstance(Locale.getDefault()));
            if (lrNameList.get(0).equals(lName))
            {
                result = -1;
            }
            else
            {
                result = 1;
            }
        }
        return result;
    }
    
    private int compareActivatedTime(IEspDevice lDevice, IEspDevice rDevice)
    {
        Long lTime = lDevice.getActivatedTime();
        Long rTime = rDevice.getActivatedTime();
        
        return rTime.compareTo(lTime);
    }
}
