package com.espressif.iot.ui.main;

import java.lang.ref.WeakReference;

import org.apache.log4j.Logger;

import com.espressif.iot.R;
import com.espressif.iot.base.api.EspBaseApiUtil;
import com.espressif.iot.type.user.EspRegisterResult;
import com.espressif.iot.user.IEspUser;
import com.espressif.iot.user.builder.BEspUser;
import com.espressif.iot.util.EspStrings;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class RegisterActivity extends Activity implements OnClickListener, OnFocusChangeListener
{
    private static final Logger log = Logger.getLogger(RegisterActivity.class);
    
    private IEspUser mUser;
    
    private EditText mAccountEdt;
    private EditText mEmailEdt;
    private EditText mPasswordEdt;
    private EditText mPasswordAgainEdt;
    
    private Button mCancelBtn;
    private Button mRegisterBtn;
    
    private static final int PASSOWRD_WORDS_NUMBER_MIN = 6;
    
    private RegisterHandler mHandler;
    
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.register_activity);
        
        mUser = BEspUser.getBuilder().getInstance();
        mHandler = new RegisterHandler(this);
        
        init();
    }
    
    private void init()
    {
        mAccountEdt = (EditText)findViewById(R.id.register_account);
        mAccountEdt.addTextChangedListener(new FilterSpaceTextListener(mAccountEdt));
        mAccountEdt.setOnFocusChangeListener(this);
        
        mEmailEdt = (EditText)findViewById(R.id.register_email);
        mEmailEdt.addTextChangedListener(new FilterSpaceTextListener(mEmailEdt));
        mEmailEdt.setOnFocusChangeListener(this);
        
        mPasswordEdt = (EditText)findViewById(R.id.register_password);
        mPasswordAgainEdt = (EditText)findViewById(R.id.register_password_again);
        
        mCancelBtn = (Button)findViewById(R.id.register_cancel);
        mCancelBtn.setOnClickListener(this);
        mRegisterBtn = (Button)findViewById(R.id.register_register);
        mRegisterBtn.setOnClickListener(this);
    }
    
    @Override
    public void onClick(View v)
    {
        if (v == mCancelBtn)
        {
            finish();
        }
        else if (v == mRegisterBtn)
        {
            if (!checkAccount())
            {
                Toast.makeText(RegisterActivity.this, R.string.esp_register_account_email_toast, Toast.LENGTH_LONG)
                    .show();
                return;
            }
            if (mAccountEdt.hasFocus())
            {
                mAccountEdt.clearFocus();
            }
            
            if (mPasswordEdt.getText().length() < PASSOWRD_WORDS_NUMBER_MIN)
            {
                Toast.makeText(RegisterActivity.this, R.string.esp_register_input_password, Toast.LENGTH_LONG).show();
                return;
            }
            if (!checkPassword())
            {
                Toast.makeText(RegisterActivity.this, R.string.esp_register_same_password_toast, Toast.LENGTH_LONG)
                    .show();
                return;
            }
            if (mEmailEdt.hasFocus())
            {
                mEmailEdt.clearFocus();
            }
            
            String username = mAccountEdt.getText().toString();
            String email = mEmailEdt.getText().toString();
            String password = mPasswordEdt.getText().toString();
            new RegisterTask(username, email, password).execute();
        }
    }
    
    @Override
    public void onFocusChange(View v, boolean hasFocus)
    {
        if (v == mAccountEdt)
        {
            if (!hasFocus)
            {
                if (!TextUtils.isEmpty(mAccountEdt.getText().toString()))
                {
                    EspBaseApiUtil.submit(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            boolean result = mUser.findAccountUsernameRegistered(mAccountEdt.getText().toString());
                            mHandler.post(new FindAccountResultRunnable(mAccountEdt, result));
                        }
                    });
                }
            }
        }
        else if (v == mEmailEdt)
        {
            if (!hasFocus)
            {
                if (!TextUtils.isEmpty(mEmailEdt.getText().toString()))
                {
                    EspBaseApiUtil.submit(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            boolean result = mUser.findAccountEmailRegistered(mEmailEdt.getText().toString());
                            mHandler.post(new FindAccountResultRunnable(mEmailEdt, result));
                        }
                    });
                }
            }
        }
    }
    
    private class FindAccountResultRunnable implements Runnable
    {
        private EditText mEditText;
        private boolean mResult;
        
        public FindAccountResultRunnable(EditText editText, boolean result)
        {
            mEditText = editText;
            mResult = result;
        }
        
        @Override
        public void run()
        {
            Drawable[] drawables = mEditText.getCompoundDrawables();
            if (mResult)
            {
                drawables[2] = mEditText.getContext().getResources().getDrawable(R.drawable.esp_register_icon_forbid);
                mEditText.setCompoundDrawablesWithIntrinsicBounds(drawables[0],
                    drawables[1],
                    drawables[2],
                    drawables[3]);
                mRegisterBtn.setEnabled(false);
            }
            else
            {
                drawables[2] = null;
                mEditText.setCompoundDrawablesWithIntrinsicBounds(drawables[0],
                    drawables[1],
                    drawables[2],
                    drawables[3]);
                mRegisterBtn.setEnabled(true);
            }
        }
        
    }
    
    private class FilterSpaceTextListener implements TextWatcher
    {
        private EditText mEditText;
        
        public FilterSpaceTextListener(EditText view)
        {
            mEditText = view;
        }
        
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after)
        {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count)
        {
        }

        @Override
        public void afterTextChanged(Editable s)
        {
            String space = " ";
            String str = s.toString();
            if (str.contains(space))
            {
                String newStr = str.replace(space, "");
                mEditText.setText(newStr);
                mEditText.setSelection(newStr.length());
            }
        }
        
    }
    
    /**
     * Check user name and email
     * 
     * @return
     */
    private boolean checkAccount()
    {
        CharSequence username = mAccountEdt.getText();
        CharSequence email = mEmailEdt.getText();
        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(email))
        {
            return false;
        }
        else
        {
            return true;
        }
    }
    
    /**
     * Check two input password same
     * 
     * @return
     */
    private boolean checkPassword()
    {
        final String password = mPasswordEdt.getText().toString();
        final String passwordAgain = mPasswordAgainEdt.getText().toString();
        if (TextUtils.isEmpty(password) && TextUtils.isEmpty(passwordAgain))
        {
            // passwords are both empty
            return true;
        }
        else if (!TextUtils.isEmpty(password) && TextUtils.isEmpty(passwordAgain))
        {
            // one password is empty
            return false;
        }
        else if (TextUtils.isEmpty(password) && !TextUtils.isEmpty(passwordAgain))
        {
            // one password is empty
            return false;
        }
        else
        {
            // passwords are both not empty
            return password.equals(passwordAgain);
        }
    }
    
    private class RegisterTask extends AsyncTask<Void, Void, EspRegisterResult>
    {
        private Context mContext;
        
        private String mUserName;
        
        private String mEmail;
        
        private String mPassword;
        
        private ProgressDialog mDialog;
        
        public RegisterTask(String userName, String email, String password)
        {
            mContext = RegisterActivity.this;
            mUserName = userName;
            mEmail = email;
            mPassword = password;
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
        protected EspRegisterResult doInBackground(Void... params)
        {
            return mUser.doActionUserRegisterInternet(mUserName, mEmail, mPassword);
        }
        
        @Override
        protected void onPostExecute(EspRegisterResult result)
        {
            mDialog.dismiss();
            mDialog = null;
            
            switch (result)
            {
                case SUC:
                    registerSuccess(R.string.esp_register_result_success, mEmail, mPassword);
                    break;
                case NETWORK_UNACCESSIBLE:
                    registerFailed(R.string.esp_register_result_network_unaccessible);
                    break;
                case USER_OR_EMAIL_ERR_FORMAT:
                    registerFailed(R.string.esp_register_result_account_format_error);
                    break;
                case USER_OR_EMAIL_EXIST_ALREADY:
                    registerFailed(R.string.esp_register_result_account_exist);
                    break;
            }
        }
    }
    
    private void registerSuccess(int msg, String email, String password)
    {
        log.debug("registerSuccess");
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
        Intent data = new Intent();
        data.putExtra(EspStrings.Key.REGISTER_NAME_EMAIL, email);
        data.putExtra(EspStrings.Key.REGISTER_NAME_PASSWORD, password);
        setResult(RESULT_OK, data);
        finish();
    }
    
    private void registerFailed(int msg)
    {
        log.debug("registerFailed");
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }
    
    private static class RegisterHandler extends Handler
    {
        private WeakReference<RegisterActivity> mActivity;
        
        public RegisterHandler(RegisterActivity activity)
        {
            mActivity = new WeakReference<RegisterActivity>(activity);
        }
        
        @Override
        public void handleMessage(Message msg)
        {
            RegisterActivity activity = mActivity.get();
            if (activity == null)
            {
                return;
            }
        }
    }
}
