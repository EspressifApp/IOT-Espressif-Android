package com.espressif.iot.type.help;

import com.espressif.iot.help.statemachine.IEspHelpStep;

public enum HelpStepUsePlug implements IEspHelpStep
{
    START_USE_HELP(0, "0. start plug use help: find plug in user device list first"),
    FAIL_FOUND_PLUG(1, "1. user has not configured any plug: hint user configure at least one plug first"),
    FIND_ONLINE(2, "2. find online plug"),
    NO_PLUG_ONLINE(3, "3. there is no plug online: hint user connect at least one plug"),
    PLUG_SELECT(4, "4. user has configured plug, hint user tap the plug"),
    PLUG_NOT_COMPATIBILITY(5, "5, the plug version is not compatibility, hint user upgrade"),
    PLUG_CONTROL(6, "6. hint user tap the plug to open or close the plug"),
    PLUG_CONTROL_FAILED(7, "post command failed, hint user check the network and the plug"),
    SUC(8, "8, Plug use help is suc");

    private final int mStepValue;
    
    private final String mDetailedMessage;
    
    private HelpStepUsePlug(int value, String detailedMessage)
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
    public HelpStepUsePlug retryStep()
    {
        HelpStepUsePlug retryStep = null;
        switch (this)
        {
            case START_USE_HELP:
                break;
            case FAIL_FOUND_PLUG:
                break;
            case FIND_ONLINE:
                break;
            case NO_PLUG_ONLINE:
                retryStep = HelpStepUsePlug.FIND_ONLINE;
                break;
            case PLUG_SELECT:
                break;
            case PLUG_NOT_COMPATIBILITY:
                break;
            case PLUG_CONTROL:
                break;
            case PLUG_CONTROL_FAILED:
                retryStep = HelpStepUsePlug.PLUG_CONTROL;
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
    public HelpStepUsePlug nextStep(boolean suc)
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
    public HelpStepUsePlug valueOfStep(int stepValue)
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
    
    private HelpStepUsePlug __nextStepSuc()
    {
        if (mStepValue % 2 != 0)
        {
            throw new IllegalStateException("before call nextStep(), the mStepValue should be even");
        }
        HelpStepUsePlug nextStep = null;
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
    
    private HelpStepUsePlug __nextStepFail()
    {
        return valueOfStep(mStepValue + 1);
    }
    
    /**
     * get HelpStepUsePlug by its ordinal
     * 
     * @param ordinal the ordinal of the HelpStepUsePlug
     * @return
     */
    public static HelpStepUsePlug valueOf(int ordinal)
    {
        if (ordinal < 0 || ordinal >= values().length)
        {
            throw new IndexOutOfBoundsException("Invalid ordinal");
        }
        return values()[ordinal];
    }
}
