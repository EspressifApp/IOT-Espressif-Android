package com.espressif.iot.help.statemachine;

public interface IEspHelpStep
{
    /**
     * Get the step value
     * 
     * @return the stepValue
     */
    int getStepValue();
    
    /**
     * Get the detailed message of the step
     * 
     * @return the detailed message of the step
     */
    String getDetailedMessage();
    
    /**
     * 
     * @return the retry Step
     */
    Enum<?> retryStep();
    
    /**
     * 
     * @return the next Step
     */
    Enum<?> nextStep(boolean suc);
    
    /**
     * Get the Enum with step
     * 
     * @param stepValue
     * @return the Enum
     */
    Enum<?> valueOfStep(int stepValue);
}
