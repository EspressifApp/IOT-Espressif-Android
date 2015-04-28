package com.espressif.iot.model.help.statemachine;

import com.espressif.iot.help.statemachine.IEspHelpHandler;
import com.espressif.iot.object.IEspSingletonObject;
import com.espressif.iot.type.help.HelpStepUseVoltage;

public class EspHelpUseVoltageHandler implements IEspHelpHandler, IEspSingletonObject
{
    private EspHelpUseVoltageHandler()
    {
    }
    
    private static class InstanceHolder
    {
        static EspHelpUseVoltageHandler instance = new EspHelpUseVoltageHandler();
    }
    
    public static EspHelpUseVoltageHandler getInstance()
    {
        return InstanceHolder.instance;
    }
    
    @Override
    public int getRetryStateOrdinal(int currentStateOrdinal)
    {
        HelpStepUseVoltage currentStep = HelpStepUseVoltage.valueOf(currentStateOrdinal);
        return currentStep.retryStep().ordinal();
    }
    
    @Override
    public int getNextStateOrdinal(int currentStateOrdinal, boolean isSuc)
    {
        HelpStepUseVoltage currentStep = HelpStepUseVoltage.valueOf(currentStateOrdinal);
        return currentStep.nextStep(isSuc).ordinal();
    }
    
    @Override
    public String getStateInDetailed(int currentStateOrdinal)
    {
        return HelpStepUseVoltage.valueOf(currentStateOrdinal).getDetailedMessage();
    }
}
