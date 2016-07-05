package com.espressif.iot.ui.device.soundbox;

import java.util.ArrayList;
import java.util.List;

import com.espressif.iot.R;
import com.ximalaya.ting.android.opensdk.datatrasfer.CommonRequest;

import android.os.Bundle;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class PlatformXimaFragment extends PlatformBaseFragment {
    private static final String APP_SECRECT = "d793f86ee582749f8a8a381d37fbad87";
    private CommonRequest mXimalaya;

    private ViewPager mPager;
    private PagerAdapter mPagerAdapter;
    private List<TrackBaseFragment> mFragments;

    @Override
    public String getFragmentTag() {
        return getClass().getSimpleName();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mXimalaya = CommonRequest.getInstanse();
        mXimalaya.init(getActivity(), APP_SECRECT);
        mXimalaya.setDefaultPagesize(50);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.soundbox_fragment_xima, container, false);

        mFragments = new ArrayList<TrackBaseFragment>();
        // Live radio
        XimaLiveFragment liveFg = new XimaLiveFragment();
        liveFg.setTitle(getString(R.string.esp_soundbox_xima_live_title));
        mFragments.add(liveFg);
        // Hot tracks
        XimaHotFragment hotFg = new XimaHotFragment();
        hotFg.setTitle(getString(R.string.esp_soundbox_xima_hot_title));
        mFragments.add(hotFg);
        // Search tracks
        XimaSearchFragment searchFg = new XimaSearchFragment();
        searchFg.setTitle(getString(R.string.esp_soundbox_xima_search_title));
        mFragments.add(searchFg);

        mPager = (ViewPager)view.findViewById(R.id.pager);
        PagerTabStrip pagerTab = (PagerTabStrip)view.findViewById(R.id.indicator);
        int tabColor = getResources().getColor(R.color.esp_actionbar_color);
        pagerTab.setTextColor(tabColor);
        pagerTab.setTabIndicatorColor(tabColor);
        mPagerAdapter = new PagerAdapter(getFragmentManager(), mFragments);
        mPager.setAdapter(mPagerAdapter);
        mPager.setOffscreenPageLimit(5);

        return view;
    }

    @Override
    public void refresh() {
        for (TrackBaseFragment fm : mFragments) {
            fm.refresh();
        }
    }
}
