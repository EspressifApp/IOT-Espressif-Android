package com.espressif.iot.ui.softap_sta_support.help;

import java.lang.ref.WeakReference;

import com.espressif.iot.R;
import com.espressif.iot.help.IEspHelpUISSSMeshConfigure;
import com.espressif.iot.help.ui.IEspHelpUISSSUseDevice;
import com.espressif.iot.type.help.HelpStepSSSMeshConfigure;
import com.espressif.iot.type.help.HelpStepSSSUseDevice;
import com.espressif.iot.type.help.HelpType;
import com.espressif.iot.ui.softap_sta_support.SoftApStaSupportActivity;
import com.espressif.iot.ui.softap_sta_support.help.SSSHelpActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView.ScaleType;
import android.widget.RadioGroup.OnCheckedChangeListener;

public class HelpSoftApStaSupportActivity extends SoftApStaSupportActivity implements OnCheckedChangeListener,
    IEspHelpUISSSUseDevice, IEspHelpUISSSMeshConfigure
{
    private HelpSSSFragmentDevices mHelpFragmentDevice;
    private HelpSSSFragmentConfigure mHelpFragmentConfig;
    
    private Handler mHelpHandler;
    private final static int REQUEST_HELP = 0;
    
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        mHelpHandler = new HelpHandler(this);
        setTitleRightIcon(R.drawable.esp_icon_help);
    }
    
    @Override
    protected void initPagerFragments()
    {
        mFragmentDevice = new HelpSSSFragmentDevices();
        mFragmentConfig = new HelpSSSFragmentConfigure();
        
        mHelpFragmentDevice = (HelpSSSFragmentDevices)mFragmentDevice;
        mHelpFragmentConfig = (HelpSSSFragmentConfigure)mFragmentConfig;
    }
    
    @Override
    protected void onTitleRightIconClick()
    {
        startActivityForResult(new Intent(this, SSSHelpActivity.class), REQUEST_HELP);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == REQUEST_HELP)
        {
            switch(resultCode)
            {
                case RESULT_HELP_SSS_USE_DEVICE:
                    mViewPager.setCurrentItem(FRAGMENT_DEVICE);
                    mViewPager.setInterceptTouchEvent(false);
                    mHelpHandler.sendEmptyMessage(RESULT_HELP_SSS_USE_DEVICE);
                    break;
                case RESULT_HELP_SSS_UPGRADE:
                    mViewPager.setCurrentItem(FRAGMENT_DEVICE);
                    mViewPager.setInterceptTouchEvent(false);
                    mHelpHandler.sendEmptyMessage(RESULT_HELP_SSS_UPGRADE);
                    break;
                case RESULT_HELP_SSS_MESH_CONFIGURE:
                    mViewPager.setCurrentItem(FRAGMENT_DEVICE);
                    mViewPager.setInterceptTouchEvent(false);
                    mHelpHandler.sendEmptyMessage(RESULT_HELP_SSS_MESH_CONFIGURE);
                    break;
            }
        }
    }
    
    @Override
    public void onExitHelpMode()
    {
        clearHelpContainer();
        mViewPager.setInterceptTouchEvent(true);
    }
    
    @Override
    public void onHelpRetryClick()
    {
        if (mHelpMachine.isHelpModeUseSSSDevice())
        {
            mHelpFragmentDevice.onHelpUseSSSDevice();
        }
    }
    
    @Override
    public void onHelpNextClick()
    {
        if (mHelpMachine.isHelpModeUseSSSDevice())
        {
            HelpStepSSSUseDevice step = HelpStepSSSUseDevice.valueOf(mHelpMachine.getCurrentStateOrdinal());
            if (step == HelpStepSSSUseDevice.START_USE_HELP)
            {
                mHelpMachine.transformState(true);
                onHelpUseSSSDevice();
            }
        }
    }

    public void gestureAnimHint(int hintMsgRes, int animRes)
    {
        ImageView hintView = new ImageView(this);
        hintView.setScaleType(ScaleType.CENTER_INSIDE);
        hintView.setImageResource(R.drawable.esp_pull_to_refresh_hint);
        FrameLayout.LayoutParams lp =
            new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, Gravity.CENTER);
        mHelpContainer.addView(hintView, lp);
        
        setHelpHintMessage(hintMsgRes);
        
        Animation anim = AnimationUtils.loadAnimation(this, animRes);
        hintView.startAnimation(anim);
    }
    
    @Override
    public void onHelpUseSSSDevice()
    {
        clearHelpContainer();
        
        HelpStepSSSUseDevice step = HelpStepSSSUseDevice.valueOf(mHelpMachine.getCurrentStateOrdinal());
        switch(step)
        {
            case START_DIRECT_CONNECT:
                highlightHelpView(findViewById(R.id.pager_tab_configure));
                setHelpHintMessage(R.string.esp_sss_help_use_device_click_configure_msg);
                break;
            case DEVICE_SELECT:
                mHelpFragmentDevice.onHelpUseSSSDevice();
                mHelpHandler.post(new Runnable()
                {
                    
                    @Override
                    public void run()
                    {
                        highlightHelpView(mViewPager);
                    }
                });
                break;
            case SUC:
                Toast.makeText(this, R.string.esp_sss_help_use_device_success_msg, Toast.LENGTH_LONG).show();
                mHelpMachine.exit();
                onExitHelpMode();
                break;
            default:
                break;
        }
    }
    
    @Override
    public void onHelpSSSMeshConfigure()
    {
        clearHelpContainer();
        
        HelpStepSSSMeshConfigure step = HelpStepSSSMeshConfigure.valueOf(mHelpMachine.getCurrentStateOrdinal());
        switch(step)
        {
            case START_MESH_CONFIGURE:
                highlightHelpView(findViewById(R.id.pager_tab_configure));
                setHelpHintMessage(R.string.esp_sss_help_mesh_configure_select_configure_msg);
                break;
            default:
                break;
        }
    }
    
    @Override
    protected void checkHelpPagerConfigureSelected()
    {
        if (mHelpMachine.isHelpModeUseSSSDevice())
        {
            mHelpMachine.transformState(true);
            mHelpHandler.post(new Runnable()
            {
                
                @Override
                public void run()
                {
                    mHelpFragmentConfig.onHelpUseSSSDevice();
                }
            });
        }
        else if (mHelpMachine.isHelpModeSSSMeshConfigure())
        {
            mHelpMachine.transformState(true);
            mHelpHandler.post(new Runnable()
            {
                
                @Override
                public void run()
                {
                    mHelpFragmentConfig.onHelpSSSMeshConfigure();
                }
            });
        }
    }
    
    @Override
    protected void checkHelpNotifyDevice()
    {
        if (mHelpMachine.isHelpModeUseSSSDevice())
        {
            onHelpUseSSSDevice();
        }
    }
    
    private static class HelpHandler extends Handler {
        private WeakReference<HelpSoftApStaSupportActivity> mActivity;
        
        public HelpHandler(HelpSoftApStaSupportActivity activity) {
            mActivity = new WeakReference<HelpSoftApStaSupportActivity>(activity);
        }
        
        @Override
        public void handleMessage(Message msg)
        {
            HelpSoftApStaSupportActivity activity = mActivity.get();
            if (activity == null)
            {
                return;
            }
            
            switch(msg.what) {
                case RESULT_HELP_SSS_USE_DEVICE:
                    mHelpMachine.start(HelpType.SSS_USAGE_DEVICE);
                    activity.mHelpFragmentDevice.onHelpUseSSSDevice();
                    break;
                case RESULT_HELP_SSS_UPGRADE:
                    mHelpMachine.start(HelpType.SSS_UPGRADE);
                    activity.mHelpFragmentDevice.onHelpUpgradeSSSDevice();
                    break;
                case RESULT_HELP_SSS_MESH_CONFIGURE:
                    mHelpMachine.start(HelpType.SSS_MESH_CONFIGURE);
                    activity.onHelpSSSMeshConfigure();
                    break;
            }
        }
    }
}
