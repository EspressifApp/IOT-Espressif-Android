package com.espressif.iot.ui.main;

import org.apache.log4j.Logger;

import com.espressif.iot.R;
import com.espressif.iot.base.application.EspApplication;
import com.espressif.iot.type.user.EspLoginResult;
import com.espressif.iot.type.user.EspRegisterResult;
import com.espressif.iot.ui.esptouch.EspTouchActivity;
import com.espressif.iot.ui.softap_sta_support.SoftApStaSupportActivity;
import com.espressif.iot.ui.softap_sta_support.help.HelpSoftApStaSupportActivity;
import com.espressif.iot.user.IEspUser;
import com.espressif.iot.user.builder.BEspUser;
import com.espressif.iot.util.EspStrings;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;
import android.widget.EditText;

public class LoginActivity extends Activity implements OnClickListener
{
    
    private static final Logger log = Logger.getLogger(LoginActivity.class);
    
    private IEspUser mUser;
    
    private EditText mEmailEdt;
    
    private EditText mPasswordEdt;
    
    private CheckBox mAutoLoginCB;
    
    private Button mLoginBtn;
    
    private Button mRegisterBtn;
    
    private Button mSkipBtn;
    
    private Button mLocalBtn;
    
    private Button mEsptouchBtn;
    
    private Button mQuickUsageBtn;
    
    private final static int REQUEST_REGISTER = 1;
    
    private final static String SKIP_DEFAULT_PASSWORD = "123456";
    
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
        
        // listen the login, the default account: liu, mEdtPassword: 123
        mLoginBtn = (Button)findViewById(R.id.login_btn_login);
        mLoginBtn.setOnClickListener(this);
        
        mRegisterBtn = (Button)findViewById(R.id.login_btn_register);
        mRegisterBtn.setOnClickListener(this);
        
        mSkipBtn = (Button)findViewById(R.id.login_skip);
        mSkipBtn.setOnClickListener(this);
        
        mLocalBtn = (Button)findViewById(R.id.login_local);
        mLocalBtn.setOnClickListener(this);
        
        // listen the auto login event
        mAutoLoginCB = (CheckBox)findViewById(R.id.login_cb_auto_login);
        
        mEsptouchBtn = (Button)findViewById(R.id.login_esptouch);
        mEsptouchBtn.setOnClickListener(this);

        mQuickUsageBtn = (Button)findViewById(R.id.login_btn_quick_usage);
        mQuickUsageBtn.setOnClickListener(this);
    }
    
    @Override
    public void onClick(View v)
    {
        if (v == mLoginBtn)
        {
            String email = mEmailEdt.getText().toString();
            String password = mPasswordEdt.getText().toString();
            boolean autoLogin = mAutoLoginCB.isChecked();
            new LoginTask(this, email, password, autoLogin) {
                
                public void loginResult(EspLoginResult result) {
                    if (result == EspLoginResult.SUC)
                    {
                        loginSuccess(0);
                    }
                }
                
            }.execute();
        }
        else if (v == mSkipBtn)
        {
            skip();
        }
        else if (v == mRegisterBtn)
        {
            Intent intent = new Intent(this, RegisterActivity.class);
            startActivityForResult(intent, REQUEST_REGISTER);
        }
        else if (v ==  mLocalBtn)
        {
            Class<?> _class;
            if (EspApplication.HELP_ON)
            {
                _class = HelpSoftApStaSupportActivity.class;
            }
            else
            {
                _class = SoftApStaSupportActivity.class;
            }
            startActivity(new Intent(this, _class));
        }
        else if (v == mEsptouchBtn)
        {
            startActivity(new Intent(this, EspTouchActivity.class));
        }
        else if (v == mQuickUsageBtn)
        {
            mUser.loadUserDeviceListDB();
            Intent intent = new Intent(this, EspApplication.getEspUIActivity());
            startActivity(intent);
            finish();
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
    
    private void loginSuccess(int msg)
    {
        Intent intent = new Intent(this, EspApplication.getEspUIActivity());
        startActivity(intent);
        finish();
    }
    
    private void loginFailed(int msg)
    {
    }
    
    private void skip()
    {
        SharedPreferences shared = getSharedPreferences(EspStrings.Key.MAC_USER, Context.MODE_PRIVATE);
        String macUsername = shared.getString(EspStrings.Key.MAC_USER_NAME_KEY, "");
        
        if (TextUtils.isEmpty(macUsername))
        {
            WifiManager wifiManager = (WifiManager)getSystemService(Context.WIFI_SERVICE);
            if (!wifiManager.isWifiEnabled())
            {
                Toast.makeText(this, R.string.esp_skip_wifi_toast, Toast.LENGTH_LONG).show();
                return;
            }
            
            String mac = wifiManager.getConnectionInfo().getMacAddress();
            String[] macSplits = mac.split(":");
            for (int i = 0; i < macSplits.length; i++)
            {
                macUsername += macSplits[i];
            }
            shared.edit().putString(EspStrings.Key.MAC_USER_NAME_KEY, macUsername).commit();
        }
        
        log.info("MacAccount = " + macUsername);
        
        new SkipTask(macUsername).execute();
    }
    
    private class SkipTask extends AsyncTask<Void, Void, EspLoginResult>
    {
        private Context mContext;
        
        private String mUserName;
        
        private ProgressDialog mDialog;
        
        public SkipTask(String username)
        {
            mContext = LoginActivity.this;
            mUserName = username;
        }
        
        @Override
        protected void onPreExecute()
        {
            mDialog = new ProgressDialog(mContext);
            mDialog.setMessage(getString(R.string.esp_login_progress_message));
            mDialog.setCancelable(false);
            mDialog.setCanceledOnTouchOutside(false);
            mDialog.show();
        }
        
        @Override
        protected EspLoginResult doInBackground(Void... params)
        {
            final String username = "esp_" + mUserName;
            final String email = username + "@mac.com";
            final String password = SKIP_DEFAULT_PASSWORD;
            // Step 1: login
            log.debug("Skip login");
            EspLoginResult result = mUser.doActionUserLoginInternet(email, password, false, true);
            switch (result)
            {
                case SUC:
                case NETWORK_UNACCESSIBLE:
                case PASSWORD_ERR:
                    return result;
                case NOT_REGISTER:
                    // if mac address not register
                    // Step 2: register
                    if (register(username, email, password)) {
                        // if register success
                        // Step 3: login again
                        log.debug("Skip login again after register success");
                        return mUser.doActionUserLoginInternet(email, password, false, true);
                    } else {
                        log.debug("register Skip account failed");
                        return result;
                    }
            }
            return null;
        }
        
        private boolean register(String username, String email, String password)
        {
            log.debug("Skip register");
            EspRegisterResult result = mUser.doActionUserRegisterInternet(username, email, password);
            switch(result) {
                case SUC:
                    return true;
                default:
                    return false;
            }
        }
        
        @Override
        protected void onPostExecute(EspLoginResult result)
        {
            mDialog.dismiss();
            mDialog = null;
            
            switch (result)
            {
                case SUC:
                    loginSuccess(R.string.esp_login_result_success);
                    break;
                case NETWORK_UNACCESSIBLE:
                case NOT_REGISTER:
                case PASSWORD_ERR:
                    loginFailed(R.string.esp_skip_result_failed);
                    break;
            }
        }
    }
}
