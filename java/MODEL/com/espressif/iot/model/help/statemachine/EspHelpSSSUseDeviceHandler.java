package com.espressif.iot.model.help.statemachine;

import com.espressif.iot.help.statemachine.IEspHelpHandler;
import com.espressif.iot.object.IEspSingletonObject;
import com.espressif.iot.type.help.HelpStepSSSUseDevice;

public class EspHelpSSSUseDeviceHandler implements IEspHelpHandler, IEspSingletonObject
{
    private EspHelpSSSUseDeviceHandler()
    {
    }
    
    private static class InstanceHolder
    {
        static EspHelpSSSUseDeviceHandler instance = new EspHelpSSSUseDeviceHandler();
    }
    
    public static EspHelpSSSUseDeviceHandler getInstance()
    {
        return InstanceHolder.instance;
    }
    
    @Override
    public int getRetryStateOrdinal(int currentStateOrdinal)
    {
        HelpStepSSSUseDevice currentStep = HelpStepSSSUseDevice.valueOf(currentStateOrdinal);
        return currentStep.retryStep().ordinal();
    }
    
    @Override
    public int getNextStateOrdinal(int currentStateOrdinal, boolean isSuc)
    {
        HelpStepSSSUseDevice currentStep = HelpStepSSSUseDevice.valueOf(currentStateOrdinal);
        return currentStep.nextStep(isSuc).ordinal();
    }
    
    @Override
    public String getStateInDetailed(int currentStateOrdinal)
    {
        return HelpStepSSSUseDevice.valueOf(currentStateOrdinal).getDetailedMessage();
    }
}
