package com.espressif.iot.model.help.statemachine;

import com.espressif.iot.help.statemachine.IEspHelpHandler;
import com.espressif.iot.object.IEspSingletonObject;
import com.espressif.iot.type.help.HelpStepUseRemote;

public class EspHelpUseRemoteHandler implements IEspHelpHandler, IEspSingletonObject
{
    private EspHelpUseRemoteHandler()
    {
    }
    
    private static class InstanceHolder
    {
        static EspHelpUseRemoteHandler instance = new EspHelpUseRemoteHandler();
    }
    
    public static EspHelpUseRemoteHandler getInstance()
    {
        return InstanceHolder.instance;
    }
    
    @Override
    public int getRetryStateOrdinal(int currentStateOrdinal)
    {
        HelpStepUseRemote currentStep = HelpStepUseRemote.valueOf(currentStateOrdinal);
        return currentStep.retryStep().ordinal();
    }
    
    @Override
    public int getNextStateOrdinal(int currentStateOrdinal, boolean isSuc)
    {
        HelpStepUseRemote currentStep = HelpStepUseRemote.valueOf(currentStateOrdinal);
        return currentStep.nextStep(isSuc).ordinal();
    }
    
    @Override
    public String getStateInDetailed(int currentStateOrdinal)
    {
        return HelpStepUseRemote.valueOf(currentStateOrdinal).getDetailedMessage();
    }
    
}
