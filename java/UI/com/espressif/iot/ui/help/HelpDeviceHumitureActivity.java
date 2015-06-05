package com.espressif.iot.ui.help;

import com.espressif.iot.R;
import com.espressif.iot.help.ui.IEspHelpUIUseHumiture;
import com.espressif.iot.type.help.HelpStepUseHumiture;
import com.espressif.iot.ui.device.DeviceHumitureActivity;

public class HelpDeviceHumitureActivity extends DeviceHumitureActivity implements IEspHelpUIUseHumiture
{
    @Override
    protected void checkHelpModeDevice(boolean compatibility)
    {
        if (mHelpMachine.isHelpModeUseHumiture())
        {
            mHelpMachine.transformState(compatibility);
            if (!compatibility)
            {
                onHelpUseHumiture();
            }
            // if compatibility is true, do onHelpUseHumiture() when first get data (onPostExecute in RefreshTask)
        }
    }
    
    @Override
    protected void checkHelpExecuteFinish(boolean suc)
    {
        if (mHelpMachine.isHelpModeUseHumiture())
        {
            onHelpUseHumiture();
            if (suc)
            {
                mHelpMachine.transformState(true);
                onHelpUseHumiture();
            }
            else if (isChartViewDrawn())
            {
                mHelpMachine.transformState(false);
                onHelpUseHumiture();
            }
        }
    }
    
    @Override
    protected boolean checkHelpIsChartDeviceHelp()
    {
        return mHelpMachine.isHelpModeUseHumiture();
    }
    
    @Override
    public void onHelpUseHumiture()
    {
        clearHelpContainer();
        
        HelpStepUseHumiture step = HelpStepUseHumiture.valueOf(mHelpMachine.getCurrentStateOrdinal());
        switch(step)
        {
            case START_USE_HELP:
                break;
            case FAIL_FOUND_HUMITURE:
                break;
            case HUMITURE_SELECT:
                break;
                
            case HUMITURE_NOT_COMPATIBILITY:
                mHelpMachine.exit();
                setResult(RESULT_EXIT_HELP_MODE);
                break;
            case PULL_DOWN_TO_REFRESH:
                highlightHelpView(mChartViewContainer);
                setHelpHintMessage(R.string.esp_help_use_humiture_pull_down_to_refresh_msg);
                break;
            case GET_DATA_FAILED:
                highlightHelpView(mChartViewContainer);
                setHelpHintMessage(R.string.esp_help_use_humiture_get_data_failed_msg);
                mHelpMachine.retry();
                break;
            case SELECT_DATE:
                highlightHelpView(getRightTitleIcon());
                setHelpHintMessage(R.string.esp_help_use_humiture_select_date_msg);
                break;
            case SELECT_DATE_FAILED:
                highlightHelpView(mChartViewContainer);
                setHelpHintMessage(R.string.esp_help_use_humiture_select_date_failed_msg);
                mHelpMachine.retry();
                break;
            case SUC:
                setHelpFrameDark();
                setHelpHintMessage(R.string.esp_help_use_humiture_success_msg);
                setHelpButtonVisible(HELP_BUTTON_EXIT, true);
                break;
        }
    }
}
