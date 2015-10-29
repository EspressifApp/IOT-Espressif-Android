package com.espressif.iot.action.device.espbutton;

import com.espressif.iot.action.IEspActionLocal;
import com.espressif.iot.device.IEspDevice;

public interface IEspActionEspButtonGroupDelete extends IEspActionLocal
{
    public boolean doActionEspButtonDeleteGroup(IEspDevice inetDevice, String buttonMac, long[] delGroupIds);
}
