package com.espressif.iot.ui.help;

import java.lang.ref.WeakReference;

import com.espressif.iot.R;
import com.espressif.iot.help.ui.IEspHelpUIUsePlugs;
import com.espressif.iot.type.help.HelpStepUsePlug;
import com.espressif.iot.ui.device.DevicePlugsActivity;

import android.os.Handler;
import android.os.Message;

public class HelpDevicePlugsActivity extends DevicePlugsActivity implements IEspHelpUIUsePlugs
{
    @Override
    protected void checkHelpModePlugs(boolean compatibility)
    {
        if (mHelpMachine.isHelpModeUsePlugs())
        {
            mHelpMachine.transformState(compatibility);
            HelpHandler helpHandler = new HelpHandler(this);
            helpHandler.sendEmptyMessageDelayed(0, 100);
        }
    }
    
    @Override
    protected void checkHelpExecuteFinish(int command, boolean result)
    {
        if (mHelpMachine.isHelpModeUsePlugs() && command == COMMAND_POST)
        {
            mHelpMachine.transformState(true);
            onHelpUsePlugs();
        }
    }
    
    @Override
    public void onHelpUsePlugs()
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
                highlightHelpView(mApertureListView);
                setHelpHintMessage(R.string.esp_help_use_plugs_tap_icon_msg);
                break;
            case PLUG_CONTROL_FAILED:
                highlightHelpView(mApertureListView);
                setHelpHintMessage(R.string.esp_help_use_plugs_control_failed_msg);
                mHelpMachine.retry();
                break;
            case SUC:
                setHelpFrameDark();
                setHelpHintMessage(R.string.esp_help_use_plugs_success_msg);
                setHelpButtonVisible(HELP_BUTTON_EXIT, true);
                break;
        }
    }
    
    private static class HelpHandler extends Handler
    {
        private WeakReference<HelpDevicePlugsActivity> mActivity;
        
        public HelpHandler(HelpDevicePlugsActivity activity)
        {
            mActivity = new WeakReference<HelpDevicePlugsActivity>(activity);
        }
        
        @Override
        public void handleMessage(Message msg)
        {
            HelpDevicePlugsActivity activity = mActivity.get();
            if (activity == null)
            {
                return;
            }
            
            activity.onHelpUsePlugs();
        }
    }
}
