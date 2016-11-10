package com.espressif.iot.ui.login;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.espressif.iot.R;
import com.espressif.iot.type.user.EspLoginResult;
import com.espressif.iot.type.user.EspThirdPartyLoginPlat;
import com.espressif.iot.ui.widget.dialog.NoBgDialog;
import com.espressif.iot.user.IEspUser;
import com.espressif.iot.user.builder.BEspUser;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class LoginThirdPartyDialog implements OnClickListener {
    private final Logger log = Logger.getLogger(getClass());

    private static final String APP_ID = "1104658257";

    private Tencent mTencent;

    private Context mContext;

    private TextView mQQTV;

    private NoBgDialog mThirdPartyLoginSelectDialog;
    private ProgressDialog mThirdPartyEnteringDialog;

    private Handler mHandler = new Handler();

    public interface OnLoginListener {
        void onLoginComplete(EspLoginResult result);
    }

    private OnLoginListener mLoginListener;

    public LoginThirdPartyDialog(Context context) {
        mContext = context;

        mTencent = Tencent.createInstance(APP_ID, mContext);

        View content = View.inflate(mContext, R.layout.third_party_login_dialog, null);
        mQQTV = (TextView)content.findViewById(R.id.qq_login);
        mQQTV.setOnClickListener(this);

        mThirdPartyLoginSelectDialog = new NoBgDialog(mContext);
        mThirdPartyLoginSelectDialog.setView(content);

        mThirdPartyEnteringDialog = new ProgressDialog(mContext);
        mThirdPartyEnteringDialog.setMessage(mContext.getString(R.string.esp_third_party_authorizing));
        mThirdPartyEnteringDialog.setCanceledOnTouchOutside(false);
    }

    public void show() {
        if (mThirdPartyEnteringDialog.isShowing()) {
            return;
        }
        if (!mThirdPartyLoginSelectDialog.isShowing()) {
            mThirdPartyLoginSelectDialog.show();
        }
    }

    public void setOnLoginListener(OnLoginListener listener) {
        mLoginListener = listener;
    }

    @Override
    public void onClick(View v) {
        mThirdPartyLoginSelectDialog.dismiss();
        mThirdPartyEnteringDialog.show();

        switch (v.getId()) {
            case R.id.qq_login:
                mTencent.login((Activity)mContext, "all", mQQListener);
                break;
        }
    }

    private void thirdPartyLogin(String state, String token, String userId) {
        final EspThirdPartyLoginPlat espPlat = new EspThirdPartyLoginPlat();
        espPlat.setAccessToken(token);
        espPlat.setState(state);
        espPlat.setOpenId(userId);

        new LoginTask(mContext) {
            @Override
            public EspLoginResult doLogin() {
                IEspUser user = BEspUser.getBuilder().getInstance();
                return user.doActionThirdPartyLoginInternet(espPlat);
            }

            @Override
            public void loginResult(EspLoginResult result) {
                if (mLoginListener != null) {
                    mLoginListener.onLoginComplete(result);
                }
            }
        }.execute();
    }

    private IUiListener mQQListener = new IUiListener() {

        @Override
        public void onError(UiError error) {
            log.warn("qq error" + error.errorMessage);
        }

        @Override
        public void onComplete(Object jsonObj) {
            log.info("qq complete = " + jsonObj.toString());
            JSONObject json = (JSONObject)jsonObj;
            try {
                final String token = json.getString("access_token");
                final String openId = json.getString("openid");
                final String state = "qq";
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        thirdPartyLogin(state, token, openId);
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onCancel() {
            log.debug("qq cancel");
        }
    };

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Tencent.onActivityResultData(requestCode, resultCode, data, mQQListener);
    }
}
