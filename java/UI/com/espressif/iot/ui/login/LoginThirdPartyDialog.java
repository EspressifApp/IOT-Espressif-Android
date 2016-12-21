package com.espressif.iot.ui.login;

import java.util.List;

import org.apache.log4j.Logger;

import com.espressif.iot.R;
import com.espressif.iot.logintool.EspLoginSDK;
import com.espressif.iot.logintool.Platform;
import com.espressif.iot.logintool.Platform.PlatformListener;
import com.espressif.iot.type.user.EspLoginResult;
import com.espressif.iot.type.user.EspThirdPartyLoginPlat;
import com.espressif.iot.ui.widget.dialog.NoBgDialog;
import com.espressif.iot.user.IEspUser;
import com.espressif.iot.user.builder.BEspUser;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class LoginThirdPartyDialog implements OnClickListener, PlatformListener {
    private final Logger log = Logger.getLogger(getClass());

    private Context mContext;

    private TextView mQQTV;
    private Platform mQQPlat;

    private NoBgDialog mThirdPartyLoginSelectDialog;
    private ProgressDialog mThirdPartyEnteringDialog;

    private Handler mHandler = new Handler();

    public interface OnLoginListener {
        void onLoginComplete(EspLoginResult result);
    }

    private OnLoginListener mLoginListener;

    public LoginThirdPartyDialog(Context context) {
        mContext = context;

        initPlatforms();

        initWidgets();
    }

    private void initPlatforms() {
        List<Platform> platforms = EspLoginSDK.getInstance().getPlatforms();
        for (Platform p : platforms) {
            switch (p.getType()) {
                case QQ:
                    mQQPlat = p;
                    mQQPlat.setPlatformListener(this);
                    break;
            }
        }
    }

    private void initWidgets() {
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
                mQQPlat.authorize();
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

    @Override
    public void onComplete(Platform platform) {
        log.info(platform.getType() + " onComplete");
        mThirdPartyEnteringDialog.dismiss();
        final String token = platform.getAcessToken();
        final String openId = platform.getOpenId();
        final String state = platform.getState();
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                thirdPartyLogin(state, token, openId);
            }
        });
    }

    @Override
    public void onError(Platform platform, String errorMsg) {
        log.warn(platform.getType() + " onError: " +  errorMsg);
        mThirdPartyEnteringDialog.dismiss();
    }

    @Override
    public void onCancel(Platform platform) {
        log.debug(platform.getType() + " onCancel");
        mThirdPartyEnteringDialog.dismiss();
    }
}
