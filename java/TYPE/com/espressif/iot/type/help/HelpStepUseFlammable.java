package com.espressif.iot.type.help;

import com.espressif.iot.help.statemachine.IEspHelpStep;

public enum HelpStepUseFlammable implements IEspHelpStep
{
    START_USE_HELP(0, "0. start flammable use help: find flammable in user device list first"),
    FAIL_FOUND_FLAMMABLE(1, "1. user has not configured any flammable: hint user configure at least one flammable first"),
    FLAMMABLE_SELECT(2, "2. user has configured flammable, hint user tap the flammable"),
    FLAMMABLE_NOT_COMPATIBILITY(3, "3, the flammable version is not compatibility, hint user upgrade"),
    PULL_DOWN_TO_REFRESH(4, "4, hint user pull down to refresh the data"),
    GET_DATA_FAILED(5, "5, hint user check network and flammable, then pull down again"),
    SELECT_DATE(6, "6, hint user select old date"),
    SELECT_DATE_FAILED(7, "7, get selected date data failed"),
    SUC(8, "8, Flammable use help is suc");;

    private final int mStepValue;
    
    private final String mDetailedMessage;
    
    private HelpStepUseFlammable(int value, String detailedMessage)
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
    public HelpStepUseFlammable retryStep()
    {
        HelpStepUseFlammable retryStep = null;
        switch(this)
        {
            case FAIL_FOUND_FLAMMABLE:
                break;
            case FLAMMABLE_NOT_COMPATIBILITY:
                break;
            case FLAMMABLE_SELECT:
                break;
            case GET_DATA_FAILED:
                break;
            case PULL_DOWN_TO_REFRESH:
                break;
            case SELECT_DATE:
                break;
            case SELECT_DATE_FAILED:
                retryStep = PULL_DOWN_TO_REFRESH;
                break;
            case START_USE_HELP:
                break;
            case SUC:
                break;
        }
        if (retryStep == null)
        {
            throw new IllegalStateException("step " + this.mStepValue + " don't support retry");
        }
        else
        {
            return retryStep;
        }
    }

    @Override
    public HelpStepUseFlammable nextStep(boolean suc)
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

    private HelpStepUseFlammable __nextStepSuc()
    {
        if (mStepValue % 2 != 0)
        {
            throw new IllegalStateException("before call nextStep(), the mStepValue should be even");
        }
        HelpStepUseFlammable nextStep = null;
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
    
    private HelpStepUseFlammable __nextStepFail()
    {
        return valueOfStep(mStepValue + 1);
    }
    
    @Override
    public HelpStepUseFlammable valueOfStep(int stepValue)
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
    
    public static HelpStepUseFlammable valueOf(int ordinal)
    {
        if (ordinal < 0 || ordinal >= values().length)
        {
            throw new IndexOutOfBoundsException("Invalid ordinal");
        }
        return values()[ordinal];
    }
}
