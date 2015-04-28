package com.espressif.iot.type.help;

import com.espressif.iot.help.statemachine.IEspHelpStep;

public enum HelpStepUseRemote implements IEspHelpStep
{
    START_USE_HELP(0, "0. start remote use help: find remote in user device list first"),
    FAIL_FOUND_REMOTE(1, "1. user has not configured any remote: hint user configure at least one remote first"),
    FIND_ONLINE(2, "2. find online remote"),
    NO_REMOTE_ONLINE(3, "3. there is no remote online: hint user connect at least one remote"),
    REMOTE_SELECT(4, "4. user has configured remote, hint user tap the remote"),
    REMOTE_NOT_COMPATIBILITY(5, "5, the remote version is not compatibility, hint user upgrade"),
    REMOTE_CONTROL(6, "6. hint user use remote"),
    REMOTE_CONTROL_FAILED(7, "post command failed, hint user check the network and the remote"),
    SUC(8, "8, Light use help is suc");
    
    private final int mStepValue;
    
    private final String mDetailedMessage;
    
    private HelpStepUseRemote(int value, String detailedMessage)
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
    public HelpStepUseRemote retryStep()
    {
        HelpStepUseRemote retryStep = null;
        switch (this)
        {
            case START_USE_HELP:
                break;
            case FAIL_FOUND_REMOTE:
                break;
            case FIND_ONLINE:
                break;
            case NO_REMOTE_ONLINE:
                retryStep = HelpStepUseRemote.FIND_ONLINE;
                break;
            case REMOTE_SELECT:
                break;
            case REMOTE_NOT_COMPATIBILITY:
                break;
            case REMOTE_CONTROL:
                break;
            case REMOTE_CONTROL_FAILED:
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
    public HelpStepUseRemote nextStep(boolean suc)
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
    
    private HelpStepUseRemote __nextStepSuc()
    {
        if (mStepValue % 2 != 0)
        {
            throw new IllegalStateException("before call nextStep(), the mStepValue should be even");
        }
        HelpStepUseRemote nextStep = null;
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
    
    private HelpStepUseRemote __nextStepFail()
    {
        return valueOfStep(mStepValue + 1);
    }
    
    @Override
    public HelpStepUseRemote valueOfStep(int stepValue)
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
    
    public static HelpStepUseRemote valueOf(int ordinal)
    {
        if (ordinal < 0 || ordinal >= values().length)
        {
            throw new IndexOutOfBoundsException("Invalid ordinal");
        }
        return values()[ordinal];
    }
}
