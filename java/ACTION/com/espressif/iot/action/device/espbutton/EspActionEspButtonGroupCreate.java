package com.espressif.iot.action.device.espbutton;

import com.espressif.iot.command.device.espbutton.EspCommandEspButtonGroupCreate;
import com.espressif.iot.command.device.espbutton.IEspCommandEspButtonGroupCreate;
import com.espressif.iot.device.IEspDevice;
import com.espressif.iot.espbutton.BEspButton;
import com.espressif.iot.espbutton.IEspButtonGroup;

public class EspActionEspButtonGroupCreate implements IEspActionEspButtonGroupCreate
{
    
    @Override
    public IEspButtonGroup doActionEspButtonCreateGroup(IEspDevice inetDevice, String buttonMac)
    {
        IEspCommandEspButtonGroupCreate command = new EspCommandEspButtonGroupCreate();
        long groupId = command.doCommandEspButtonCreateGroup(inetDevice, buttonMac);
        if (groupId > 0)
        {
            IEspButtonGroup group = BEspButton.getInstance().allocEspButtonGroup();
            group.setId(groupId);
            return group;
        }
        
        return null;
    }
    
}
