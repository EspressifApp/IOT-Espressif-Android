package com.espressif.iot.model.device;

import java.util.ArrayList;
import java.util.List;

import com.espressif.iot.adt.tree.IEspDeviceTreeElement;
import com.espressif.iot.device.IEspDevice;
import com.espressif.iot.device.IEspDeviceRoot;
import com.espressif.iot.user.builder.BEspUser;

public class EspDeviceRoot extends EspDevice implements IEspDeviceRoot
{
    @Override
    public List<IEspDeviceTreeElement> getDeviceTreeElementList()
    {
        if (getDeviceState().isStateLocal())
        {
            return getDeviceTreeElementListLocal();
        }
        else if (getDeviceState().isStateInternet())
        {
            return getDeviceTreeElementListInternet();
        }
        else
        {
            return null;
        }
    }
    
    private List<IEspDeviceTreeElement> getDeviceTreeElementListLocal()
    {
        List<IEspDevice> devices = new ArrayList<IEspDevice>();
        devices.add(this);
        devices.addAll(BEspUser.getBuilder().getInstance().getDeviceList());
        return getDeviceTreeElementList(devices);
    }
    
    private List<IEspDeviceTreeElement> getDeviceTreeElementListInternet()
    {
        List<IEspDevice> userDevices = new ArrayList<IEspDevice>();
        userDevices.addAll(BEspUser.getBuilder().getInstance().getDeviceList());
        
        // create list with root device
        List<List<IEspDevice>> lists = new ArrayList<List<IEspDevice>>();
        for  (IEspDevice device : userDevices)
        {
            if (device.getRootDeviceId() == device.getId() && device.getDeviceState().isStateInternet())
            {
                List<IEspDevice> childDevices = new ArrayList<IEspDevice>();
                childDevices.add(device);
                
                lists.add(childDevices);
            }
        }
        
        // Add device in it's root device list
        for (IEspDevice device : userDevices)
        {
            // Check state
            if (!device.getDeviceState().isStateInternet())
            {
                continue;
            }
            
            for (List<IEspDevice> list : lists)
            {
                IEspDevice rootDevice = list.get(0);
                // Not device itself and same rootDeviceId
                if (rootDevice.getId() != device.getId() && rootDevice.getId() == device.getRootDeviceId() )
                {
                    list.add(device);
                    break;
                }
            }
        }
        
        List<IEspDeviceTreeElement> childList = new ArrayList<IEspDeviceTreeElement>();
        for (List<IEspDevice> list : lists)
        {
            IEspDevice rootDevice = list.get(0);
            childList.addAll(rootDevice.getDeviceTreeElementList(list));
        }
        
        return childList;
    }
}
