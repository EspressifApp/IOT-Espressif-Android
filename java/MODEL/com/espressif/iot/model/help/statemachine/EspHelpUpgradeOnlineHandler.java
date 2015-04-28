package com.espressif.iot.model.help.statemachine;

import com.espressif.iot.help.statemachine.IEspHelpHandler;
import com.espressif.iot.object.IEspSingletonObject;
import com.espressif.iot.type.help.HelpStepUpgradeOnline;

public class EspHelpUpgradeOnlineHandler implements IEspHelpHandler, IEspSingletonObject
{
    private EspHelpUpgradeOnlineHandler()
    {
    }
    
    private static class InstanceHolder
    {
        static EspHelpUpgradeOnlineHandler instance = new EspHelpUpgradeOnlineHandler();
    }
    
    public static EspHelpUpgradeOnlineHandler getInstance()
    {
        return InstanceHolder.instance;
    }
    
    @Override
    public int getRetryStateOrdinal(int currentStateOrdinal)
    {
        HelpStepUpgradeOnline currentStep = HelpStepUpgradeOnline.valueOf(currentStateOrdinal);
        return currentStep.retryStep().ordinal();
    }
    
    @Override
    public int getNextStateOrdinal(int currentStateOrdinal, boolean isSuc)
    {
        HelpStepUpgradeOnline currentStep = HelpStepUpgradeOnline.valueOf(currentStateOrdinal);
        return currentStep.nextStep(isSuc).ordinal();
    }
    
    @Override
    public String getStateInDetailed(int currentStateOrdinal)
    {
        return HelpStepUpgradeOnline.valueOf(currentStateOrdinal).getDetailedMessage();
    }
}
