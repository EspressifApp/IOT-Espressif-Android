package com.espressif.iot.base.upgrade.apk;

public class UpgradeInfoApk
{
    
    private static final int VERSION_BETA = -1;
    
    private static final int VERSION_OFFICIAL = 1;
    
    private static final int VERSION_UNKNOW = 0;
    
    private String mVersion;
    
    private int mVerType;// (-1)-(+1)
    
    private int mVerNum1;
    
    private int mVerNum2;
    
    private int mVerNum3;
    
    private long mVersionValue;// 0-1000*1000*1000=10^9
    
    private boolean mLegal;
    
    public UpgradeInfoApk(String version)
    {
        mLegal = true;
        
        try
        {
            mVersion = version;
            
            setVersionType(version.charAt(0));
            
            StringBuilder sb = new StringBuilder(version);
            sb.deleteCharAt(0);
            String[] verNums = sb.toString().split("\\.");
            mVerNum1 = Integer.parseInt(verNums[0]);
            mVerNum2 = Integer.parseInt(verNums[1]);
            mVerNum3 = Integer.parseInt(verNums[2]);
            
            mVersionValue = mVerNum1 * 1000 * 1000 + mVerNum2 * 1000 + mVerNum3;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            
            mVersionValue = 0;
            mLegal = false;
        }
    }
    
    public long getVersionValue()
    {
        return mVersionValue;
    }
    
    private void setVersionType(char ver)
    {
        switch (ver)
        {
            case 'v':
                mVerType = VERSION_OFFICIAL;
                break;
            case 'b':
                mVerType = VERSION_BETA;
                break;
            default:
                mVerType = VERSION_UNKNOW;
                mLegal = false;
                break;
        }
    }
    
    public int getVersionType()
    {
        return mVerType;
    }
    
    public boolean isLegal()
    {
        return mLegal;
    }
    
    @Override
    public String toString()
    {
        return mVersion;
    }
    
    public long getIdValue()
    {
        long idValue = 0;
        idValue += mVersionValue;
        idValue *= Math.pow(10, 1);
        idValue += (mVerType + 1);
        // UpgradeInfoApk + 1*10*18 to make different from UpgradeInfoDevice
        idValue += 1 * (Math.pow(10, 18));
        return idValue;
    }
    
}