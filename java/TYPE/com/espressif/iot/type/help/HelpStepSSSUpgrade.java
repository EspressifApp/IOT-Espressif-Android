package com.espressif.iot.type.help;

import com.espressif.iot.help.statemachine.IEspHelpStep;

public enum HelpStepSSSUpgrade implements IEspHelpStep
{
    START_HELP(0, "0, hint user pull down to find local device"),
    FOUND_DEVICE_FAILED(1, "1, found sss device failed"),
    SELECT_DEVICE(2, "hint user long click the device"),
    SUC(4, "4, Device upgrade help is suc");
    
    private final int mStepValue;
    
    private final String mDetailedMessage;
    
    private HelpStepSSSUpgrade(int value, String detailedMessage)
    {
        mStepValue = value;
        mDetailedMessage = detailedMessage;
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
    public HelpStepSSSUpgrade retryStep()
    {
        HelpStepSSSUpgrade retryStep = null;
        switch (this)
        {
            case START_HELP:
                break;
            case FOUND_DEVICE_FAILED:
                retryStep = START_HELP;
                break;
            case SELECT_DEVICE:
                break;
            case SUC:
                break;
        }
        return retryStep;
    }
    
    @Override
    public HelpStepSSSUpgrade nextStep(boolean suc)
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
    
    private HelpStepSSSUpgrade __nextStepSuc()
    {
        if (mStepValue % 2 != 0)
        {
            throw new IllegalStateException("before call nextStep(), the mStepValue should be even");
        }
        HelpStepSSSUpgrade nextStep = null;
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
    
    private HelpStepSSSUpgrade __nextStepFail()
    {
        return valueOfStep(mStepValue + 1);
    }
    
    @Override
    public HelpStepSSSUpgrade valueOfStep(int stepValue)
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
    
    public static HelpStepSSSUpgrade valueOf(int ordinal)
    {
        if (ordinal < 0 || ordinal >= values().length)
        {
            throw new IndexOutOfBoundsException("Invalid ordinal");
        }
        return values()[ordinal];
    }
}
