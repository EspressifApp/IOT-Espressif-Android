package com.espressif.iot.type.help;

import com.espressif.iot.help.statemachine.IEspHelpStep;

public enum HelpStepSSSUseDevice implements IEspHelpStep
{
    START_USE_HELP(0, "0, start help, find local plug"),
    FAIL_FOUND_DEVICE(1, "1, there are not any local device"),
    START_DIRECT_CONNECT(2, "2, hint user click configure button"),
    FIND_SOFTAP(4, "4, hint user pull down to refresh softap"),
    FOUND_SOFTAP_FAILED(5, "5, scan softap failed"),
    SELECT_SOFTAP(6, "select softap user want connect"),
    CONNECT_SOFTAP_FAILED(7, "connect soft ap failed"),
    SOFTAP_NOT_SUPPORT(9, "the connected device do not support to use local"),
    DEVICE_SELECT(10, "2, hint user select device"),
    DEVICE_CONTROL(12, "6. hint user tap the plug to open or close the device"),
    DEVICE_CONTROL_FAILED(13, "post command failed, hint user check the network and the device"),
    SUC(14, "14, Device use help is suc");
    
    private final int mStepValue;
    
    private final String mDetailedMessage;
    
    private HelpStepSSSUseDevice(int value, String detailedMessage)
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
    public HelpStepSSSUseDevice retryStep()
    {
        HelpStepSSSUseDevice retryStep = null;
        switch(this)
        {
            case START_USE_HELP:
                break;
            case FAIL_FOUND_DEVICE:
                retryStep = START_USE_HELP;
                break;
            case START_DIRECT_CONNECT:
                break;
            case FIND_SOFTAP:
                break;
            case FOUND_SOFTAP_FAILED:
                retryStep = FIND_SOFTAP;
                break;
            case SELECT_SOFTAP:
                break;
            case CONNECT_SOFTAP_FAILED:
                retryStep = SELECT_SOFTAP;
                break;
            case SOFTAP_NOT_SUPPORT:
                retryStep = SELECT_SOFTAP;
                break;
            case DEVICE_SELECT:
                break;
            case DEVICE_CONTROL:
                break;
            case DEVICE_CONTROL_FAILED:
                break;
            case SUC:
                break;
        }
        return retryStep;
    }
    
    @Override
    public HelpStepSSSUseDevice nextStep(boolean suc)
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
    
    private HelpStepSSSUseDevice __nextStepSuc()
    {
        if (mStepValue % 2 != 0)
        {
            throw new IllegalStateException("before call nextStep(), the mStepValue should be even");
        }
        HelpStepSSSUseDevice nextStep = null;
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
    
    private HelpStepSSSUseDevice __nextStepFail()
    {
        if (mStepValue == CONNECT_SOFTAP_FAILED.getStepValue())
        {
            return SOFTAP_NOT_SUPPORT;
        }
        else
        {
            return valueOfStep(mStepValue + 1);
        }
    }
    
    @Override
    public HelpStepSSSUseDevice valueOfStep(int stepValue)
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
    
    public static HelpStepSSSUseDevice valueOf(int ordinal)
    {
        if (ordinal < 0 || ordinal >= values().length)
        {
            throw new IndexOutOfBoundsException("Invalid ordinal");
        }
        return values()[ordinal];
    }
}
