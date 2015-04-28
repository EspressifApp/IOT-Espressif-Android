package com.espressif.iot.type.help;

import com.espressif.iot.help.statemachine.IEspHelpStep;

public enum HelpStepUpgradeOnline implements IEspHelpStep
{
    START_UPGRADE_ONLINE(0, "0, find device need upgrade"),
    NO_DEVICE_NEED_UPGRADE(1, "1, no device need upgrade"),
    FIND_ONLINE(2, "2, find online device"),
    NO_DEVICE_LOCAL(3, "3, no device online"),
    FOUND_ONLINE(4, "4, there is device online"),
    CHECK_COMPATIBILITY(6, "6, check compatibility"),
    UPGRADING(8, "8, the device is upgrading"),
    UPGRADE_FAILED(9, "9, upgrade failed"),
    SUC(10, "10. upgrade success");
    
    private final int mStepValue;
    
    private final String mDetailedMessage;
    
    private HelpStepUpgradeOnline(int value, String detailedMessage)
    {
        this.mStepValue = value;
        this.mDetailedMessage = detailedMessage;
    }
    
    @Override
    public int getStepValue()
    {
        return mStepValue;
    }
    
    @Override
    public String getDetailedMessage()
    {
        return mDetailedMessage;
    }
    
    @Override
    public HelpStepUpgradeLocal retryStep()
    {
        return null;
    }
    
    @Override
    public HelpStepUpgradeOnline nextStep(boolean suc)
    {
        if (suc)
        {
            return __nextStepSuc();
        }
        else
        {
            return __nextStepFail();
        }
    }
    
    private HelpStepUpgradeOnline __nextStepSuc()
    {
        if (mStepValue % 2 != 0)
        {
            throw new IllegalStateException("before call nextStep(), the mStepValue should be even");
        }
        HelpStepUpgradeOnline nextStep = null;
        for (int nextStepValue = mStepValue + 2; nextStepValue <= SUC.getStepValue(); nextStepValue += 2)
        {
            nextStep = valueOfStep(nextStepValue);
            if (nextStep != null)
            {
                return nextStep;
            }
        }
        throw new IllegalStateException("after call nextStep(), the nextStep is null");
    }
    
    private HelpStepUpgradeOnline __nextStepFail()
    {
        return valueOfStep(mStepValue + 1);
    }
    
    @Override
    public HelpStepUpgradeOnline valueOfStep(int stepValue)
    {
        for (int i = 0; i < values().length; i++)
        {
            if (values()[i].mStepValue == stepValue)
            {
                return values()[i];
            }
        }
        return null;
    }
    
    public static HelpStepUpgradeOnline valueOf(int ordinal)
    {
        if (ordinal < 0 || ordinal >= values().length)
        {
            throw new IndexOutOfBoundsException("Invalid ordinal");
        }
        return values()[ordinal];
    }
}
