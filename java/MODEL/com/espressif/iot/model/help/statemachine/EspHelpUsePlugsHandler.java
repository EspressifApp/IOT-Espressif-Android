package com.espressif.iot.model.help.statemachine;

import com.espressif.iot.help.statemachine.IEspHelpHandler;
import com.espressif.iot.object.IEspSingletonObject;
import com.espressif.iot.type.help.HelpStepUsePlugs;

public class EspHelpUsePlugsHandler implements IEspHelpHandler, IEspSingletonObject
{
    private EspHelpUsePlugsHandler()
    {
    }
    
    private static class InstanceHolder
    {
        static EspHelpUsePlugsHandler instance = new EspHelpUsePlugsHandler();
    }
    
    public static EspHelpUsePlugsHandler getInstance()
    {
        return InstanceHolder.instance;
    }
    
    @Override
    public int getRetryStateOrdinal(int currentStateOrdinal)
    {
        HelpStepUsePlugs currentStep = HelpStepUsePlugs.valueOf(currentStateOrdinal);
        return currentStep.retryStep().ordinal();
    }
    
    @Override
    public int getNextStateOrdinal(int currentStateOrdinal, boolean isSuc)
    {
        HelpStepUsePlugs currentStep = HelpStepUsePlugs.valueOf(currentStateOrdinal);
        return currentStep.nextStep(isSuc).ordinal();
    }
    
    @Override
    public String getStateInDetailed(int currentStateOrdinal)
    {
        return HelpStepUsePlugs.valueOf(currentStateOrdinal).getDetailedMessage();
    }
}
