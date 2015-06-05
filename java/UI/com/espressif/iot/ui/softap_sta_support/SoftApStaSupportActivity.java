package com.espressif.iot.ui.softap_sta_support;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.espressif.iot.R;
import com.espressif.iot.ui.main.EspActivityAbs;
import com.espressif.iot.ui.view.EspViewPager;

import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v13.app.FragmentPagerAdapter;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

public class SoftApStaSupportActivity extends EspActivityAbs implements OnCheckedChangeListener
{
    private SectionsPagerAdapter mSectionsPagerAdapter;
    
    protected EspViewPager mViewPager;
    private RadioGroup mPagerTabs;
    
    public final int FRAGMENT_DEVICE = 0;
    public final int FRAGMENT_CONFIGURE = 1;
    
    protected List<Fragment> mFragmentList;
    protected SSSFragmentDevices mFragmentDevice;
    protected SSSFragmentConfigure mFragmentConfig;
    
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.softap_sta_layout);
        
        mFragmentList = new ArrayList<Fragment>();
        initPagerFragments();
        mFragmentList.add(mFragmentDevice);
        mFragmentList.add(mFragmentConfig);
        
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
                        
                        checkHelpPagerConfigureSelected();
                        break;
                }
            }
        });
        
        mPagerTabs = (RadioGroup)findViewById(R.id.pager_tabs);
        mPagerTabs.setOnCheckedChangeListener(this);
        mPagerTabs.check(R.id.pager_tab_devices);
    }
    
    protected void initPagerFragments()
    {
        mFragmentDevice = new SSSFragmentDevices();
        mFragmentConfig = new SSSFragmentConfigure();
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
            if (position < mFragmentList.size())
            {
                return mFragmentList.get(position);
            }
            return null;
        }
        
        @Override
        public int getCount()
        {
            return mFragmentList.size();
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
                
                checkHelpNotifyDevice();
                break;
            case FRAGMENT_CONFIGURE:
                break;
        }
    }
    
    protected void checkHelpPagerConfigureSelected()
    {
    }
    
    protected void checkHelpNotifyDevice()
    {
    }
}
