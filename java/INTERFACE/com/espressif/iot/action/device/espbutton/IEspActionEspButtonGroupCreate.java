package com.espressif.iot.action.device.espbutton;

import com.espressif.iot.action.IEspActionLocal;
import com.espressif.iot.device.IEspDevice;
import com.espressif.iot.espbutton.IEspButtonGroup;

public interface IEspActionEspButtonGroupCreate extends IEspActionLocal
{
    public IEspButtonGroup doActionEspButtonCreateGroup(IEspDevice inetDevice, String buttonMac);
}
