package com.espressif.iot.help.statemachine;

import com.espressif.iot.type.help.HelpType;

/*
 * even number means normal state
 * odd number means abnormal state
 * 
 * Configure Step:
 * 
 * UI don't tell the state change to IEspHelpStateMachine
 * step 8 ,step 9 and step 11 are changed by IEspDeviceStateMachine
 * 
 * 
 * UI should tell the IEspHelpStateMachine which Ap is selected by IUser
 * 
 * steps:
 * 
 * user tap configure help
 * 0. start configure help: reminder user to tap the "configure Button"
 * user tap "configure Button"
 * 2. discover iot devices(softap): discover iot devices by wifi scan
 * 3. fail to discover the iot devices: reminder user to "guarantee that the wifi is open and the iot device could be scanned by wifi"
 * 3.A if user tap "Try again", go to step 2
 * 3.B if user tap "I give up", configure help EXIT
 * 4. scan available AP: discover AP could be configured
 * 5. fail to discover the wifi: reminder user to "guarantee that at least one AP could be accessed to the Internet is on"
 * 5.A if user tap "Try again", go to step 2
 * 5.B if user tap "I give up", configure help EXIT
 * 6. select iot device: reminder user to "select the device to be configured"
 * user tap one of the iot devices
 * 7. connect device fail: reminder user that "the device con't be connected, please guarantee that the device is on and try again"
 * 7.A if user tap "Try again", go to step 2
 * 7.B if user tap "I give up", configure help EXIT
 * 8. reminder user: "ESP_XXX" is activating on Server
 * 9. fail to connect AP: reminder user that "the AP's password maybe wrong"
 * 9.A if user tap "Try again", go to step 0
 * 9.B if user tap "I give up", configure help EXIT
 * 11. fail to activating: notify user "fail to activating the iot device on Server: reminder user to guarantee that the wifi is accessed to Internet" 
 * 11.A if user tap "Try again", go to step 0
 * 11.B if user tap "I give up", configure help EXIT
 * 12. configure suc: reminder user that "the iot device is configured suc"
 * user tap "I know", clearIsHelpOpen(), configure help EXIT
 */

public interface IEspHelpStateMachine
{
    
    /**
     * Check whether the IEspHelpStateMachine is on
     * 
     * @return whether the IEspHelpStateMachine is on
     */
    boolean isHelpOn();
    
    /**
     * Transform the IEspHelpStateMachine state
     * 
     * @param isSuc whether the action is suc
     */
    void transformState(boolean isSuc);
    
    /**
     * Transform the direct state
     * 
     * @param state
     */
    void transformState(Enum<?> state);
    
    /**
     * When IUser start to use IEspHelpStateMachine
     */
    void start(HelpType type);
    
    /**
     * When encountered some problem, try again
     */
    void retry();
    
    /**
     * When encountered some problem, exit the help mode
     */
    void exit();
    
    /**
     * Get current help mode type
     * 
     * @return current help mode type
     */
    HelpType getCurrentType();
    
    /**
     * Get current state value
     * 
     * @return the current state value
     */
    int getCurrentStateOrdinal();
    
    /**
     * Get current state in detailed String
     * 
     * @return current state in detailed String
     */
    String getCurretStateInDetailed();
    
    /**
     * Get current device's bssid to be used by help mode
     * 
     * @return current device's bssid to be used by help mode
     */
    String __getCurrentDeviceBssid();
    
    /**
     * Set current device's bssid to be used by help mode
     * 
     * @param device's bssid current device to be used by help mode
     */
    void __setCurrentDeviceBssid(final String bssid);
    
    /**
     * Set the Ap's ssid which is selected by user
     * 
     * @param apSsid the Ap's ssid which is selected by user
     */
    void setConnectedApSsid(String apSsid);
    
    /**
     * Get the Ap's ssid which is selected by user
     * 
     * @return the Ap's ssid which is selected by user
     */
    String getConnectedApSsid();
    
    /**
     * Set the selection in the ListView
     * 
     * @param selection
     */
    void setDeviceSelection(int selection);
    
    /**
     * Get current selection in the ListView
     * 
     * @return
     */
    int getDeviceSelection();
    
    /**
     * Reset the selection value
     * 
     * @return
     */
    int resetDeviceSelection();
    
    boolean isHelpModeConfigure();
    
    boolean isHelpModeUsePlug();
    
    boolean isHelpModeUsePlugs();
    
    boolean isHelpModeUseLight();
    
    boolean isHelpModeUseHumiture();
    
    boolean isHelpModeUseFlammable();
    
    boolean isHelpModeUseVoltage();
    
    boolean isHelpModeUseRemote();
    
    boolean isHelpModeUpgradeLocal();
    
    boolean isHelpModeUpgradeOnline();
    
    boolean isHelpModeUseSSSDevice();
    
    boolean isHelpModeSSSUpgrade();
}
