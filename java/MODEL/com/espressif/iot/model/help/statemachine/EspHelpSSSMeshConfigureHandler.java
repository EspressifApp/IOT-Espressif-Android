package com.espressif.iot.model.help.statemachine;

import com.espressif.iot.help.statemachine.IEspHelpHandler;
import com.espressif.iot.object.IEspSingletonObject;
import com.espressif.iot.type.help.HelpStepSSSMeshConfigure;

public class EspHelpSSSMeshConfigureHandler implements IEspHelpHandler, IEspSingletonObject
{
    private EspHelpSSSMeshConfigureHandler()
    {
    }
    
    private static class InstanceHolder
    {
        static EspHelpSSSMeshConfigureHandler instance = new EspHelpSSSMeshConfigureHandler();
    }
    
    public static EspHelpSSSMeshConfigureHandler getInstance()
    {
        return InstanceHolder.instance;
    }
    
    @Override
    public int getRetryStateOrdinal(int currentStateOrdinal)
    {
        HelpStepSSSMeshConfigure currentStep = HelpStepSSSMeshConfigure.valueOf(currentStateOrdinal);
        return currentStep.retryStep().ordinal();
    }
    
    @Override
    public int getNextStateOrdinal(int currentStateOrdinal, boolean isSuc)
    {
        HelpStepSSSMeshConfigure currentStep = HelpStepSSSMeshConfigure.valueOf(currentStateOrdinal);
        return currentStep.nextStep(isSuc).ordinal();
    }
    
    @Override
    public String getStateInDetailed(int currentStateOrdinal)
    {
        return HelpStepSSSMeshConfigure.valueOf(currentStateOrdinal).getDetailedMessage();
    }
    
}
