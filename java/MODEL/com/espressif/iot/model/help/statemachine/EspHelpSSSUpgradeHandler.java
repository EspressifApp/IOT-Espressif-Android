package com.espressif.iot.model.help.statemachine;

import com.espressif.iot.help.statemachine.IEspHelpHandler;
import com.espressif.iot.object.IEspSingletonObject;
import com.espressif.iot.type.help.HelpStepSSSUpgrade;

public class EspHelpSSSUpgradeHandler implements IEspHelpHandler, IEspSingletonObject
{
    private EspHelpSSSUpgradeHandler()
    {
    }
    
    private static class InstanceHolder
    {
        static EspHelpSSSUpgradeHandler instance = new EspHelpSSSUpgradeHandler();
    }
    
    public static EspHelpSSSUpgradeHandler getInstance()
    {
        return InstanceHolder.instance;
    }
    
    @Override
    public int getRetryStateOrdinal(int currentStateOrdinal)
    {
        HelpStepSSSUpgrade currentStep = HelpStepSSSUpgrade.valueOf(currentStateOrdinal);
        return currentStep.retryStep().ordinal();
    }
    
    @Override
    public int getNextStateOrdinal(int currentStateOrdinal, boolean isSuc)
    {
        HelpStepSSSUpgrade currentStep = HelpStepSSSUpgrade.valueOf(currentStateOrdinal);
        return currentStep.nextStep(isSuc).ordinal();
    }
    
    @Override
    public String getStateInDetailed(int currentStateOrdinal)
    {
        return HelpStepSSSUpgrade.valueOf(currentStateOrdinal).getDetailedMessage();
    }
}
