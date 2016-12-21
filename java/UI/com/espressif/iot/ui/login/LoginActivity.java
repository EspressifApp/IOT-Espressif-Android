package com.espressif.iot.ui.login;

import com.espressif.iot.R;
import com.espressif.iot.logintool.EspLoginSDK;
import com.espressif.iot.type.user.EspLoginResult;
import com.espressif.iot.ui.login.LoginThirdPartyDialog.OnLoginListener;
import com.espressif.iot.ui.register.RegisterActivity;
import com.espressif.iot.user.IEspUser;
import com.espressif.iot.user.builder.BEspUser;
import com.espressif.iot.util.AccountUtil;
import com.espressif.iot.util.EspStrings;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;
import android.widget.EditText;

public class LoginActivity extends Activity implements OnClickListener, OnEditorActionListener {
    private IEspUser mUser;

    private EditText mEmailEdt;
    private EditText mPasswordEdt;

    private Button mLoginBtn;
    private Button mRegisterBtn;
    private TextView mForgetPwdTV;
    private TextView mThirdPartyLoginTV;

    private final static int REQUEST_REGISTER = 1;

    private LoginThirdPartyDialog mThirdPartyLoginDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EspLoginSDK.init(this);
        mUser = BEspUser.getBuilder().getInstance();
        setContentView(R.layout.login_activity);
        init();
    }

    private void init() {
        mEmailEdt = (EditText)findViewById(R.id.login_edt_account);
        mPasswordEdt = (EditText)findViewById(R.id.login_edt_password);
        mPasswordEdt.setOnEditorActionListener(this);

        mLoginBtn = (Button)findViewById(R.id.login_btn_login);
        mLoginBtn.setOnClickListener(this);

        mRegisterBtn = (Button)findViewById(R.id.login_btn_register);
        mRegisterBtn.setOnClickListener(this);

        mThirdPartyLoginTV = (TextView)findViewById(R.id.login_text_third_party);
        mThirdPartyLoginTV.setOnClickListener(this);

        mForgetPwdTV = (TextView)findViewById(R.id.forget_password_text);
        mForgetPwdTV.setOnClickListener(this);

        mThirdPartyLoginDialog = new LoginThirdPartyDialog(this);
        mThirdPartyLoginDialog.setOnLoginListener(mThirdPartyLoginListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        EspLoginSDK.release();
    }

    @Override
    public void onClick(View v) {
        if (v == mLoginBtn) {
            login();
        } else if (v == mRegisterBtn) {
            Intent intent = new Intent(this, RegisterActivity.class);
            startActivityForResult(intent, REQUEST_REGISTER);
        } else if (v == mThirdPartyLoginTV) {
            mThirdPartyLoginDialog.show();
        } else if (v == mForgetPwdTV) {
            startActivity(new Intent(this, ResetUserPasswordActivity.class));
        }
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (v == mPasswordEdt) {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                login();
                return true;
            }
        }

        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_REGISTER) {
            if (resultCode == RESULT_OK) {
                // Register completed, set the new account email and password
                String email = data.getStringExtra(EspStrings.Key.REGISTER_NAME_EMAIL);
                String password = data.getStringExtra(EspStrings.Key.REGISTER_NAME_PASSWORD);
                mEmailEdt.setText(email);
                mPasswordEdt.setText(password);
            }
        }
    }

    private void login() {
        final String account = mEmailEdt.getText().toString();
        final int accountType = AccountUtil.getAccountType(account);
        if (accountType == AccountUtil.TYPE_NONE) {
            // Account id is illegal
            Toast.makeText(this, R.string.esp_login_email_hint, Toast.LENGTH_SHORT).show();
            return;
        }
        final String password = mPasswordEdt.getText().toString();
        if (TextUtils.isEmpty(password)) {
            // The password can't be empty
            Toast.makeText(this, R.string.esp_login_password_hint, Toast.LENGTH_SHORT).show();
            return;
        }
        new LoginTask(this) {
            @Override
            public EspLoginResult doLogin() {
                if (accountType == AccountUtil.TYPE_EMAIL) {
                    // Login with Email
                    return mUser.doActionUserLoginInternet(account, password);
                } else if (accountType == AccountUtil.TYPE_PHONE) {
                    // Login with Phone number
                    return mUser.doActionUserLoginPhone(account, password);
                }

                return null;
            }

            @Override
            public void loginResult(EspLoginResult result) {
                if (result == EspLoginResult.SUC) {
                    loginSuccess();
                }
            }
        }.execute();
    }

    private OnLoginListener mThirdPartyLoginListener = new OnLoginListener() {

        @Override
        public void onLoginComplete(EspLoginResult result) {
            if (result == EspLoginResult.SUC) {
                loginSuccess();
            }
        }
    };

    private void loginSuccess() {
        setResult(RESULT_OK);
        finish();
    }
}
