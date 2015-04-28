package com.espressif.iot.model.device;

import com.espressif.iot.device.IEspDevicePlug;
import com.espressif.iot.type.device.status.EspStatusPlug;
import com.espressif.iot.type.device.status.IEspStatusPlug;

public class EspDevicePlug extends EspDevice implements IEspDevicePlug
{
    private IEspStatusPlug mStatusPlug;
    
    public EspDevicePlug()
    {
        mStatusPlug = new EspStatusPlug();
    }
    
    @Override
    public Object clone()
        throws CloneNotSupportedException
    {
        EspDevicePlug device = (EspDevicePlug)super.clone();
        // deep copy
        IEspStatusPlug status = device.getStatusPlug();
        device.mStatusPlug = (IEspStatusPlug)((EspStatusPlug)status).clone();
        return device;
    }
    
    @Override
    public IEspStatusPlug getStatusPlug()
    {
        return mStatusPlug;
    }
    
    @Override
    public void setStatusPlug(IEspStatusPlug statusPlug)
    {
        mStatusPlug = statusPlug;
    }
}
