package com.espressif.iot.type.help;

import com.espressif.iot.help.statemachine.IEspHelpStep;

public enum HelpStepSSSMeshConfigure implements IEspHelpStep
{
    START_MESH_CONFIGURE(0, "Start mesh configure help"),
    FIND_SOFT_AP(2, "Find softap"),
    FOUND_SOFTAP_FAILED(3, "Found softap failed"),
    SELECT_SOFTAP(4, "Select softap"),
    SELCET_MESH_CONFIGURE(6, "Select mesh configre option"),
    SUC(8, "8, Device mesh configure help is suc");

    private final int mStepValue;
    
    private final String mDetailedMessage;
    
    private HelpStepSSSMeshConfigure(int value, String detailedMessage)
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
    public HelpStepSSSMeshConfigure retryStep()
    {
        HelpStepSSSMeshConfigure retryStep = null;
        switch(this)
        {
            case START_MESH_CONFIGURE:
                break;
            case FIND_SOFT_AP:
                break;
            case FOUND_SOFTAP_FAILED:
                retryStep = FIND_SOFT_AP;
                break;
            case SELECT_SOFTAP:
                break;
            case SELCET_MESH_CONFIGURE:
                break;
            case SUC:
                break;
        }
        return retryStep;
    }

    @Override
    public HelpStepSSSMeshConfigure nextStep(boolean suc)
    {
        if (suc)
        {
            if (mStepValue % 2 != 0)
            {
                throw new IllegalStateException("before call nextStep(), the mStepValue should be even");
            }
            HelpStepSSSMeshConfigure nextStep = null;
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
        else
        {
            return valueOfStep(mStepValue + 1);
        }
    }

    @Override
    public HelpStepSSSMeshConfigure valueOfStep(int stepValue)
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
    
    public static HelpStepSSSMeshConfigure valueOf(int ordinal)
    {
        if (ordinal < 0 || ordinal >= values().length)
        {
            throw new IndexOutOfBoundsException("Invalid ordinal");
        }
        return values()[ordinal];
    }
}
