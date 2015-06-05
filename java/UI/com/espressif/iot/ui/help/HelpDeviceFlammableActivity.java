package com.espressif.iot.ui.help;

import com.espressif.iot.R;
import com.espressif.iot.help.ui.IEspHelpUIUseFlammable;
import com.espressif.iot.type.help.HelpStepUseFlammable;
import com.espressif.iot.ui.device.DeviceFlammableActivity;

public class HelpDeviceFlammableActivity extends DeviceFlammableActivity implements IEspHelpUIUseFlammable
{
    @Override
    protected void checkHelpModeDevice(boolean compatibility)
    {
        if (mHelpMachine.isHelpModeUseFlammable())
        {
            mHelpMachine.transformState(compatibility);
            if (!compatibility)
            {
                onHelpUseFlammable();
            }
            // if compatibility is true, do onHelpUseFlammable() when first get data (onPostExecute in RefreshTask)
        }
    }
    
    @Override
    protected void checkHelpExecuteFinish(boolean suc)
    {
        if (mHelpMachine.isHelpModeUseFlammable())
        {
            onHelpUseFlammable();
            if (!suc)
            {
                mHelpMachine.transformState(false);
                onHelpUseFlammable();
            }
            else if (isChartViewDrawn())
            {
                mHelpMachine.transformState(true);
                onHelpUseFlammable();
            }
        }
    }
    
    @Override
    protected boolean checkHelpIsChartDeviceHelp()
    {
        return mHelpMachine.isHelpModeUseFlammable();
    }
    
    @Override
    public void onHelpUseFlammable()
    {
        clearHelpContainer();
        
        HelpStepUseFlammable step = HelpStepUseFlammable.valueOf(mHelpMachine.getCurrentStateOrdinal());
        switch(step)
        {
            case START_USE_HELP:
                break;
            case FAIL_FOUND_FLAMMABLE:
                break;
            case FLAMMABLE_SELECT:
                break;
                
            case FLAMMABLE_NOT_COMPATIBILITY:
                mHelpMachine.exit();
                setResult(RESULT_EXIT_HELP_MODE);
                break;
            case PULL_DOWN_TO_REFRESH:
                highlightHelpView(mChartViewContainer);
                setHelpHintMessage(R.string.esp_help_use_flammable_pull_down_to_refresh_msg);
                break;
            case GET_DATA_FAILED:
                highlightHelpView(mChartViewContainer);
                setHelpHintMessage(R.string.esp_help_use_flammable_get_data_failed_msg);
                mHelpMachine.retry();
                break;
            case SELECT_DATE:
                highlightHelpView(getRightTitleIcon());
                setHelpHintMessage(R.string.esp_help_use_flammable_select_date_msg);
                break;
            case SELECT_DATE_FAILED:
                highlightHelpView(mChartViewContainer);
                setHelpHintMessage(R.string.esp_help_use_flammable_select_date_failed_msg);
                mHelpMachine.retry();
                break;
            case SUC:
                setHelpFrameDark();
                setHelpHintMessage(R.string.esp_help_use_flammable_success_msg);
                setHelpButtonVisible(HELP_BUTTON_EXIT, true);
                break;
        }
    }
}
