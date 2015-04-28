package com.espressif.iot.model.device;

import com.espressif.iot.device.IEspDeviceVoltage;
import com.espressif.iot.type.device.status.EspStatusVoltage;
import com.espressif.iot.type.device.status.IEspStatusVoltage;

public class EspDeviceVoltage extends EspDevice implements IEspDeviceVoltage
{
    private IEspStatusVoltage mStatusVoltage;
    
    public EspDeviceVoltage()
    {
        mStatusVoltage = new EspStatusVoltage();
    }
    
    @Override
    public Object clone()
        throws CloneNotSupportedException
    {
        EspDeviceVoltage device = (EspDeviceVoltage)super.clone();
        // deep copy
        IEspStatusVoltage status = device.getStatusVoltage();
        device.mStatusVoltage = (IEspStatusVoltage)((EspStatusVoltage)status).clone();
        return device;
    }

    @Override
    public IEspStatusVoltage getStatusVoltage()
    {
        return mStatusVoltage;
    }

    @Override
    public void setStatusVoltage(IEspStatusVoltage statusVoltage)
    {
        mStatusVoltage = statusVoltage;
    }

}
