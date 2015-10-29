package com.espressif.iot.action.device.espbutton;

import java.util.ArrayList;
import java.util.List;

import com.espressif.iot.command.device.espbutton.EspCommandEspButtonGetDevices;
import com.espressif.iot.command.device.espbutton.IEspCommandEspButtonGetDevices;
import com.espressif.iot.device.IEspDevice;
import com.espressif.iot.user.IEspUser;
import com.espressif.iot.user.builder.BEspUser;

public class EspActionEspButtonGetDevices implements IEspActionEspButtonGetDevices
{
    
    @Override
    public List<IEspDevice> doAcitonEspButtonGetDevices(IEspDevice inetDevice, String buttonMac)
    {
        IEspCommandEspButtonGetDevices command = new EspCommandEspButtonGetDevices();
        List<String> bssids = command.doCommandEspButtonGetDevices(inetDevice, buttonMac);
        
        if (bssids != null)
        {
            IEspUser user = BEspUser.getBuilder().getInstance();
            List<IEspDevice> allDevices = user.getAllDeviceList();
            List<IEspDevice> result = new ArrayList<IEspDevice>();
            
            for (String bssid : bssids)
            {
                for (IEspDevice device : allDevices)
                {
                    if (bssid.equals(device.getBssid()))
                    {
                        result.add(device);
                    }
                }
            }
            
            return result;
        }
        
        return null;
    }
    
}
