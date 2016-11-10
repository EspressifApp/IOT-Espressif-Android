package com.espressif.iot.logintool;

import java.lang.ref.WeakReference;

import android.content.Context;
import android.content.Intent;

public class EspLoginSDK {
    private static Object mLock = new Object();
    private static EspLoginSDK instance;

    public static void init(Context context) {
        synchronized (mLock) {
            if (instance == null) {
                instance = new EspLoginSDK(context);
            }
        }
    }

    private Context mContext;
    private WeakReference<EspLoginHelpActivity> mHelper;

    public static EspLoginSDK getInstance() {
        return instance;
    }

    private EspLoginSDK(Context context) {
        mContext = context;
    }

    public void setLoginHelper(EspLoginHelpActivity helper) {
        if (mHelper != null) {
            mHelper.clear();
        }
        if (helper != null) {
            mHelper = new WeakReference<EspLoginHelpActivity>(helper);
        } else {
            mHelper = null;
        }
    }
    
    public void login(Platform platform) {
        EspLoginHelpActivity helper = mHelper.get();
       if (helper != null) {
           
       } else {
           Intent intent = new Intent(mContext, EspLoginHelpActivity.class);
           mContext.startActivity(intent);
       }
    }
}
