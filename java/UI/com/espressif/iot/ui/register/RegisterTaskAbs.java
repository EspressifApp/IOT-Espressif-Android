package com.espressif.iot.ui.register;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import com.espressif.iot.R;
import com.espressif.iot.type.user.EspRegisterResult;

public abstract class RegisterTaskAbs extends AsyncTask<Void, Void, EspRegisterResult>
{
    private Context mContext;
    
    private ProgressDialog mDialog;
    
    public RegisterTaskAbs(Context context)
    {
        mContext = context;
    }
    
    @Override
    protected void onPreExecute()
    {
        mDialog = new ProgressDialog(mContext);
        mDialog.setMessage(mContext.getString(R.string.esp_register_progress_message));
        mDialog.setCancelable(false);
        mDialog.show();
    }
    
    @Override
    protected EspRegisterResult doInBackground(Void... params)
    {
        return doRegister();
    }
    
    @Override
    protected void onPostExecute(EspRegisterResult result)
    {
        mDialog.dismiss();
        mDialog = null;
        
        registerResult(result);
    }
    
    protected void registerResult(EspRegisterResult result)
    {
    }
    
    protected abstract EspRegisterResult doRegister();
}
