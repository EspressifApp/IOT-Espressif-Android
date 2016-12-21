package com.espressif.iot.logintool;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;

import com.tencent.connect.common.Constants;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class EspLoginHelpActivity extends Activity {
    private static final int REQUEST_QQ_LOGIN = Constants.REQUEST_LOGIN;
    private static final int REQUEST_QQ_APPBAR = Constants.REQUEST_APPBAR;

    private EspLoginSDK mLoginSDK;

    private Handler mHandler;

    private Platform mPlatform;

    private Tencent mTencent;
    private QQListener mQQListener = new QQListener();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mHandler = new Handler();

        mLoginSDK = EspLoginSDK.getInstance();

        Intent intent = getIntent();
        String appId = intent.getStringExtra(EspLoginSDK.KEY_PLATFORM_APP_ID);
        String appKey = intent.getStringExtra(EspLoginSDK.KEY_PLATFORM_APP_KEY);
        List<Platform> platforms = mLoginSDK.getPlatforms();
        for (Platform p : platforms) {
            if (!TextUtils.isEmpty(appId) && appId.equals(p.getAppId())) {
                mPlatform = p;
                break;
            }
            if (!TextUtils.isEmpty(appKey) && appKey.equals(p.getAppKey())) {
                mPlatform = p;
                break;
            }
        }

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                authorize();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_QQ_LOGIN:
            case REQUEST_QQ_APPBAR:
                Tencent.onActivityResultData(requestCode, resultCode, data, mQQListener);
                return;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void authorize() {
        switch (mPlatform.getType()) {
            case QQ:
                authorizeQQ();
                break;
            default:
                break;
        }
    }

    private void authorizeQQ() {
        mTencent = Tencent.createInstance(mPlatform.getAppId(), this);
        mTencent.login(this, "all", mQQListener);
    }

    private class QQListener implements IUiListener {

        @Override
        public void onError(UiError error) {
            finish();
            mLoginSDK.notifyAuthorizeError(mPlatform, error.errorMessage);
        }

        @Override
        public void onComplete(Object jsonObj) {
            finish();
            JSONObject json;
            try {
                json = (JSONObject)jsonObj;
            } catch (ClassCastException e) {
                mLoginSDK.notifyAuthorizeError(mPlatform, "onComplete ClassCastException:  " + e.getMessage());
                return;
            }

            try {
                final String token = json.getString("access_token");
                final String openId = json.getString("openid");
                mPlatform.setAccessToken(token);
                mPlatform.setOpenId(openId);

                mLoginSDK.notifyAuthorizeComplete(mPlatform);
            } catch (JSONException e) {
                e.printStackTrace();
                mLoginSDK.notifyAuthorizeError(mPlatform, "onComplete JSONException:  " + e.getMessage());
            }
        }

        @Override
        public void onCancel() {
            finish();
            mLoginSDK.notifyAuthrozeCancel(mPlatform);
        }
    }
}
