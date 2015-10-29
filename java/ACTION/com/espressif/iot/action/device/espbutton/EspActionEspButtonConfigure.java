package com.espressif.iot.action.device.espbutton;

import java.util.ArrayList;
import java.util.List;

import com.espressif.iot.command.device.espbutton.EspCommandEspButtonConfigure;
import com.espressif.iot.command.device.espbutton.IEspButtonConfigureListener;
import com.espressif.iot.command.device.espbutton.IEspCommandEspButtonConfigure;
import com.espressif.iot.device.IEspDevice;

public class EspActionEspButtonConfigure implements IEspActionEspButtonConfigure
{
    
    @Override
    public boolean doActionEspButtonConfigure(String newTempKey, String newMacAddress, boolean permitAllRequest,
        List<IEspDevice> deviceList, boolean isBroadcast, IEspButtonConfigureListener listener, String... oldMacAddress)
    {
        List<List<IEspDevice>> rootDevicesGroups = new ArrayList<List<IEspDevice>>();
        for (IEspDevice device : deviceList)
        {
            boolean deviceGrouping = false;
            for (List<IEspDevice> rootGroup : rootDevicesGroups)
            {
                IEspDevice groupDevice = rootGroup.get(0);
                if (device.getRootDeviceBssid().equals(groupDevice.getRootDeviceBssid()))
                {
                    rootGroup.add(device);
                    deviceGrouping = true;
                    break;
                }
            }
            
            if (!deviceGrouping)
            {
                List<IEspDevice> deviceGroup = new ArrayList<IEspDevice>();
                deviceGroup.add(device);
                rootDevicesGroups.add(deviceGroup);
            }
        }
        
        boolean result = true;
        for (List<IEspDevice> rootGroup : rootDevicesGroups)
        {
            IEspCommandEspButtonConfigure command = new EspCommandEspButtonConfigure();
            int cmdResult =
                command.doCommandEspButtonConfigure(rootGroup,
                    newTempKey,
                    newMacAddress,
                    isBroadcast,
                    permitAllRequest,
                    listener,
                    oldMacAddress);
            
            if (cmdResult == IEspCommandEspButtonConfigure.RESULT_OVER)
            {
                break;
            }
            else if (cmdResult == IEspCommandEspButtonConfigure.RESULT_FAILED)
            {
                result = false;
            }
        }
        
        return result;
    }
    
}
