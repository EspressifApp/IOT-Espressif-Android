package com.espressif.iot.ui.device.soundbox;

import java.util.ArrayList;
import java.util.List;

import com.espressif.iot.R;
import com.espressif.iot.ui.device.DeviceActivityAbs;
import com.ximalaya.ting.android.opensdk.datatrasfer.CommonRequest;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.view.View;

public class DeviceSoundboxActivity extends DeviceActivityAbs {
    private static final String APP_SECRECT = "d793f86ee582749f8a8a381d37fbad87";
    private CommonRequest mXimalaya;

    private ViewPager mPager;
    private PagerAdapter mPagerAdapter;
    private List<Fragment> mFragments;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mXimalaya = CommonRequest.getInstanse();
        mXimalaya.init(this, APP_SECRECT);
        mXimalaya.setDefaultPagesize(50);
    }

    @Override
    protected View initControlView() {
        View view = View.inflate(this, R.layout.device_activity_soundbox, null);

        mPager = (ViewPager)view.findViewById(R.id.pager);
        PagerTabStrip pagerTab = (PagerTabStrip)view.findViewById(R.id.indicator);
        int tabColor = getResources().getColor(R.color.esp_actionbar_color);
        pagerTab.setTextColor(tabColor);
        pagerTab.setTabIndicatorColor(tabColor);
        mFragments = new ArrayList<Fragment>();
        XimaHotFragment hotFg = new XimaHotFragment();
        hotFg.setTitle(getString(R.string.esp_soundbox_xima_hot_title));
        mFragments.add(hotFg);
        XimaSearchFragment searchFg = new XimaSearchFragment();
        searchFg.setTitle(getString(R.string.esp_soundbox_xima_search_title));
        mFragments.add(searchFg);
        mPagerAdapter = new PagerAdapter(getFragmentManager());
        mPager.setAdapter(mPagerAdapter);

        return view;
    }

    @Override
    protected void executePrepare() {
    }

    @Override
    protected void executeFinish(int command, boolean result) {
    }

    private class PagerAdapter extends FragmentPagerAdapter {

        public PagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int positon) {
            return mFragments.get(positon);
        }

        @Override
        public int getCount() {
            return mFragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return ((XimaBaseFragment)mFragments.get(position)).getTitle();
        }
    }

}
