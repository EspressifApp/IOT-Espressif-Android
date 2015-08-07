package com.espressif.iot.model.device;

import java.util.ArrayList;
import java.util.List;

import com.espressif.iot.adt.tree.IEspDeviceTreeElement;
import com.espressif.iot.device.IEspDevice;
import com.espressif.iot.device.IEspDeviceRoot;
import com.espressif.iot.type.device.IEspDeviceState;
import com.espressif.iot.user.builder.BEspUser;

public class EspDeviceRoot extends EspDevice implements IEspDeviceRoot
{
    @Override
    public List<IEspDeviceTreeElement> getDeviceTreeElementList(List<IEspDevice> allDeviceList)
    {
        List<IEspDeviceTreeElement> elements = new ArrayList<IEspDeviceTreeElement>();
        for (IEspDevice device : allDeviceList)
        {
            if (device.getParentDeviceBssid() == null && !device.equals(this))
            {
                elements.addAll(device.getDeviceTreeElementList(allDeviceList));
            }
        }
        return elements;
    }
    
    @Override
    public List<IEspDeviceTreeElement> getDeviceTreeElementList()
    {
        return getDeviceTreeElementList(BEspUser.getBuilder().getInstance().getAllDeviceList());
    }
    
    @Override
    public IEspDeviceState getDeviceState()
    {
        return mDeviceState;
    }
    
    @Override
    public void setDeviceState(IEspDeviceState deviceState)
    {
        this.mDeviceState = deviceState;
    }
    
    @Override
    public void copyDeviceState(IEspDevice device)
    {
        this.mDeviceState.setStateValue(device.getDeviceState().getStateValue());
    }
    
}
