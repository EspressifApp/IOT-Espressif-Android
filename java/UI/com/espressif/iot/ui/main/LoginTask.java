package com.espressif.iot.ui.main;

import com.espressif.iot.R;
import com.espressif.iot.type.user.EspLoginResult;
import com.espressif.iot.user.IEspUser;
import com.espressif.iot.user.builder.BEspUser;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

public class LoginTask extends AsyncTask<Void, Void, EspLoginResult>
{
    private Context mContext;
    
    private String mEmail;
    
    private String mPassword;
    
    private boolean mAutoLogin;
    
    private ProgressDialog mDialog;
    
    public LoginTask(Context context, String email, String password, boolean autoLogin)
    {
        mContext = context;
        mEmail = email;
        mPassword = password;
        mAutoLogin = autoLogin;
    }
    
    @Override
    protected void onPreExecute()
    {
        mDialog = new ProgressDialog(mContext);
        mDialog.setMessage(mContext.getString(R.string.esp_login_progress_message));
        mDialog.setCancelable(false);
        mDialog.show();
    }
    
    @Override
    protected EspLoginResult doInBackground(Void... params)
    {
        IEspUser user = BEspUser.getBuilder().getInstance();
        return user.doActionUserLoginInternet(mEmail, mPassword, false, mAutoLogin);
    }
    
    @Override
    protected void onPostExecute(EspLoginResult result)
    {
        mDialog.dismiss();
        mDialog = null;
        
        int msgRes = 0;
        switch (result)
        {
            case SUC:
                msgRes = R.string.esp_login_result_success;
                break;
            case NETWORK_UNACCESSIBLE:
                msgRes = R.string.esp_login_result_network_unaccessible;
                break;
            case NOT_REGISTER:
                msgRes = R.string.esp_login_result_not_register;
                break;
            case PASSWORD_ERR:
                msgRes = R.string.esp_login_result_password_error;
                break;
        }
        
        Toast.makeText(mContext, msgRes, Toast.LENGTH_LONG).show();
        
        loginResult(result);
    }
    
    public void loginResult(EspLoginResult result)
    {
    }
}
