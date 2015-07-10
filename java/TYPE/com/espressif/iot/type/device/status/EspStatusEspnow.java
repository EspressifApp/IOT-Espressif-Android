package com.espressif.iot.type.device.status;

public class EspStatusEspnow implements IEspStatusEspnow
{
    private String mMac;
    
    private int mVoltage = -1;
    
    @Override
    public void setMac(String mac)
    {
        mMac = mac;
    }
    
    @Override
    public String getMac()
    {
        return mMac;
    }
    
    @Override
    public void setVoltage(int voltage)
    {
        mVoltage = voltage;
    }
    
    @Override
    public int getVoltage()
    {
        return mVoltage;
    }
    
    @Override
    public boolean isLowBattery()
    {
        return (mVoltage >= 0 && mVoltage < LOW_BATTERY_VOLTAGEMV);
    }
    
}
