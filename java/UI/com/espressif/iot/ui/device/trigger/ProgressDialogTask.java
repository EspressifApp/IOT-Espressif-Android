package com.espressif.iot.ui.device.trigger;

import com.espressif.iot.R;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;

abstract class ProgressDialogTask<Params, Progress, Result> extends AsyncTask<Params, Progress, Result> {
    protected Activity mContext;
    protected ProgressDialog mDialog;
    
    public ProgressDialogTask(Activity activity) {
        mContext = activity;
        mDialog = new ProgressDialog(mContext);
        mDialog.setMessage(mContext.getString(R.string.esp_device_task_dialog_message));
        mDialog.setCancelable(false);
    }
    
    @Override
    protected void onPreExecute() {
        mDialog.show();
    }
    
    @Override
    protected void onPostExecute(Result result) {
        if (mDialog != null) {
            mDialog.dismiss();
            mDialog = null;
        }
    }
}