package com.espressif.iot.ui.device.soundbox;

import java.util.List;

import com.espressif.iot.device.IEspDeviceSoundbox;

import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v13.app.FragmentPagerAdapter;

public abstract class PlatformBaseFragment extends Fragment {
    public abstract String getFragmentTag();

    protected IEspDeviceSoundbox mSoundbox;

    public void setSoundbox(IEspDeviceSoundbox soundbox) {
        mSoundbox = soundbox;
    }

    public void refresh() {
    }

    protected class PagerAdapter extends FragmentPagerAdapter {

        private List<TrackBaseFragment> mFragments;

        public PagerAdapter(FragmentManager fm, List<TrackBaseFragment> fragments) {
            super(fm);
            mFragments = fragments;
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
            return mFragments.get(position).getTitle();
        }
    }
}
