package com.espressif.iot.ui.register;

import org.apache.log4j.Logger;

import com.espressif.iot.R;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

public class RegisterActivity extends Activity
{
    private final Logger log = Logger.getLogger(getClass());
    
    private static final int PASSOWRD_WORDS_NUMBER_MIN = 6;
    
    private FragmentManager mFragmentManager;
    
    private RegisterEmailFragment mEmailFragment;
    private RegisterPhoneFragment mPhoneFragment;
    
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.register_activity);
        
        mFragmentManager = getFragmentManager();
        mEmailFragment = new RegisterEmailFragment();
        mPhoneFragment = new RegisterPhoneFragment();
        mFragmentManager.beginTransaction()
            .add(R.id.fragment_container, mEmailFragment)
            .add(R.id.fragment_container, mPhoneFragment)
            .show(mEmailFragment)
            .hide(mPhoneFragment)
            .commit();
    }
    
    /**
     * Show the target Fragment
     * 
     * @param tag Use RegisterEmailFragment.TAG or RegisterPhoneFragment.TAG
     */
    public void showFragment(String tag)
    {
        Fragment showFg = null;
        Fragment hideFg = null;
        if (tag.equals(RegisterEmailFragment.TAG))
        {
            showFg = mEmailFragment;
            hideFg = mPhoneFragment;
        }
        else if (tag.equals(RegisterPhoneFragment.TAG))
        {
            showFg = mPhoneFragment;
            hideFg = mEmailFragment;
        }
        
        mFragmentManager.beginTransaction().show(showFg).hide(hideFg).commit();
    }
    
    public void registerSuccess(int msg, Intent data)
    {
        log.debug("registerSuccess");
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
        setResult(Activity.RESULT_OK, data);
        finish();
    }
    
    public void registerFailed(int msg)
    {
        log.debug("registerFailed");
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }
    
    /**
     * Check password
     * 
     * @return
     */
    public boolean checkPassword(TextView passwordTV, TextView passwordAgainTV)
    {
        final String password = passwordTV.getText().toString();
        final String passwordAgain = passwordAgainTV.getText().toString();
        
        if (password.length() < PASSOWRD_WORDS_NUMBER_MIN)
        {
            // The password is too short
            Toast.makeText(this, R.string.esp_register_input_password, Toast.LENGTH_LONG).show();
            return false;
        }
        
        if (password.equals(passwordAgain))
        {
            // The password is legal
            return true;
        }
        else
        {
            // Two passwords is not same
            Toast.makeText(this, R.string.esp_register_same_password_toast, Toast.LENGTH_LONG).show();
            return false;
        }
    }
}
