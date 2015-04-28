package com.espressif.iot.help.statemachine;

public interface IEspHelpHandler
{
    /**
     * Get the state ordinal when encountered some problem, user would like to try again
     */
    int getRetryStateOrdinal(int currentStateOrdinal);
    
    /**
     * Get the next state's ordinal
     * 
     * @param currentStateOrdinal current state in detailed String
     * @param isSuc whether the action is suc
     */
    int getNextStateOrdinal(int currentStateOrdinal, boolean isSuc);
    
    /**
     * Get current state in detailed String
     * 
     * @return current state in detailed String
     */
    String getStateInDetailed(int currentStateOrdinal);
}
