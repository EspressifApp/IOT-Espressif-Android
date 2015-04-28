package com.espressif.iot.model.help.statemachine;

import com.espressif.iot.help.statemachine.IEspHelpHandler;
import com.espressif.iot.object.IEspSingletonObject;
import com.espressif.iot.type.help.HelpStepConfigure;

public class EspHelpConfigureHandler implements IEspHelpHandler, IEspSingletonObject
{
    
    /*
     * Singleton lazy initialization start
     */
    private EspHelpConfigureHandler()
    {
    }
    
    private static class InstanceHolder
    {
        static EspHelpConfigureHandler instance = new EspHelpConfigureHandler();
    }
    
    public static EspHelpConfigureHandler getInstance()
    {
        return InstanceHolder.instance;
    }
    
    /*
     * Singleton lazy initialization end
     */
    
    @Override
    public int getRetryStateOrdinal(int currentStateOrdinal)
    {
        HelpStepConfigure currentStep = HelpStepConfigure.valueOf(currentStateOrdinal);
        return currentStep.retryStep().ordinal();
    }
    
    @Override
    public int getNextStateOrdinal(int currentStateOrdinal, boolean isSuc)
    {
        HelpStepConfigure currentStep = HelpStepConfigure.valueOf(currentStateOrdinal);
        return currentStep.nextStep(isSuc).ordinal();
    }
    
    @Override
    public String getStateInDetailed(int currentStateOrdinal)
    {
        return HelpStepConfigure.valueOf(currentStateOrdinal).getDetailedMessage();
    }
    
}
