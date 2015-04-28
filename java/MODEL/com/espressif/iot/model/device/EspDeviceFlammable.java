package com.espressif.iot.model.device;

import com.espressif.iot.device.IEspDeviceFlammable;
import com.espressif.iot.type.device.status.EspStatusFlammable;
import com.espressif.iot.type.device.status.IEspStatusFlammable;

public class EspDeviceFlammable extends EspDevice implements IEspDeviceFlammable
{
    private IEspStatusFlammable mStatusFlammable;
    
    public EspDeviceFlammable()
    {
        mStatusFlammable = new EspStatusFlammable();
    }
    
    @Override
    public Object clone()
        throws CloneNotSupportedException
    {
        EspDeviceFlammable device = (EspDeviceFlammable)super.clone();
        // deep copy
        IEspStatusFlammable status = device.getStatusFlammable();
        device.mStatusFlammable = (IEspStatusFlammable)((EspStatusFlammable)status).clone();
        return device;
    }
    
    @Override
    public IEspStatusFlammable getStatusFlammable()
    {
        return mStatusFlammable;
    }
    
    @Override
    public void setStatusFlammable(IEspStatusFlammable statusFlammable)
    {
        mStatusFlammable = statusFlammable;
    }
    
}
