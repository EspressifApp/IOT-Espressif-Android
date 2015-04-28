package com.espressif.iot.ui.softap_sta_support;

import java.lang.ref.WeakReference;
import java.util.Locale;

import com.espressif.iot.R;
import com.espressif.iot.help.ui.IEspHelpUISSSUseDevice;
import com.espressif.iot.type.help.HelpStepSSSUseDevice;
import com.espressif.iot.type.help.HelpType;
import com.espressif.iot.ui.EspActivityAbs;
import com.espressif.iot.ui.softap_sta_support.help.SSSHelpActivity;
import com.espressif.iot.ui.view.EspViewPager;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.support.v13.app.FragmentPagerAdapter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Toast;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView.ScaleType;
import android.widget.RadioGroup.OnCheckedChangeListener;

public class SoftApStaSupportActivity extends EspActivityAbs implements OnCheckedChangeListener, IEspHelpUISSSUseDevice
{
    private SectionsPagerAdapter mSectionsPagerAdapter;
    
    private EspViewPager mViewPager;
    private RadioGroup mPagerTabs;
    
    private final static int FRAGMENT_COUNT = 2;
    final int FRAGMENT_DEVICE = 0;
    final int FRAGMENT_CONFIGURE = 1;
    
    private SSSFragmentDevices mFragmentDevice;
    private SSSFragmentConfigure mFragmentConfig;
    
    private Handler mHelpHandler;
    private final static int REQUEST_HELP = 0;
    
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.softap_sta_layout);
        
        mFragmentDevice = new SSSFragmentDevices();
        mFragmentConfig = new SSSFragmentConfigure();
        
        mSectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager());
        
        mViewPager = (EspViewPager)findViewById(R.id.pager);
        mViewPager.setInterceptTouchEvent(true);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener()
        {
            @Override
            public void onPageSelected(int position)
            {
                switch(position)
                {
                    case FRAGMENT_DEVICE:
                        mPagerTabs.check(R.id.pager_tab_devices);
                        break;
                    case FRAGMENT_CONFIGURE:
                        mPagerTabs.check(R.id.pager_tab_configure);
                        
                        if (mHelpMachine.isHelpModeUseSSSDevice())
                        {
                            mHelpMachine.transformState(true);
                            mHelpHandler.post(new Runnable()
                            {
                                
                                @Override
                                public void run()
                                {
                                    mFragmentConfig.onHelpUseSSSDevice();
                                }
                            });
                        }
                        break;
                }
            }
        });
        
        mPagerTabs = (RadioGroup)findViewById(R.id.pager_tabs);
        mPagerTabs.setOnCheckedChangeListener(this);
        mPagerTabs.check(R.id.pager_tab_devices);
        
        mHelpHandler = new HelpHandler(this);
        setTitleRightIcon(R.drawable.esp_icon_help);
    }
    
    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId)
    {
        switch(checkedId)
        {
            case R.id.pager_tab_devices:
                mViewPager.setCurrentItem(FRAGMENT_DEVICE);
                break;
            case R.id.pager_tab_configure:
                mViewPager.setCurrentItem(FRAGMENT_CONFIGURE);
                break;
        }
    }
    
    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter
    {
        
        public SectionsPagerAdapter(FragmentManager fm)
        {
            super(fm);
        }
        
        @Override
        public Fragment getItem(int position)
        {
            switch (position)
            {
                case FRAGMENT_DEVICE:
                    return mFragmentDevice;
                case FRAGMENT_CONFIGURE:
                    return mFragmentConfig;
            }
            return null;
        }
        
        @Override
        public int getCount()
        {
            return FRAGMENT_COUNT;
        }
        
        @Override
        public CharSequence getPageTitle(int position)
        {
            Locale l = Locale.getDefault();
            switch (position)
            {
                case FRAGMENT_DEVICE:
                    return getString(R.string.esp_sss_actiontab_device).toUpperCase(l);
                case FRAGMENT_CONFIGURE:
                    return getString(R.string.esp_sss_actiontab_configure).toUpperCase(l);
            }
            return null;
        }
    }
    
    public void notifyFragment(int targetId)
    {
        switch(targetId) {
            case FRAGMENT_DEVICE:
                mFragmentDevice.showScanningPorgress(true);
                mFragmentDevice.scanStas();
                mViewPager.setCurrentItem(FRAGMENT_DEVICE);
                
                if (mHelpMachine.isHelpModeUseSSSDevice())
                {
                    onHelpUseSSSDevice();
                }
                break;
            case FRAGMENT_CONFIGURE:
                break;
        }
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
            }
        }
    }
    
    private static class HelpHandler extends Handler {
        private WeakReference<SoftApStaSupportActivity> mActivity;
        
        public HelpHandler(SoftApStaSupportActivity activity) {
            mActivity = new WeakReference<SoftApStaSupportActivity>(activity);
        }
        
        @Override
        public void handleMessage(Message msg)
        {
            SoftApStaSupportActivity activity = mActivity.get();
            if (activity == null)
            {
                return;
            }
            
            switch(msg.what) {
                case RESULT_HELP_SSS_USE_DEVICE:
                    mHelpMachine.start(HelpType.SSS_USAGE_DEVICE);
                    activity.mFragmentDevice.onHelpUseSSSDevice();
                    break;
                case RESULT_HELP_SSS_UPGRADE:
                    mHelpMachine.start(HelpType.SSS_UPGRADE);
                    activity.mFragmentDevice.onHelpUpgradeSSSDevice();
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
            mFragmentDevice.onHelpUseSSSDevice();
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
                mFragmentDevice.onHelpUseSSSDevice();
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
}
