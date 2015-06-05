package com.espressif.iot.ui.softap_sta_support.help;

import com.espressif.iot.R;
import com.espressif.iot.help.ui.IEspHelpUISSSUseDevice;
import com.espressif.iot.model.help.statemachine.EspHelpStateMachine;
import com.espressif.iot.type.help.HelpStepSSSUseDevice;
import com.espressif.iot.ui.softap_sta_support.SSSFragmentConfigure;

import android.os.Bundle;

public class HelpSSSFragmentConfigure extends SSSFragmentConfigure implements IEspHelpUISSSUseDevice
{
    private HelpSoftApStaSupportActivity mActivity;
    private EspHelpStateMachine mHelpMachine;
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        
        mActivity = (HelpSoftApStaSupportActivity)getActivity();
        mHelpMachine = EspHelpStateMachine.getInstance();
    }
    
    @Override
    protected void checkHelpTransformNext(boolean suc, int transformCount)
    {
        if (mHelpMachine.isHelpModeUseSSSDevice())
        {
            for (int i = 0; i < transformCount; i++)
            {
                mHelpMachine.transformState(suc);
            }
            onHelpUseSSSDevice();
        }
    }
    
    @Override
    protected boolean checkHelpModeUse()
    {
        return mHelpMachine.isHelpModeUseSSSDevice();
    }
    
    @Override
    public void onHelpUseSSSDevice()
    {
        mActivity.clearHelpContainer();
        
        HelpStepSSSUseDevice step = HelpStepSSSUseDevice.valueOf(mHelpMachine.getCurrentStateOrdinal());
        switch(step)
        {
            case START_USE_HELP:
                break;
            case FAIL_FOUND_DEVICE:
                break;
            case START_DIRECT_CONNECT:
                break;
            case FIND_SOFTAP:
                mSoftApList.clear();
                mSoftAPAdapter.notifyDataSetChanged();
                mActivity.highlightHelpView(mListView);
                mActivity.gestureAnimHint(R.string.esp_sss_help_use_device_find_softap_msg,
                    R.anim.esp_pull_to_refresh_hint);
                break;
            case FOUND_SOFTAP_FAILED:
                mActivity.highlightHelpView(mListView);
                mHelpMachine.retry();
                mActivity.gestureAnimHint(R.string.esp_sss_help_use_device_found_softap_failed_msg,
                    R.anim.esp_pull_to_refresh_hint);
                break;
            case SELECT_SOFTAP:
                mActivity.highlightHelpView(mListView);
                mActivity.setHelpHintMessage(R.string.esp_sss_help_use_device_select_softap_msg);
                break;
            case CONNECT_SOFTAP_FAILED:
                mHelpMachine.retry();
                mActivity.highlightHelpView(mListView);
                mActivity.setHelpHintMessage(R.string.esp_sss_help_use_device_connect_softap_failed_msg);
                break;
            case SOFTAP_NOT_SUPPORT:
                mHelpMachine.retry();
                mActivity.highlightHelpView(mListView);
                mActivity.setHelpHintMessage(R.string.esp_sss_help_use_device_not_support_msg);
                break;
            case DEVICE_SELECT:
                mActivity.onHelpUseSSSDevice();
                break;
            case DEVICE_CONTROL:
                break;
            case DEVICE_CONTROL_FAILED:
                break;
            case SUC:
                break;
        }
        
    }
}
