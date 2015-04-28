package com.espressif.iot.type.device.status;

public class EspStatusRemote implements IEspStatusRemote, Cloneable
{
    private int mAddr;
    
    private int mCmd;
    
    private int mRep;
    
    @Override
    public int getAddress()
    {
        return mAddr;
    }
    
    @Override
    public void setAddress(int addr)
    {
        mAddr = addr;
    }
    
    @Override
    public int getCommand()
    {
        return mCmd;
    }
    
    @Override
    public void setCommand(int cmd)
    {
        mCmd = cmd;
    }
    
    @Override
    public int getRepeat()
    {
        return mRep;
    }
    
    @Override
    public void setRepeat(int rep)
    {
        mRep = rep;
    }
    
    @Override
    public String toString()
    {
        return "EspStatusRemote: (mAddr=[" + mAddr + "],mCmd=[" + mCmd + "],mRep=[" + mRep + "])";
    }
    
    @Override
    public Object clone()
        throws CloneNotSupportedException
    {
        return super.clone();
    }
    
}
