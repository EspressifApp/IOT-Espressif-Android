package com.espressif.iot.action.device.espbutton;

import java.util.List;

import com.espressif.iot.action.IEspActionLocal;
import com.espressif.iot.device.IEspDevice;
import com.espressif.iot.espbutton.IEspButtonGroup;

public interface IEspActionEspButtonGroupList extends IEspActionLocal
{
    public List<IEspButtonGroup> doActionEspButtonListGroup(IEspDevice inetDevice, String buttonMac);
}
