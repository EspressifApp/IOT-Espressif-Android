package com.espressif.iot.type.help;

import com.espressif.iot.help.statemachine.IEspHelpStep;

public enum HelpStepUseLight implements IEspHelpStep
{
    START_USE_HELP(0, "0. start light use help: find light in user device list first"),
    FAIL_FOUND_LIGHT(1, "1. user has not configured any light: hint user configure at least one light first"),
    FIND_ONLINE(2, "2. find online light"),
    NO_LIGHT_ONLINE(3, "3. there is no light online: hint user connect at least one light"),
    LIGHT_SELECT(4, "4. user has configured light, hint user tap the light"),
    LIGHT_NOT_COMPATIBILITY(5, "5, the light version is not compatibility, hint user upgrade"),
    LIGHT_CONTROL(6, "6. hint user use light"),
    LIGHT_CONTROL_FAILED(7, "post command failed, hint user check the network and the light"),
    SUC(8, "8, Light use help is suc");
    
    private final int mStepValue;
    
    private final String mDetailedMessage;
    
    private HelpStepUseLight(int value, String detailedMessage)
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
    public HelpStepUseLight retryStep()
    {
        HelpStepUseLight retryStep = null;
        switch(this)
        {
            case START_USE_HELP:
                break;
            case FAIL_FOUND_LIGHT:
                break;
            case FIND_ONLINE:
                break;
            case NO_LIGHT_ONLINE:
                retryStep = HelpStepUseLight.FIND_ONLINE;
                break;
            case LIGHT_SELECT:
                break;
            case LIGHT_NOT_COMPATIBILITY:
                break;
            case LIGHT_CONTROL:
                break;
            case LIGHT_CONTROL_FAILED:
                retryStep = HelpStepUseLight.LIGHT_CONTROL;
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
    public HelpStepUseLight nextStep(boolean suc)
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
    
    @Override
    public HelpStepUseLight valueOfStep(int stepValue)
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
    
    private HelpStepUseLight __nextStepSuc()
    {
        if (mStepValue % 2 != 0)
        {
            throw new IllegalStateException("before call nextStep(), the mStepValue should be even");
        }
        HelpStepUseLight nextStep = null;
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
    
    private HelpStepUseLight __nextStepFail()
    {
        return valueOfStep(mStepValue + 1);
    }
    
    public static HelpStepUseLight valueOf(int ordinal)
    {
        if (ordinal < 0 || ordinal >= values().length)
        {
            throw new IndexOutOfBoundsException("Invalid ordinal");
        }
        return values()[ordinal];
    }
}
