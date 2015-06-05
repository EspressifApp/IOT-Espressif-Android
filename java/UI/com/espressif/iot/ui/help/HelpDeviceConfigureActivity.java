package com.espressif.iot.ui.help;

import java.lang.ref.WeakReference;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView.ScaleType;

import com.espressif.iot.R;
import com.espressif.iot.device.IEspDeviceNew;
import com.espressif.iot.help.ui.IEspHelpUIConfigure;
import com.espressif.iot.type.help.HelpStepConfigure;
import com.espressif.iot.ui.configure.ApInfo;
import com.espressif.iot.ui.configure.DeviceConfigureActivity;

public class HelpDeviceConfigureActivity extends DeviceConfigureActivity implements IEspHelpUIConfigure
{
    private HelpHandler mHelpHandler;
    
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        mHelpHandler = new HelpHandler(this);
        super.onCreate(savedInstanceState);
    }
    
    @Override
    protected void showConfigureSettingsDialog(IEspDeviceNew device)
    {
        new HelpDeviceConfigureSettingsDialog(this, device).show();
    }
    
    @Override
    public void showConfigureProgressDialog(IEspDeviceNew device, ApInfo apInfo)
    {
        new HelpDeviceConfigureProgressDialog(this, device, apInfo).show();
    }
    
    private static class HelpHandler extends Handler {
        private WeakReference<HelpDeviceConfigureActivity> mActivity;
        
        public HelpHandler(HelpDeviceConfigureActivity activity)
        {
            mActivity = new WeakReference<HelpDeviceConfigureActivity>(activity);
        }
        
        @Override
        public void handleMessage(Message msg)
        {
            HelpDeviceConfigureActivity activity = mActivity.get();
            if (activity == null)
            {
                return;
            }
            
            activity.onHelpConfigure();
        }
    }
    
    @Override
    public void onHelpConfigure()
    {
        highlightHelpView(mSoftApListView);
        
        HelpStepConfigure step = HelpStepConfigure.valueOf(mHelpMachine.getCurrentStateOrdinal());
        switch (step)
        {
            case START_CONFIGURE_HELP:
                break;
            case DISCOVER_IOT_DEVICES:
                mHelpMachine.transformState(!mSoftApList.isEmpty());
                mHelpHandler.sendEmptyMessage(RESULT_HELP_CONFIGURE);
                break;
            case FAIL_DISCOVER_IOT_DEVICES:
                hintDiscoverSoftAP();
                break;
            case SCAN_AVAILABLE_AP:
                setHelpHintMessage(R.string.esp_help_configure_select_softap_msg);
                break;
            case FAIL_DISCOVER_AP:
                setHelpHintMessage(R.string.esp_help_configure_discover_wifi_msg);
                mHelpMachine.retry();
                break;
            case SELECT_CONFIGURED_DEVICE:
                break;
            case FAIL_CONNECT_DEVICE:
                setHelpHintMessage(R.string.esp_help_configure_connect_device_failed_msg);
                setHelpButtonVisible(HELP_BUTTON_ALL, true);
                break;
            
            case DEVICE_IS_ACTIVATING:
            case FAIL_ACTIVATE:
            case FAIL_CONNECT_AP:
            case SUC:
                // Process in EspUIActivity, not here.
                break;
        }
    }
    
    private void hintDiscoverSoftAP()
    {
        ImageView hintView = new ImageView(this);
        hintView.setScaleType(ScaleType.CENTER_INSIDE);
        hintView.setImageResource(R.drawable.esp_pull_to_refresh_hint);
        FrameLayout.LayoutParams lp =
            new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, Gravity.CENTER);
        mHelpContainer.addView(hintView, lp);
        
        setHelpHintMessage(R.string.esp_help_configure_discover_softap_msg);
        
        Animation anim = AnimationUtils.loadAnimation(this, R.anim.esp_pull_to_refresh_hint);
        hintView.startAnimation(anim);
    }
    
    @Override
    public void onExitHelpMode()
    {
        setResult(RESULT_EXIT_HELP_MODE);
        finish();
    }
    
    @Override
    public void onHelpRetryClick()
    {
        onHelpConfigure();
    }
    
    @Override
    protected void checkHelpModeConfigureClear()
    {
        if (mHelpMachine.isHelpModeConfigure())
        {
            mSoftApList.clear();
            mSoftApAdapter.notifyDataSetInvalidated();
            
            mHelpHandler.sendEmptyMessage(RESULT_HELP_CONFIGURE);
        }
    }
    
    @Override
    protected void checkHelpModeConfigureRetry()
    {
        if (mHelpMachine.isHelpModeConfigure())
        {
            if (mHelpMachine.getCurrentStateOrdinal() <= HelpStepConfigure.FAIL_DISCOVER_IOT_DEVICES.ordinal())
            {
                mHelpMachine.retry();
                mHelpHandler.sendEmptyMessage(RESULT_HELP_CONFIGURE);
            }
        }
    }
    
    @Override
    protected boolean checkHelpPopMenuItemClick(int itemId)
    {
        if(mHelpMachine.isHelpModeConfigure())
        {
            switch(itemId)
            {
                case POPMENU_ID_DIRECT_CONNECT:
                case POPMENU_ID_MESH:
                    return true;
                default:
                    return false;
            }
        }
        else
        {
            return false;
        }
    }
}
