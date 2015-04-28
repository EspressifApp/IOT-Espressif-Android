package com.espressif.iot.model.help.statemachine;

import com.espressif.iot.help.statemachine.IEspHelpHandler;
import com.espressif.iot.object.IEspSingletonObject;
import com.espressif.iot.type.help.HelpStepUsePlug;

public class EspHelpUsePlugHandler implements IEspHelpHandler, IEspSingletonObject
{
    private EspHelpUsePlugHandler()
    {
    }
    
    private static class InstanceHolder
    {
        static EspHelpUsePlugHandler instance = new EspHelpUsePlugHandler();
    }
    
    public static EspHelpUsePlugHandler getInstance()
    {
        return InstanceHolder.instance;
    }
    
    @Override
    public int getRetryStateOrdinal(int currentStateOrdinal)
    {
        HelpStepUsePlug currentStep = HelpStepUsePlug.valueOf(currentStateOrdinal);
        return currentStep.retryStep().ordinal();
    }
    
    @Override
    public int getNextStateOrdinal(int currentStateOrdinal, boolean isSuc)
    {
        HelpStepUsePlug currentStep = HelpStepUsePlug.valueOf(currentStateOrdinal);
        return currentStep.nextStep(isSuc).ordinal();
    }
    
    @Override
    public String getStateInDetailed(int currentStateOrdinal)
    {
        return HelpStepUsePlug.valueOf(currentStateOrdinal).getDetailedMessage();
    }
}
