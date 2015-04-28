package com.espressif.iot.type.help;

import com.espressif.iot.help.statemachine.IEspHelpStep;

public enum HelpStepUseVoltage implements IEspHelpStep
{
    START_USE_HELP(0, "0. start voltage use help: find voltage in user device list first"),
    FAIL_FOUND_VOLTAGE(1, "1. user has not configured any voltage: hint user configure at least one voltage first"),
    VOLTAGE_SELECT(2, "2. user has configured voltage, hint user tap the voltage"),
    VOLTAGE_NOT_COMPATIBILITY(3, "3, the voltage version is not compatibility, hint user upgrade"),
    PULL_DOWN_TO_REFRESH(4, "4, hint user pull down to refresh the data"),
    GET_DATA_FAILED(5, "5, hint user check network and voltage, then pull down again"),
    SELECT_DATE(6, "6, hint user select old date"),
    SELECT_DATE_FAILED(7, "7, get selected date data failed"),
    SUC(8, "8, Voltage use help is suc");;

    private final int mStepValue;
    
    private final String mDetailedMessage;
    
    private HelpStepUseVoltage(int value, String detailedMessage)
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
    public HelpStepUseVoltage retryStep()
    {
        HelpStepUseVoltage retryStep = null;
        switch(this)
        {
            case FAIL_FOUND_VOLTAGE:
                break;
            case VOLTAGE_NOT_COMPATIBILITY:
                break;
            case VOLTAGE_SELECT:
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
    public HelpStepUseVoltage nextStep(boolean suc)
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

    private HelpStepUseVoltage __nextStepSuc()
    {
        if (mStepValue % 2 != 0)
        {
            throw new IllegalStateException("before call nextStep(), the mStepValue should be even");
        }
        HelpStepUseVoltage nextStep = null;
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
    
    private HelpStepUseVoltage __nextStepFail()
    {
        return valueOfStep(mStepValue + 1);
    }
    
    @Override
    public HelpStepUseVoltage valueOfStep(int stepValue)
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
    
    public static HelpStepUseVoltage valueOf(int ordinal)
    {
        if (ordinal < 0 || ordinal >= values().length)
        {
            throw new IndexOutOfBoundsException("Invalid ordinal");
        }
        return values()[ordinal];
    }
}
