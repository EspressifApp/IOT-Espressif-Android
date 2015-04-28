package com.espressif.iot.model.help.statemachine;

import com.espressif.iot.help.statemachine.IEspHelpHandler;
import com.espressif.iot.object.IEspSingletonObject;
import com.espressif.iot.type.help.HelpStepUpgradeLocal;

public class EspHelpUpgradeLocalHandler implements IEspHelpHandler, IEspSingletonObject
{
    private EspHelpUpgradeLocalHandler()
    {
    }
    
    private static class InstanceHolder
    {
        static EspHelpUpgradeLocalHandler instance = new EspHelpUpgradeLocalHandler();
    }
    
    public static EspHelpUpgradeLocalHandler getInstance()
    {
        return InstanceHolder.instance;
    }
    
    @Override
    public int getRetryStateOrdinal(int currentStateOrdinal)
    {
        HelpStepUpgradeLocal currentStep = HelpStepUpgradeLocal.valueOf(currentStateOrdinal);
        return currentStep.retryStep().ordinal();
    }
    
    @Override
    public int getNextStateOrdinal(int currentStateOrdinal, boolean isSuc)
    {
        HelpStepUpgradeLocal currentStep = HelpStepUpgradeLocal.valueOf(currentStateOrdinal);
        return currentStep.nextStep(isSuc).ordinal();
    }
    
    @Override
    public String getStateInDetailed(int currentStateOrdinal)
    {
        return HelpStepUpgradeLocal.valueOf(currentStateOrdinal).getDetailedMessage();
    }
}
