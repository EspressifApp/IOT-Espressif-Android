package com.espressif.iot.ui.login;

import com.espressif.iot.R;
import com.espressif.iot.base.application.EspApplication;
import com.espressif.iot.type.user.EspLoginResult;
import com.espressif.iot.ui.login.LoginThirdPartyDialog.OnLoginListener;
import com.espressif.iot.ui.register.RegisterActivity;
import com.espressif.iot.user.IEspUser;
import com.espressif.iot.user.builder.BEspUser;
import com.espressif.iot.util.AccountUtil;
import com.espressif.iot.util.EspStrings;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.EditText;

public class LoginActivity extends Activity implements OnClickListener
{
    private IEspUser mUser;
    
    private EditText mEmailEdt;
    private EditText mPasswordEdt;
    
    private CheckBox mAutoLoginCB;
    
    private Button mLoginBtn;
    private Button mRegisterBtn;
    private Button mQuickUsageBtn;
    private TextView mThirdPartyLoginTV;
    
    private final static int REQUEST_REGISTER = 1;
    
    private LoginThirdPartyDialog mThirdPartyLoginDialog;
    
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.login_activity);
        
        mUser = BEspUser.getBuilder().getInstance();
        
        init();
    }
    
    private void init()
    {
        mEmailEdt = (EditText)findViewById(R.id.login_edt_account);
        mPasswordEdt = (EditText)findViewById(R.id.login_edt_password);
        
        mLoginBtn = (Button)findViewById(R.id.login_btn_login);
        mLoginBtn.setOnClickListener(this);
        
        mRegisterBtn = (Button)findViewById(R.id.login_btn_register);
        mRegisterBtn.setOnClickListener(this);
        
        // listen the auto login event
        mAutoLoginCB = (CheckBox)findViewById(R.id.login_cb_auto_login);
        
        mQuickUsageBtn = (Button)findViewById(R.id.login_btn_quick_usage);
        mQuickUsageBtn.setOnClickListener(this);
        
        mThirdPartyLoginTV = (TextView)findViewById(R.id.login_text_third_party);
        mThirdPartyLoginTV.setOnClickListener(this);
        
        mThirdPartyLoginDialog = new LoginThirdPartyDialog(this);
        mThirdPartyLoginDialog.setOnLoginListener(mThirdPartyLoginListener);
    }
    
    @Override
    public void onClick(View v)
    {
        if (v == mLoginBtn)
        {
            login();
        }
        else if (v == mRegisterBtn)
        {
            Intent intent = new Intent(this, RegisterActivity.class);
            startActivityForResult(intent, REQUEST_REGISTER);
        }
        else if (v == mQuickUsageBtn)
        {
            mUser.loadUserDeviceListDB();
            Intent intent = new Intent(this, EspApplication.getEspUIActivity());
            startActivity(intent);
            finish();
        }
        else if (v == mThirdPartyLoginTV)
        {
            mThirdPartyLoginDialog.show();
        }
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == REQUEST_REGISTER)
        {
            if (resultCode == RESULT_OK)
            {
                String email = data.getStringExtra(EspStrings.Key.REGISTER_NAME_EMAIL);
                String password = data.getStringExtra(EspStrings.Key.REGISTER_NAME_PASSWORD);
                mEmailEdt.setText(email);
                mPasswordEdt.setText(password);
            }
        }
    }
    
    private void login()
    {
        final String account = mEmailEdt.getText().toString();
        final int accountType = AccountUtil.getAccountType(account);
        if (accountType == AccountUtil.TYPE_NONE)
        {
            Toast.makeText(this, R.string.esp_login_email_hint, Toast.LENGTH_SHORT).show();
            return;
        }
        final String password = mPasswordEdt.getText().toString();
        if (TextUtils.isEmpty(password))
        {
            Toast.makeText(this, R.string.esp_login_password_hint, Toast.LENGTH_SHORT).show();
            return;
        }
        new LoginTask(this)
        {
            @Override
            public EspLoginResult doLogin()
            {
                if (accountType == AccountUtil.TYPE_EMAIL)
                {
                    return mUser.doActionUserLoginInternet(account, password);
                }
                else if (accountType == AccountUtil.TYPE_PHONE)
                {
                    return mUser.doActionUserLoginPhone(account, password);
                }
                
                return null;
            }
            
            @Override
            public void loginResult(EspLoginResult result)
            {
                if (result == EspLoginResult.SUC)
                {
                    loginSuccess();
                }
            }
        }.execute();
    }
    
    private OnLoginListener mThirdPartyLoginListener = new OnLoginListener()
    {
        
        @Override
        public void onLoginComplete(EspLoginResult result)
        {
            if (result == EspLoginResult.SUC)
            {
                loginSuccess();
            }
        }
    };
    
    private void loginSuccess()
    {
        SharedPreferences shared = getSharedPreferences(EspStrings.Key.SETTINGS_NAME, Context.MODE_PRIVATE);
        shared.edit().putBoolean(EspStrings.Key.KEY_AUTO_LOGIN, mAutoLoginCB.isChecked()).commit();
        
        Intent intent = new Intent(this, EspApplication.getEspUIActivity());
        startActivity(intent);
        finish();
    }
 
}
