package com.espressif.iot.model.help.statemachine;

import com.espressif.iot.help.statemachine.IEspHelpHandler;
import com.espressif.iot.object.IEspSingletonObject;
import com.espressif.iot.type.help.HelpStepUseLight;

public class EspHelpUseLightHandler implements IEspHelpHandler, IEspSingletonObject
{
    private EspHelpUseLightHandler()
    {
    }
    
    private static class InstanceHolder
    {
        static EspHelpUseLightHandler instance = new EspHelpUseLightHandler();
    }
    
    public static EspHelpUseLightHandler getInstance()
    {
        return InstanceHolder.instance;
    }
    
    @Override
    public int getRetryStateOrdinal(int currentStateOrdinal)
    {
        HelpStepUseLight currentStep = HelpStepUseLight.valueOf(currentStateOrdinal);
        return currentStep.retryStep().ordinal();
    }
    
    @Override
    public int getNextStateOrdinal(int currentStateOrdinal, boolean isSuc)
    {
        HelpStepUseLight currentStep = HelpStepUseLight.valueOf(currentStateOrdinal);
        return currentStep.nextStep(isSuc).ordinal();
    }
    
    @Override
    public String getStateInDetailed(int currentStateOrdinal)
    {
        return HelpStepUseLight.valueOf(currentStateOrdinal).getDetailedMessage();
    }
}
