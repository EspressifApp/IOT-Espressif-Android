package com.espressif.iot.ui.device.soundbox;

import com.ximalaya.ting.android.opensdk.datatrasfer.CommonRequest;

import android.app.Activity;

public abstract class XimaBaseFragment extends TrackBaseFragment {
    protected CommonRequest mXimalaya;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        mXimalaya = CommonRequest.getInstanse();
    }
}
