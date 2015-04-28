package com.espressif.iot.model.device;

import com.espressif.iot.device.IEspDeviceRemote;
import com.espressif.iot.type.device.status.EspStatusRemote;
import com.espressif.iot.type.device.status.IEspStatusRemote;

public class EspDeviceRemote extends EspDevice implements IEspDeviceRemote
{
    private IEspStatusRemote mStatusRemote;
    
    public EspDeviceRemote()
    {
        mStatusRemote = new EspStatusRemote();
    }
    
    @Override
    public Object clone()
        throws CloneNotSupportedException
    {
        EspDeviceRemote device = (EspDeviceRemote)super.clone();
        // deep copy
        IEspStatusRemote status = device.getStatusRemote();
        device.mStatusRemote = (IEspStatusRemote)((EspStatusRemote)status).clone();
        return device;
    }
    
    @Override
    public IEspStatusRemote getStatusRemote()
    {
        return mStatusRemote;
    }
    
    @Override
    public void setStatusFlammable(IEspStatusRemote statusRemote)
    {
        mStatusRemote = statusRemote;
    }
    
}
