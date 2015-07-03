package com.espressif.iot.type.help;

import com.espressif.iot.help.statemachine.IEspHelpStep;

public enum HelpStepMeshConfigure implements IEspHelpStep
{
    START(0, "0, Start mesh configure"),
    SCAN_MESH_SOFTAP(2, "2, Scan mesh softap"),
    SCAN_MESH_SOFTAP_FAILED(3, "3, Scan mesh softap failed"),
    SELECT_SOFTAP(4, "4, SELECT softap"),
    SUC(6, "6, Device mesh configure help is suc");

    private final int mStepValue;
    
    private final String mDetailedMessage;
    
    private HelpStepMeshConfigure(int value, String detailedMessage)
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
    public HelpStepMeshConfigure retryStep()
    {
        HelpStepMeshConfigure retryStep = null;
        switch (this)
        {
            case START:
                break;
            case SCAN_MESH_SOFTAP:
                break;
            case SCAN_MESH_SOFTAP_FAILED:
                retryStep = SCAN_MESH_SOFTAP;
                break;
            case SELECT_SOFTAP:
                break;
            case SUC:
                break;
        }
        return retryStep;
    }

    @Override
    public HelpStepMeshConfigure nextStep(boolean suc)
    {
        if (suc)
        {
            if (mStepValue % 2 != 0)
            {
                throw new IllegalStateException("before call nextStep(), the mStepValue should be even");
            }
            HelpStepMeshConfigure nextStep = null;
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
    public HelpStepMeshConfigure valueOfStep(int stepValue)
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
    
    public static HelpStepMeshConfigure valueOf(int ordinal)
    {
        if (ordinal < 0 || ordinal >= values().length)
        {
            throw new IndexOutOfBoundsException("Invalid ordinal");
        }
        return values()[ordinal];
    }
}
