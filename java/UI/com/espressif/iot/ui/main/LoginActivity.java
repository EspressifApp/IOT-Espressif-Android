package com.espressif.iot.ui.main;

import java.util.HashMap;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.framework.ShareSDK;

import com.espressif.iot.R;
import com.espressif.iot.base.application.EspApplication;
import com.espressif.iot.sharesdk.PlatformThirdParty;
import com.espressif.iot.type.user.EspLoginResult;
import com.espressif.iot.ui.softap_sta_support.SoftApStaSupportActivity;
import com.espressif.iot.ui.softap_sta_support.help.HelpSoftApStaSupportActivity;
import com.espressif.iot.ui.view.NoBgDialog;
import com.espressif.iot.user.IEspUser;
import com.espressif.iot.user.builder.BEspUser;
import com.espressif.iot.util.EspStrings;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.EditText;

public class LoginActivity extends Activity implements OnClickListener, PlatformActionListener
{
    private IEspUser mUser;
    
    private EditText mEmailEdt;
    private EditText mPasswordEdt;
    
    private CheckBox mAutoLoginCB;
    
    private Button mLoginBtn;
    private Button mRegisterBtn;
    private Button mLocalBtn;
    private Button mQuickUsageBtn;
    private TextView mThirdPartyLoginTV;
    
    private final static int REQUEST_REGISTER = 1;
    
    private Platform mWeibo;
    private Platform mWeChat;
    private Platform mQQ;
    
    private TextView mWeiboTV;
    private TextView mWeChatTV;
    private TextView mQQTV;
    
    private NoBgDialog mThirdPartyLoginDialog;
    
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
        
        mLocalBtn = (Button)findViewById(R.id.login_local);
        mLocalBtn.setOnClickListener(this);
        
        // listen the auto login event
        mAutoLoginCB = (CheckBox)findViewById(R.id.login_cb_auto_login);
        
        mQuickUsageBtn = (Button)findViewById(R.id.login_btn_quick_usage);
        mQuickUsageBtn.setOnClickListener(this);
        
        mThirdPartyLoginTV = (TextView)findViewById(R.id.login_text_third_party);
        mThirdPartyLoginTV.setOnClickListener(this);
        mThirdPartyLoginTV.setVisibility(View.GONE);
        
        ShareSDK.initSDK(this);
        mWeibo = ShareSDK.getPlatform(PlatformThirdParty.NAME_WEIBO);
        mWeibo.setPlatformActionListener(this);
        mWeChat = ShareSDK.getPlatform(PlatformThirdParty.NAME_WECHAT);
        mWeChat.setPlatformActionListener(this);
        mQQ = ShareSDK.getPlatform(PlatformThirdParty.NAME_QQ);
        mQQ.setPlatformActionListener(this);
        
        LayoutInflater inflater = LayoutInflater.from(this);
        View content = inflater.inflate(R.layout.third_party_login_dialog, null);
        mWeiboTV = (TextView)content.findViewById(R.id.weibo_login);
        mWeiboTV.setOnClickListener(mThirdPartyAuthClickListener);
        mWeChatTV = (TextView)content.findViewById(R.id.wechat_login);
        mWeChatTV.setOnClickListener(mThirdPartyAuthClickListener);
        mQQTV = (TextView)content.findViewById(R.id.qq_login);
        mQQTV.setOnClickListener(mThirdPartyAuthClickListener);
        mThirdPartyLoginDialog = new NoBgDialog(this);
        mThirdPartyLoginDialog.setView(content);
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
    
    private OnClickListener mThirdPartyAuthClickListener = new OnClickListener()
    {
        
        @Override
        public void onClick(View v)
        {
            Platform platform = null;
            
            switch(v.getId())
            {
                case R.id.weibo_login:
                    platform = mWeibo;
                    break;
                case R.id.wechat_login:
                    platform = mWeChat;
                    break;
                case R.id.qq_login:
                    platform = mQQ;
                    break;
            }
            
            if (platform.isAuthValid())
            {
                String state = PlatformThirdParty.convertShareSDKNameToEspState(platform.getName());
                String token = platform.getDb().getToken();
                thirdPartyLogin(state, token);
            }
            else
            {
                platform.authorize();
            }
            mThirdPartyLoginDialog.dismiss();
        }
    };
    
    private Handler mHandler = new Handler();
    
    @Override
    public void onComplete(Platform platform, int action, HashMap<String, Object> res)
    {
        final String state = PlatformThirdParty.convertShareSDKNameToEspState(platform.getName());
        final String token = platform.getDb().getToken();
        
        mHandler.post(new Runnable()
        {
            @Override
            public void run()
            {
                thirdPartyLogin(state, token);
            }
        });
    }
    
    @Override
    public void onCancel(Platform platform, int action)
    {
    }

    @Override
    public void onError(Platform platform, int action, Throwable t)
    {
        final String platName = platform.getName();
        mHandler.post(new Runnable()
        {
            @Override
            public void run()
            {
                Toast.makeText(getBaseContext(),
                    getString(R.string.esp_third_party_get_token_failed, platName),
                    Toast.LENGTH_LONG).show();
            }
        });
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
        String email = mEmailEdt.getText().toString();
        String password = mPasswordEdt.getText().toString();
        boolean autoLogin = mAutoLoginCB.isChecked();
        new LoginTask(this, email, password, autoLogin)
        {
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
    
    private void thirdPartyLogin(String state, String token)
    {
        NameValuePair pair = new BasicNameValuePair(state, token);
        new LoginTask(this)
        {
            @Override
            public void loginResult(EspLoginResult result)
            {
                if (result == EspLoginResult.SUC)
                {
                    loginSuccess();
                }
            }
        }.execute(pair);
    }
    
    private void loginSuccess()
    {
        Intent intent = new Intent(this, EspApplication.getEspUIActivity());
        startActivity(intent);
        finish();
    }
}
