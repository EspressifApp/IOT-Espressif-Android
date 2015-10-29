package com.espressif.iot.command.device.espbutton;

import java.util.List;

import com.espressif.iot.command.IEspCommandLocal;
import com.espressif.iot.device.IEspDevice;
import com.espressif.iot.espbutton.IEspButtonGroup;

public interface IEspCommandEspButtonGroupList extends IEspCommandEspButton, IEspCommandLocal
{
    public List<IEspButtonGroup> doCommandEspButtonListGroup(IEspDevice inetDevice, String buttonMac);
}
