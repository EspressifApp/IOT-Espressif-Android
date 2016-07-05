package com.espressif.iot.ui.device.soundbox;

import java.util.ArrayList;
import java.util.List;

import com.espressif.iot.R;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class PlatformQingtingFragment extends PlatformBaseFragment {
    private static final String CLIENT_ID = "Mjg3ODdmMzItMWI0MS0xMWU2LTkyM2YtMDAxNjNlMDAyMGFk";
    private static final String CLIENT_SECRET = "ZWEwYjhlMDUtYzc2ZC0zNTU4LThhZDYtMjA5YWY2MzUxNDE4";

    private QingtingFM mQingtingFM;

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

        mQingtingFM = QingtingFM.INSTANCE;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.soundbox_fragment_qingting, container, false);

        mFragments = new ArrayList<TrackBaseFragment>();
        QingtingLiveFragment liveFg = new QingtingLiveFragment();
        liveFg.setTitle(getString(R.string.esp_soundbox_qingbting_live_title));
        mFragments.add(liveFg);

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

    public void authorization() {
        new AuthorizationTask().execute();
    }

    public boolean isQingtingAccessable() {
        return (!TextUtils.isEmpty(mQingtingFM.getAccessToken())
            && mQingtingFM.getAccessTokenExpires() > System.currentTimeMillis());
    }

    private class AuthorizationTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            return mQingtingFM.authorization(CLIENT_ID, CLIENT_SECRET);
        }

    }
}
