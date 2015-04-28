package com.espressif.iot.type.help;

import com.espressif.iot.help.statemachine.IEspHelpStep;

public enum HelpStepUsePlugs implements IEspHelpStep
{
    START_USE_HELP(0, "0. start plugs use help: find plug in user device list first"),
    FAIL_FOUND_PLUGS(1, "1. user has not configured any plugs: hint user configure at least one plugs first"),
    FIND_ONLINE(2, "2. find online plug"),
    NO_PLUGS_ONLINE(3, "3. there is no plugs online: hint user connect at least one plugs"),
    PLUGS_SELECT(4, "4. user has configured plugs, hint user click the plugs"),
    PLUGS_NOT_COMPATIBILITY(5, "5, the plugs version is not compatibility, hint user upgrade"),
    PLUGS_CONTROL(6, "6. hint user tap the plugs to open or close the plugs"),
    PLUGS_CONTROL_FAILED(7, "post command failed, hint user check the network and the plugs"),
    SUC(8, "8, Plugs use help is suc");
    
    private final int mStepValue;
    
    private final String mDetailedMessage;
    
    private HelpStepUsePlugs(int value, String detailedMessage)
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
    public HelpStepUsePlugs retryStep()
    {
        HelpStepUsePlugs retryStep = null;
        switch (this)
        {
            case START_USE_HELP:
                break;
            case FAIL_FOUND_PLUGS:
                break;
            case FIND_ONLINE:
                break;
            case NO_PLUGS_ONLINE:
                retryStep = HelpStepUsePlugs.FIND_ONLINE;
                break;
            case PLUGS_SELECT:
                break;
            case PLUGS_NOT_COMPATIBILITY:
                break;
            case PLUGS_CONTROL:
                break;
            case PLUGS_CONTROL_FAILED:
                retryStep = HelpStepUsePlugs.PLUGS_CONTROL;
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
    public HelpStepUsePlugs nextStep(boolean suc)
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
    
    private HelpStepUsePlugs __nextStepSuc()
    {
        if (mStepValue % 2 != 0)
        {
            throw new IllegalStateException("before call nextStep(), the mStepValue should be even");
        }
        HelpStepUsePlugs nextStep = null;
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
    
    private HelpStepUsePlugs __nextStepFail()
    {
        return valueOfStep(mStepValue + 1);
    }
    
    @Override
    public HelpStepUsePlugs valueOfStep(int stepValue)
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
    
    public static HelpStepUsePlugs valueOf(int ordinal)
    {
        if (ordinal < 0 || ordinal >= values().length)
        {
            throw new IndexOutOfBoundsException("Invalid ordinal");
        }
        return values()[ordinal];
    }
}
