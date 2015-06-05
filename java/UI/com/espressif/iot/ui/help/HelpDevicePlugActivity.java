package com.espressif.iot.ui.help;

import com.espressif.iot.R;
import com.espressif.iot.help.ui.IEspHelpUIUsePlug;
import com.espressif.iot.type.help.HelpStepUsePlug;
import com.espressif.iot.ui.device.DevicePlugActivity;

public class HelpDevicePlugActivity extends DevicePlugActivity implements IEspHelpUIUsePlug
{
    @Override
    protected void checkHelpModePlug(boolean compatibility)
    {
        if (mHelpMachine.isHelpModeUsePlug())
        {
            mHelpMachine.transformState(compatibility);
            onHelpUsePlug();
        }
    }
    
    @Override
    protected void checkHelpExecuteFinish(int command, boolean result)
    {
        if (mHelpMachine.isHelpModeUsePlug() && command == COMMAND_POST)
        {
            mHelpMachine.transformState(true);
            onHelpUsePlug();
        }
    }
    
    @Override
    public void onHelpUsePlug()
    {
        clearHelpContainer();
        
        HelpStepUsePlug step = HelpStepUsePlug.valueOf(mHelpMachine.getCurrentStateOrdinal());
        switch (step)
        {
            case START_USE_HELP:
            case FAIL_FOUND_PLUG:
            case FIND_ONLINE:
            case NO_PLUG_ONLINE:
            case PLUG_SELECT:
                break;
                
            case PLUG_NOT_COMPATIBILITY:
                mHelpMachine.exit();
                setResult(RESULT_EXIT_HELP_MODE);
                break;
            case PLUG_CONTROL:
                highlightHelpView(mPlugSwitch);
                setHelpHintMessage(R.string.esp_help_use_plug_tap_icon_msg);
                break;
            case PLUG_CONTROL_FAILED:
                highlightHelpView(mPlugSwitch);
                setHelpHintMessage(R.string.esp_help_use_plug_control_failed_msg);
                mHelpMachine.retry();
                break;
            case SUC:
                setHelpFrameDark();
                setHelpHintMessage(R.string.esp_help_use_plug_success_msg);
                setHelpButtonVisible(HELP_BUTTON_EXIT, true);
                break;
        }
    }
}
