package com.espressif.iot.ui.help;

import com.espressif.iot.R;
import com.espressif.iot.help.ui.IEspHelpUIUseVoltage;
import com.espressif.iot.type.help.HelpStepUseVoltage;
import com.espressif.iot.ui.device.DeviceVoltageActivity;

public class HelpDeviceVoltageActivity extends DeviceVoltageActivity implements IEspHelpUIUseVoltage
{
    @Override
    protected void checkHelpModeDevice(boolean compatibility)
    {
        if (mHelpMachine.isHelpModeUseVoltage())
        {
            mHelpMachine.transformState(compatibility);
            if (!compatibility)
            {
                onHelpUseVoltage();
            }
            // if compatibility is true, do onHelpUseVoltage() when first get data (onPostExecute in RefreshTask)
        }
    }
    
    @Override
    protected void checkHelpExecuteFinish(boolean suc)
    {
        if (mHelpMachine.isHelpModeUseVoltage())
        {
            onHelpUseVoltage();
            if (!suc)
            {
                mHelpMachine.transformState(false);
                onHelpUseVoltage();
            }
            else if (isChartViewDrawn())
            {
                mHelpMachine.transformState(true);
                onHelpUseVoltage();
            }
        }
    }
    
    @Override
    protected boolean checkHelpIsChartDeviceHelp()
    {
        return mHelpMachine.isHelpModeUseVoltage();
    }
    
    @Override
    public void onHelpUseVoltage()
    {
        clearHelpContainer();
        
        HelpStepUseVoltage step = HelpStepUseVoltage.valueOf(mHelpMachine.getCurrentStateOrdinal());
        switch(step)
        {
            case START_USE_HELP:
                break;
            case FAIL_FOUND_VOLTAGE:
                break;
            case VOLTAGE_SELECT:
                break;
                
            case VOLTAGE_NOT_COMPATIBILITY:
                mHelpMachine.exit();
                setResult(RESULT_EXIT_HELP_MODE);
                break;
            case PULL_DOWN_TO_REFRESH:
                highlightHelpView(mChartViewContainer);
                setHelpHintMessage(R.string.esp_help_use_voltage_pull_down_to_refresh_msg);
                break;
            case GET_DATA_FAILED:
                highlightHelpView(mChartViewContainer);
                setHelpHintMessage(R.string.esp_help_use_voltage_get_data_failed_msg);
                mHelpMachine.retry();
                break;
            case SELECT_DATE:
                highlightHelpView(getRightTitleIcon());
                setHelpHintMessage(R.string.esp_help_use_voltage_select_date_msg);
                break;
            case SELECT_DATE_FAILED:
                highlightHelpView(mChartViewContainer);
                setHelpHintMessage(R.string.esp_help_use_voltage_select_date_failed_msg);
                mHelpMachine.retry();
                break;
            case SUC:
                setHelpFrameDark();
                setHelpHintMessage(R.string.esp_help_use_voltage_success_msg);
                setHelpButtonVisible(HELP_BUTTON_EXIT, true);
                break;
        }
    }
}
