package com.espressif.iot.model.help.statemachine;

import com.espressif.iot.help.statemachine.IEspHelpHandler;
import com.espressif.iot.object.IEspSingletonObject;
import com.espressif.iot.type.help.HelpStepMeshConfigure;

public class EspHelpMeshConfigureHandler implements IEspHelpHandler, IEspSingletonObject
{
    private EspHelpMeshConfigureHandler()
    {
    }
    
    private static class InstanceHolder
    {
        static EspHelpMeshConfigureHandler instance = new EspHelpMeshConfigureHandler();
    }
    
    public static EspHelpMeshConfigureHandler getInstance()
    {
        return InstanceHolder.instance;
    }
    
    @Override
    public int getRetryStateOrdinal(int currentStateOrdinal)
    {
        HelpStepMeshConfigure currentStep = HelpStepMeshConfigure.valueOf(currentStateOrdinal);
        return currentStep.retryStep().ordinal();
    }
    
    @Override
    public int getNextStateOrdinal(int currentStateOrdinal, boolean isSuc)
    {
        HelpStepMeshConfigure currentStep = HelpStepMeshConfigure.valueOf(currentStateOrdinal);
        return currentStep.nextStep(isSuc).ordinal();
    }
    
    @Override
    public String getStateInDetailed(int currentStateOrdinal)
    {
        return HelpStepMeshConfigure.valueOf(currentStateOrdinal).getDetailedMessage();
    }
}
