package com.espressif.iot.ui.register;

import com.espressif.iot.R;
import com.espressif.iot.base.api.EspBaseApiUtil;
import com.espressif.iot.type.user.EspRegisterResult;
import com.espressif.iot.user.IEspUser;
import com.espressif.iot.user.builder.BEspUser;
import com.espressif.iot.util.AccountUtil;
import com.espressif.iot.util.EspStrings;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class RegisterEmailFragment extends Fragment implements OnClickListener, OnFocusChangeListener
{
    public static final String TAG = "RegisterEmailFragment";
    
    private IEspUser mUser;
    
    private EditText mUsernameEdt;
    private EditText mEmailEdt;
    private EditText mPasswordEdt;
    private EditText mPasswordAgainEdt;
    private TextView mWithPhoneTV;
    
    private Button mCancelBtn;
    private Button mRegisterBtn;
    
    private Handler mHandler;
    
    private static final int FIND_USERNAME_EXIST = 0;
    private static final int FIND_EMAIL_EXIST = 1;
    
    private RegisterActivity mActivity;
    
    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        
        mActivity = (RegisterActivity)activity;
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        mUser = BEspUser.getBuilder().getInstance();
        mHandler = new Handler();
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.register_email_fragment, container, false);
        
        mUsernameEdt = (EditText)view.findViewById(R.id.register_username);
        mUsernameEdt.addTextChangedListener(new FilterSpaceTextListener(mUsernameEdt));
        mUsernameEdt.setOnFocusChangeListener(this);
        
        mEmailEdt = (EditText)view.findViewById(R.id.register_email);
        mEmailEdt.addTextChangedListener(new FilterSpaceTextListener(mEmailEdt));
        mEmailEdt.setOnFocusChangeListener(this);
        
        mPasswordEdt = (EditText)view.findViewById(R.id.register_password);
        mPasswordAgainEdt = (EditText)view.findViewById(R.id.register_password_again);
        
        mWithPhoneTV = (TextView)view.findViewById(R.id.register_with_phone);
        mWithPhoneTV.setOnClickListener(this);
        mWithPhoneTV.setVisibility(View.GONE);
        
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
        
        if (mUsernameEdt.getTag() != null)
        {
            ((FindAccountRunnable)mUsernameEdt.getTag()).cancel();
            mUsernameEdt.setTag(null);
        }
        if (mEmailEdt.getTag() != null)
        {
            ((FindAccountRunnable)mEmailEdt.getTag()).cancel();
            mEmailEdt.setTag(null);
        }
    }
    
    @Override
    public void onClick(View v)
    {
        if (v == mCancelBtn)
        {
            getActivity().finish();
        }
        else if (v == mRegisterBtn)
        {
            registerEmail();
        }
        else if (v == mWithPhoneTV)
        {
            mActivity.showFragment(RegisterPhoneFragment.TAG);
        }
    }
    
    private void registerEmail()
    {
        if (!checkAccount())
        {
            return;
        }
        if (mUsernameEdt.hasFocus())
        {
            mUsernameEdt.clearFocus();
        }
        
        if (!mActivity.checkPassword(mPasswordEdt, mPasswordAgainEdt))
        {
            return;
        }
        
        if (mEmailEdt.hasFocus())
        {
            mEmailEdt.clearFocus();
        }
        
        final String username = mUsernameEdt.getText().toString();
        final String email = mEmailEdt.getText().toString();
        final String password = mPasswordEdt.getText().toString();
        new RegisterTaskAbs(getActivity())
        {
            
            @Override
            protected EspRegisterResult doRegister()
            {
                return mUser.doActionUserRegisterInternet(username, email, password);
            }
            
            @Override
            protected void registerResult(EspRegisterResult result)
            {
                switch (result)
                {
                    case SUC:
                        registerSuccess(email, password);
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

    @Override
    public void onFocusChange(View v, boolean hasFocus)
    {
        if (v == mUsernameEdt)
        {
            if (!hasFocus)
            {
                findAccount(mUsernameEdt, FIND_USERNAME_EXIST);
            }
        }
        else if (v == mEmailEdt)
        {
            if (!hasFocus)
            {
                findAccount(mEmailEdt, FIND_EMAIL_EXIST);
            }
        }
    }
    
    /**
     * Check the account has be registered or not
     * 
     * @param editText
     * @param type @see #FIND_EMAIL_EXIST and #FIND_USERNAME_EXIST
     */
    private void findAccount(EditText editText, int type)
    {
        if (!TextUtils.isEmpty(editText.getText().toString()))
        {
            if (editText.getTag() != null)
            {
                FindAccountRunnable lastRunnable = (FindAccountRunnable)editText.getTag();
                lastRunnable.cancel();
            }
            FindAccountRunnable findRunnable = new FindAccountRunnable(type);
            editText.setTag(findRunnable);
            EspBaseApiUtil.submit(findRunnable);
        }
    }
    
    private class FindAccountRunnable implements Runnable
    {
        final int mType;
        
        private volatile boolean mCancel;
        
        public FindAccountRunnable(int type)
        {
            mType = type;
            
            mCancel = false;
        }
        
        @Override
        public void run()
        {
            boolean result;
            switch (mType)
            {
                case FIND_USERNAME_EXIST:
                    if (mCancel)
                    {
                        return;
                    }
                    result = mUser.findAccountUsernameRegistered(mUsernameEdt.getText().toString());
                    if (mCancel)
                    {
                        return;
                    }
                    mHandler.post(new FindAccountResultUIRunnable(mUsernameEdt, result));
                    break;
                case FIND_EMAIL_EXIST:
                    if (mCancel)
                    {
                        return;
                    }
                    result = mUser.findAccountEmailRegistered(mEmailEdt.getText().toString());
                    if (mCancel)
                    {
                        return;
                    }
                    mHandler.post(new FindAccountResultUIRunnable(mEmailEdt, result));
                    break;
            }
        }
        
        public void cancel()
        {
            mCancel = true;
        }
    }
    
    private class FindAccountResultUIRunnable implements Runnable
    {
        private EditText mEditText;
        private boolean mResult;
        
        public FindAccountResultUIRunnable(EditText editText, boolean result)
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
            }
            else
            {
                drawables[2] = null;
            }
            
            mEditText.setCompoundDrawablesWithIntrinsicBounds(drawables[0], drawables[1], drawables[2], drawables[3]);
            mRegisterBtn.setEnabled(!mResult);
            mEditText.setTag(null);
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
        CharSequence username = mUsernameEdt.getText();
        CharSequence email = mEmailEdt.getText();
        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(email))
        {
            Toast.makeText(getActivity(), R.string.esp_register_account_email_toast, Toast.LENGTH_LONG).show();
            return false;
        }
        else if (!AccountUtil.isEmail(email.toString()))
        {
            Toast.makeText(getActivity(), R.string.esp_register_account_email_format_error, Toast.LENGTH_LONG).show();
            return false;
        }
        else
        {
            return true;
        }
    }
    
    private void registerSuccess(String email, String password)
    {
        Intent data = new Intent();
        data.putExtra(EspStrings.Key.REGISTER_NAME_EMAIL, email);
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
                mActivity.registerFailed(R.string.esp_register_result_account_format_error);
                break;
            case USER_OR_EMAIL_EXIST_ALREADY:
                mActivity.registerFailed(R.string.esp_register_result_account_exist);
                break;
            case SUC:
                break;
        }
    }
}
