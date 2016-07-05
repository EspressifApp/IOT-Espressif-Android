package com.espressif.iot.ui.device.soundbox;

import android.os.Bundle;

public abstract class QingtingBaseFragment extends TrackBaseFragment {
    protected QingtingFM mQingtingFM;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mQingtingFM = QingtingFM.INSTANCE;
    }
}
