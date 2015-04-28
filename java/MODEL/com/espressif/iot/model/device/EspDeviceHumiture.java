package com.espressif.iot.model.device;

import com.espressif.iot.device.IEspDeviceHumiture;
import com.espressif.iot.type.device.status.EspStatusHumiture;
import com.espressif.iot.type.device.status.IEspStatusHumiture;

public class EspDeviceHumiture extends EspDevice implements IEspDeviceHumiture
{
    private IEspStatusHumiture mStatusHumiture;
    
    public EspDeviceHumiture()
    {
        mStatusHumiture = new EspStatusHumiture();
    }
    
    @Override
    public IEspStatusHumiture getStatusHumiture()
    {
        return mStatusHumiture;
    }
    
    @Override
    public Object clone()
        throws CloneNotSupportedException
    {
        EspDeviceHumiture device = (EspDeviceHumiture)super.clone();
        // deep copy
        IEspStatusHumiture status = device.getStatusHumiture();
        device.mStatusHumiture = (IEspStatusHumiture)((EspStatusHumiture)status).clone();
        return device;
    }
    
    @Override
    public void setStatusHumiture(IEspStatusHumiture status)
    {
        mStatusHumiture = status;
    }
    
}
