package com.espressif.iot.logintool;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;

public class EspLoginHelpActivity extends Activity {
    private EspLoginSDK mLoginSDK;

    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mHandler = new Handler();

        mLoginSDK = EspLoginSDK.getInstance();
        mLoginSDK.setLoginHelper(this);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mLoginSDK.setLoginHelper(null);
    }
}
