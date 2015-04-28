package com.espressif.iot.type.help;

import com.espressif.iot.help.statemachine.IEspHelpStep;

/*
 * 
 * more info please see IEspHelpStateMachine.java
 * 
 */
public enum HelpStepConfigure implements IEspHelpStep
{
    START_CONFIGURE_HELP(0, "0. start configure help: reminder user to tap the \"configure Button\""),
    DISCOVER_IOT_DEVICES(2, "2. discover iot devices(softap): discover iot devices by wifi scan"),
    FAIL_DISCOVER_IOT_DEVICES(3, "3. fail to discover the iot devices: reminder user to \"guarantee that "
            + "the wifi is open and the iot device could be scanned by wifi\""),
    SCAN_AVAILABLE_AP(4, "4. scan available AP: discover AP could be configured"),
    FAIL_DISCOVER_AP(5, "5. fail to discover the wifi: reminder user to \"guarantee that "
            + "at least one AP could be accessed to the Internet is on\""),
    SELECT_CONFIGURED_DEVICE(6, "6. fail to discover the wifi: reminder user to \"guarantee "
            + "that at least one AP could be accessed to the Internet is on\""),
    FAIL_CONNECT_DEVICE(7, "7. connect device fail: reminder user that \"the device con't be connected, please guarantee that "
            + "the device is on and try again\""),
    DEVICE_IS_ACTIVATING(8, "8. reminder user: \"ESP_XXX\" is activating on Server"),
    FAIL_CONNECT_AP(9, "9. fail to connect AP: reminder user that \"the AP's password maybe wrong\""),
    FAIL_ACTIVATE(11, "11. fail to activating: notify user \"fail to activating the iot device on Server: reminder user to guarantee that "
            + "the wifi is accessed to Internet\""),
    SUC(12, "12. configure suc: reminder user that \"the iot device is configured suc\"");
    
    private final int mStepValue;
    
    private final String mDetailedMessage;
    
    private HelpStepConfigure(int value, String detailedMessage)
    {
        this.mStepValue = value;
        this.mDetailedMessage = detailedMessage;
    }
    
    @Override
    public int getStepValue()
    {
        return this.mStepValue;
    }
    
    @Override
    public String getDetailedMessage()
    {
        return this.mDetailedMessage;
    }
    
    @Override
    public HelpStepConfigure retryStep()
    {
        HelpStepConfigure retryStep = null;
        switch (this)
        {
            // step 3 to step 2
            case FAIL_DISCOVER_IOT_DEVICES:
                retryStep = HelpStepConfigure.DISCOVER_IOT_DEVICES;
                break;
            // step 5 to step 4
            case FAIL_DISCOVER_AP:
                retryStep = HelpStepConfigure.SCAN_AVAILABLE_AP;
                break;
            // step 7 to step 2
            case FAIL_CONNECT_DEVICE:
                retryStep = HelpStepConfigure.DISCOVER_IOT_DEVICES;
                break;
            // step 9 to step 0
            case FAIL_CONNECT_AP:
                retryStep = HelpStepConfigure.START_CONFIGURE_HELP;
                break;
            // step 11 to step 0
            case FAIL_ACTIVATE:
                retryStep = HelpStepConfigure.START_CONFIGURE_HELP;
                break;
            case DEVICE_IS_ACTIVATING:
                break;
            case DISCOVER_IOT_DEVICES:
                break;
            case SCAN_AVAILABLE_AP:
                break;
            case SELECT_CONFIGURED_DEVICE:
                retryStep = HelpStepConfigure.SCAN_AVAILABLE_AP;
                break;
            case START_CONFIGURE_HELP:
                break;
            case SUC:
                break;
        }
        if (retryStep == null)
        {
            throw new IllegalStateException("step " + this.mStepValue + " don't support retry");
        }
        else
        {
            return retryStep;
        }
    }
    
    private HelpStepConfigure __nextStepSuc()
    {
        if (mStepValue % 2 != 0)
        {
            throw new IllegalStateException("before call nextStep(), the mStepValue should be even");
        }
        HelpStepConfigure nextStep = null;
        for (int nextStepValue = mStepValue + 2; nextStepValue <= SUC.getStepValue(); nextStepValue += 2)
        {
            nextStep = valueOfStep(nextStepValue);
            if (nextStep != null)
            {
                return nextStep;
            }
        }
        throw new IllegalStateException("after call nextStep(), the nextStep is null");
    }
    
    private HelpStepConfigure __nextStepFail()
    {
        /**
         * Most time step is +1, except the step FAIL_CONNECT_AP(9)'s next fail step is FAIL_ACTIVATE(10).
         * For the step DEVICE_IS_ACTIVATING(8) could produce 2 problems.
         * At now, the situation is not common, so we adopt the strange processing methods
         */
        if (mStepValue != FAIL_CONNECT_AP.getStepValue())
        {
            return valueOfStep(mStepValue + 1);
        }
        else
        {
            return valueOfStep(mStepValue + 2);
        }
    }
    
    @Override
    public HelpStepConfigure nextStep(boolean suc)
    {
        if (suc)
        {
            return __nextStepSuc();
        }
        else
        {
            return __nextStepFail();
        }
    }
    
    @Override
    public HelpStepConfigure valueOfStep(int stepValue)
    {
        for (int i = 0; i < values().length; i++)
        {
            if (values()[i].mStepValue == stepValue)
            {
                return values()[i];
            }
        }
        return null;
    }
    
    /**
     * get enum by its ordinal
     * 
     * @param ordinal the ordinal of the enum
     * @return
     */
    public static HelpStepConfigure valueOf(int ordinal)
    {
        if (ordinal < 0 || ordinal >= values().length)
        {
            throw new IndexOutOfBoundsException("Invalid ordinal");
        }
        return values()[ordinal];
    }
}
