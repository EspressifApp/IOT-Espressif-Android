package com.espressif.iot.model.help.statemachine;

import com.espressif.iot.help.statemachine.IEspHelpHandler;
import com.espressif.iot.object.IEspSingletonObject;
import com.espressif.iot.type.help.HelpStepUseFlammable;

public class EspHelpUseFlammableHandler implements IEspHelpHandler, IEspSingletonObject
{
    private EspHelpUseFlammableHandler()
    {
    }
    
    private static class InstanceHolder
    {
        static EspHelpUseFlammableHandler instance = new EspHelpUseFlammableHandler();
    }
    
    public static EspHelpUseFlammableHandler getInstance()
    {
        return InstanceHolder.instance;
    }
    
    @Override
    public int getRetryStateOrdinal(int currentStateOrdinal)
    {
        HelpStepUseFlammable currentStep = HelpStepUseFlammable.valueOf(currentStateOrdinal);
        return currentStep.retryStep().ordinal();
    }
    
    @Override
    public int getNextStateOrdinal(int currentStateOrdinal, boolean isSuc)
    {
        HelpStepUseFlammable currentStep = HelpStepUseFlammable.valueOf(currentStateOrdinal);
        return currentStep.nextStep(isSuc).ordinal();
    }
    
    @Override
    public String getStateInDetailed(int currentStateOrdinal)
    {
        return HelpStepUseFlammable.valueOf(currentStateOrdinal).getDetailedMessage();
    }
}
