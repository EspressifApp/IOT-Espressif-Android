package com.espressif.iot.model.help.statemachine;

import com.espressif.iot.help.statemachine.IEspHelpHandler;
import com.espressif.iot.object.IEspSingletonObject;
import com.espressif.iot.type.help.HelpStepUseHumiture;

public class EspHelpUseHumitureHandler implements IEspHelpHandler, IEspSingletonObject
{
    private EspHelpUseHumitureHandler()
    {
    }
    
    private static class InstanceHolder
    {
        static EspHelpUseHumitureHandler instance = new EspHelpUseHumitureHandler();
    }
    
    public static EspHelpUseHumitureHandler getInstance()
    {
        return InstanceHolder.instance;
    }
    
    @Override
    public int getRetryStateOrdinal(int currentStateOrdinal)
    {
        HelpStepUseHumiture currentStep = HelpStepUseHumiture.valueOf(currentStateOrdinal);
        return currentStep.retryStep().ordinal();
    }
    
    @Override
    public int getNextStateOrdinal(int currentStateOrdinal, boolean isSuc)
    {
        HelpStepUseHumiture currentStep = HelpStepUseHumiture.valueOf(currentStateOrdinal);
        return currentStep.nextStep(isSuc).ordinal();
    }
    
    @Override
    public String getStateInDetailed(int currentStateOrdinal)
    {
        return HelpStepUseHumiture.valueOf(currentStateOrdinal).getDetailedMessage();
    }
}
