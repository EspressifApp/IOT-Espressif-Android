package com.espressif.iot.ui.help;

import com.espressif.iot.R;
import com.espressif.iot.help.ui.IEspHelpUIUseLight;
import com.espressif.iot.type.help.HelpStepUseLight;
import com.espressif.iot.ui.device.DeviceLightActivity;

public class HelpDeviceLightActivity extends DeviceLightActivity implements IEspHelpUIUseLight
{
    
    @Override
    protected void checkHelpModeLight(boolean compatibility)
    {
        if (mHelpMachine.isHelpModeUseLight())
        {
            mHelpMachine.transformState(compatibility);
            onHelpUseLight();
        }

    }
    
    @Override
    protected void checkHelpExecuteFinish(int command, boolean result)
    {
        if (mHelpMachine.isHelpModeUseLight() && command == COMMAND_POST)
        {
            mHelpMachine.transformState(result);
            onHelpUseLight();
        }
    }
    
    @Override
    public void onHelpUseLight()
    {
        clearHelpContainer();
        
        HelpStepUseLight step = HelpStepUseLight.valueOf(mHelpMachine.getCurrentStateOrdinal());
        switch (step)
        {
            case START_USE_HELP:
            case FAIL_FOUND_LIGHT:
            case FIND_ONLINE:
            case NO_LIGHT_ONLINE:
            case LIGHT_SELECT:
                break;
                
            case LIGHT_NOT_COMPATIBILITY:
                mHelpMachine.exit();
                setResult(RESULT_EXIT_HELP_MODE);
                break;
            case LIGHT_CONTROL:
                highlightHelpView(mLightLayout);
                setHelpHintMessage(R.string.esp_help_use_light_control_msg);
                break;
            case LIGHT_CONTROL_FAILED:
                highlightHelpView(mLightLayout);
                setHelpHintMessage(R.string.esp_help_use_light_control_failed_msg);
                mHelpMachine.retry();
                break;
            case SUC:
                setHelpFrameDark();
                setHelpHintMessage(R.string.esp_help_use_light_success_msg);
                setHelpButtonVisible(HELP_BUTTON_EXIT, true);
                break;
        }
    }
}
