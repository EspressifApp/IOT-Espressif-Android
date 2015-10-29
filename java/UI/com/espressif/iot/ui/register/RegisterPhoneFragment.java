package com.espressif.iot.ui.register;

import com.espressif.iot.R;
import com.espressif.iot.model.user.EspCaptcha;
import com.espressif.iot.type.user.EspRegisterResult;
import com.espressif.iot.user.IEspUser;
import com.espressif.iot.user.builder.BEspUser;
import com.espressif.iot.util.EspStrings;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class RegisterPhoneFragment extends Fragment implements OnClickListener
{
    public static final String TAG = "RegisterPhoneFragment";
    
    private IEspUser mUser;
    
    private RegisterActivity mActivity;
    
    private EditText mPhoneET;
    private EditText mCaptchaCodeET;
    private EditText mPasswordET;
    private EditText mPasswordAgainET;
    
    private Button mGetCaptchaBtn;
    
    private TextView mWithEmailTV;
    
    private Button mCancelBtn;
    private Button mRegisterBtn;
    
    private int mCaptchaCounter;
    
    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        
        mActivity = (RegisterActivity)activity;
        mUser = BEspUser.getBuilder().getInstance();
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.register_phone_fragment , container, false);
        
        mPhoneET = (EditText)view.findViewById(R.id.register_phone);
        mCaptchaCodeET = (EditText)view.findViewById(R.id.register_captcha_code);
        mPasswordET = (EditText)view.findViewById(R.id.register_password);
        mPasswordAgainET = (EditText)view.findViewById(R.id.register_password_again);
        
        mGetCaptchaBtn = (Button)view.findViewById(R.id.register_get_captcha_btn);
        mGetCaptchaBtn.setOnClickListener(this);
        
        mWithEmailTV = (TextView)view.findViewById(R.id.register_with_email);
        mWithEmailTV.setOnClickListener(this);
         
        mCancelBtn = (Button)view.findViewById(R.id.register_cancel);
        mCancelBtn.setOnClickListener(this);
        mRegisterBtn = (Button)view.findViewById(R.id.register_register);
        mRegisterBtn.setOnClickListener(this);
        
        return view;
    }

    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
        
        mGetCaptchaBtn.removeCallbacks(mUpdateTimerRun);
    }
    
    @Override
    public void onClick(View v)
    {
        if (v == mGetCaptchaBtn)
        {
            if (!checkPhoneNumber())
            {
                return;
            }
            
            new AsyncTask<String, Void, Boolean>()
            {
                @Override
                protected Boolean doInBackground(String... params)
                {
                    String number = params[0];
                    return mUser.doActionGetSmsCaptchaCode(number, EspCaptcha.STATE_REGISTER);
                }
            }.execute(mPhoneET.getText().toString());
            
            mCaptchaCounter = 60;
            mGetCaptchaBtn.post(mUpdateTimerRun);
            mGetCaptchaBtn.setEnabled(false);
        }
        else if (v == mWithEmailTV)
        {
            mActivity.showFragment(RegisterEmailFragment.TAG);
        }
        else if (v == mCancelBtn)
        {
            mActivity.finish();
        }
        else if (v == mRegisterBtn)
        {
            registerPhone();
        }
    }
    
    private void registerPhone()
    {
        if (!checkPhoneNumber())
        {
            return;
        }
        if (!checkCaptchaCode())
        {
            return;
        }
        if (!mActivity.checkPassword(mPasswordET, mPasswordAgainET))
        {
            return;
        }
        
        final String phoneNumber = mPhoneET.getText().toString();
        final String captchaCode = mCaptchaCodeET.getText().toString();
        final String userPassword = mPasswordET.getText().toString();
        new RegisterTaskAbs(getActivity())
        {
            
            @Override
            protected EspRegisterResult doRegister()
            {
                return mUser.doActionUserRegisterPhone(phoneNumber, captchaCode, userPassword);
            }
            
            @Override
            protected void registerResult(EspRegisterResult result) {
                switch (result)
                {
                    case SUC:
                        registerSuccess(phoneNumber, userPassword);
                        break;
                    case NETWORK_UNACCESSIBLE:
                    case CONTENT_FORMAT_ERROR:
                    case USER_OR_EMAIL_EXIST_ALREADY:
                        registerFailed(result);
                        break;
                }
            }
        }.execute();
    }
    
    private Runnable mUpdateTimerRun = new Runnable()
    {
        
        @Override
        public void run()
        {
            mGetCaptchaBtn.setText(getString(R.string.esp_register_get_sms_captcha_code_again, --mCaptchaCounter));
            if (mCaptchaCounter > 0)
            {
                mGetCaptchaBtn.postDelayed(mUpdateTimerRun, 1000);
            }
            else
            {
                mGetCaptchaBtn.setText(R.string.esp_register_get_sms_captcha_code);
                mGetCaptchaBtn.setEnabled(true);
            }
        }
    };
    
    private boolean checkPhoneNumber()
    {
        String phoneNumber = mPhoneET.getText().toString();
        if (TextUtils.isEmpty(phoneNumber))
        {
            Toast.makeText(getActivity(), R.string.esp_register_phone_number_toast, Toast.LENGTH_SHORT)
                .show();
            return false;
        }
        else
        {
            return true;
        }
    }
    
    private boolean checkCaptchaCode()
    {
        String captchaCode = mCaptchaCodeET.getText().toString();
        if (TextUtils.isEmpty(captchaCode))
        {
            Toast.makeText(getActivity(), R.string.esp_register_captcha_code_toast, Toast.LENGTH_SHORT)
            .show();
            return false;
        }
        else
        {
            return true;
        }
    }
    
    private void registerSuccess(String phoneNumber, String password)
    {
        Intent data = new Intent();
        data.putExtra(EspStrings.Key.REGISTER_NAME_PHONE, phoneNumber);
        data.putExtra(EspStrings.Key.REGISTER_NAME_PASSWORD, password);
        mActivity.registerSuccess(R.string.esp_register_result_success, data);
    }
    
    private void registerFailed(EspRegisterResult result)
    {
        switch (result)
        {
            case NETWORK_UNACCESSIBLE:
                mActivity.registerFailed(R.string.esp_register_result_network_unaccessible);
                break;
            case CONTENT_FORMAT_ERROR:
                mActivity.registerFailed(R.string.esp_register_result_phont_code_error);
                break;
            case USER_OR_EMAIL_EXIST_ALREADY:
                mActivity.registerFailed(R.string.esp_register_result_phone_exist);
                break;
            case SUC:
                break;
        }
    }
}
