package com.espressif.iot.ui.welcome;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.espressif.iot.R;
import com.espressif.iot.type.user.EspRegisterResult;
import com.espressif.iot.ui.view.EspPagerAdapter;
import com.espressif.iot.ui.view.EspViewPager;
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
import android.support.v4.view.PagerAdapter;
import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

public class RegisterActivity extends Activity
{
    
    private static final Logger log = Logger.getLogger(RegisterActivity.class);
    
    private EditText mAccountEdt;
    
    private EditText mPasswordEdt;
    
    private EditText mPasswordAgainEdt;
    
    private EditText mEmailEdt;
    
    private EspViewPager mPager;
    
    private List<View> mPagerViewList;
    
    private PagerAdapter mPagerAdapter;
    
    private static final int PAGE_COUNT = 3;
    private static final int PAGE_ACCOUNT_INDEX = 0;
    private static final int PAGE_PASSWORD_INDEX = 1;
    private static final int PAGE_CONFIRM_INDEX = 2;
    
    private static final int PASSOWRD_WORDS_NUMBER_MIN = 6;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_activity);
        init();
    }
    
    private void init()
    {
        mPager = (EspViewPager)findViewById(R.id.register_pager);
        mPagerViewList = new ArrayList<View>();
        initPagerItem();
        mPagerAdapter = new EspPagerAdapter(mPagerViewList);
        mPager.setAdapter(mPagerAdapter);
    }
    
    private void initPagerItem()
    {
        LayoutInflater inflater = getLayoutInflater();
        
        for (int i = 0; i < PAGE_COUNT; i++)
        {
            View pagerItem = inflater.inflate(R.layout.register_pager_item, null);
            EditText et1 = (EditText)pagerItem.findViewById(R.id.register_pager_item_edit_1);
            EditText et2 = (EditText)pagerItem.findViewById(R.id.register_pager_item_edit_2);
            Button btn1 = (Button)pagerItem.findViewById(R.id.register_pager_item_btn_1);
            Button btn2 = (Button)pagerItem.findViewById(R.id.register_pager_item_btn_2);
            ImageView topImg = (ImageView)pagerItem.findViewById(R.id.register_pager_item_top_img);
            
            switch (i)
            {
                case PAGE_ACCOUNT_INDEX:
                    topImg.setBackgroundResource(R.drawable.esp_register_top_1);
                    
                    mAccountEdt = et1;
                    Drawable accountIcon = getResources().getDrawable(R.drawable.esp_register_icon_account);
                    mAccountEdt.setCompoundDrawablesWithIntrinsicBounds(accountIcon, null, null, null);
                    mAccountEdt.setHint(R.string.esp_register_account);
                    
                    mEmailEdt = et2;
                    Drawable emailIcon = getResources().getDrawable(R.drawable.esp_register_icon_email);
                    mEmailEdt.setCompoundDrawablesWithIntrinsicBounds(emailIcon, null, null, null);
                    mEmailEdt.setHint(R.string.esp_register_email);
                    mEmailEdt.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
                    
                    btn1.setText(android.R.string.cancel);
                    btn1.setOnClickListener(mFinishListener);
                    
                    btn2.setText(R.string.esp_register_next);
                    btn2.setOnClickListener(mNextPageListener);
                    break;
                case PAGE_PASSWORD_INDEX:
                    topImg.setBackgroundResource(R.drawable.esp_register_top_2);
                    
                    mPasswordEdt = et1;
                    Drawable passwordIcon = getResources().getDrawable(R.drawable.esp_register_icon_password);
                    mPasswordEdt.setCompoundDrawablesWithIntrinsicBounds(passwordIcon, null, null, null);
                    mPasswordEdt.setHint(R.string.esp_register_input_password);
                    mPasswordEdt.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    
                    mPasswordAgainEdt = et2;
                    mPasswordAgainEdt.setCompoundDrawablesWithIntrinsicBounds(passwordIcon, null, null, null);
                    mPasswordAgainEdt.setHint(R.string.esp_register_input_password_again);
                    mPasswordAgainEdt.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    
                    btn1.setText(R.string.esp_register_prev);
                    btn1.setOnClickListener(mPrevPageListener);
                    
                    btn2.setText(R.string.esp_register_next);
                    btn2.setOnClickListener(mNextPageListener);
                    break;
                case PAGE_CONFIRM_INDEX:
                    topImg.setBackgroundResource(R.drawable.esp_register_top_3);
                    
                    et1.setVisibility(View.GONE);
                    et2.setVisibility(View.GONE);
                    
                    pagerItem.findViewById(R.id.register_pager_item_head_edit).setVisibility(View.VISIBLE);
                    
                    btn1.setText(R.string.esp_register_prev);
                    btn1.setOnClickListener(mPrevPageListener);
                    
                    btn2.setText(R.string.esp_register_register);
                    btn2.setOnClickListener(mRegisterListener);
                    break;
            }
            
            mPagerViewList.add(pagerItem);
        }
    }
    
    private View.OnClickListener mFinishListener = new OnClickListener()
    {
        
        @Override
        public void onClick(View v)
        {
            finish();
        }
    };
    
    private OnClickListener mNextPageListener = new OnClickListener()
    {
        
        @Override
        public void onClick(View v)
        {
            int currentPage = mPager.getCurrentItem();
            switch (currentPage)
            {
                case PAGE_ACCOUNT_INDEX:
                    if (!checkAccount())
                    {
                        Toast.makeText(RegisterActivity.this,
                            R.string.esp_register_account_email_toast,
                            Toast.LENGTH_LONG).show();
                        return;
                    }
                    break;
                case PAGE_PASSWORD_INDEX:
                    if (mPasswordEdt.getText().length() < PASSOWRD_WORDS_NUMBER_MIN)
                    {
                        Toast.makeText(RegisterActivity.this, R.string.esp_register_input_password, Toast.LENGTH_LONG)
                            .show();
                        return;
                    }
                    
                    if (!checkPassword())
                    {
                        Toast.makeText(RegisterActivity.this,
                            R.string.esp_register_same_password_toast,
                            Toast.LENGTH_LONG).show();
                        return;
                    }
                    break;
                case PAGE_CONFIRM_INDEX:
                    break;
            }
            mPager.setCurrentItem(mPager.getCurrentItem() + 1, true);
        }
    };
    
    private OnClickListener mPrevPageListener = new OnClickListener()
    {
        
        @Override
        public void onClick(View v)
        {
            mPager.setCurrentItem(mPager.getCurrentItem() - 1, true);
        }
    };
    
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
    
    private OnClickListener mRegisterListener = new OnClickListener()
    {
        
        @Override
        public void onClick(View v)
        {
            String username = mAccountEdt.getText().toString();
            String email = mEmailEdt.getText().toString();
            String password = mPasswordEdt.getText().toString();
            new RegisterTask(username, email, password).execute();
        }
    };
    
    private class RegisterTask extends AsyncTask<Void, Void, EspRegisterResult>
    {
        
        private IEspUser mUser;
        
        private Context mContext;
        
        private String mUserName;
        
        private String mEmail;
        
        private String mPassword;
        
        private ProgressDialog mDialog;
        
        public RegisterTask(String userName, String email, String password)
        {
            mUser = BEspUser.getBuilder().getInstance();
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
}
