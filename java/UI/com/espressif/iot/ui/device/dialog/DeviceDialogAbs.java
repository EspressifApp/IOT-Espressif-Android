package com.espressif.iot.ui.device.dialog;

import java.util.List;
import java.util.Vector;

import com.espressif.iot.R;
import com.espressif.iot.device.IEspDevice;
import com.espressif.iot.type.device.IEspDeviceStatus;
import com.espressif.iot.user.IEspUser;
import com.espressif.iot.user.builder.BEspUser;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public abstract class DeviceDialogAbs implements EspDeviceDialogInterface, DialogInterface.OnDismissListener,
    DialogInterface.OnCancelListener
{
    protected IEspUser mUser;
    
    protected Context mContext;
    
    protected IEspDevice mDevice;
    
    protected AlertDialog mDialog;
    
    protected View mProgressContainer;
    
    private List<StatusTask> mTaskList;
    
    public DeviceDialogAbs(Context context, IEspDevice device)
    {
        mUser = BEspUser.getBuilder().getInstance();
        mContext = context;
        mDevice = device;
        
        mTaskList = new Vector<StatusTask>();
    }
    
    @Override
    public void cancel()
    {
        if (mDialog != null)
        {
            mDialog.cancel();
        }
    }
    
    @Override
    public void dismiss()
    {
        if (mDialog != null)
        {
            mDialog.dismiss();
        }
    }

    @Override
    public void show()
    {
        LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.device_dialog_abs, null);
        
        mProgressContainer = view.findViewById(R.id.progress_container);
        mProgressContainer.setVisibility(View.GONE);
        
        ViewGroup contentView = (ViewGroup)view.findViewById(R.id.device_dialog_content);
        contentView.addView(getContentView(inflater));
        
        mDialog =
            new AlertDialog.Builder(mContext).setTitle(mDevice.getName())
                .setView(view)
                .setNegativeButton(R.string.esp_sss_device_dialog_exit, null)
                .show();
        mDialog.setOnDismissListener(this);
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.setOnCancelListener(this);
        
        new StatusTask().execute();
    }
    
    @Override
    public void onCancel(DialogInterface dialog)
    {
        for (StatusTask task : mTaskList)
        {
            task.cancel(true);
        }
        
        mTaskList.clear();
    }
    
    @Override
    public void onDismiss(DialogInterface dialog)
    {
        if (mOnDissmissedListener != null)
        {
            mOnDissmissedListener.onDissmissed(this);
        }
    }
    
    protected class StatusTask extends AsyncTask<IEspDeviceStatus, Void, Boolean>
    {
        private boolean mBroadcast;
        
        public StatusTask()
        {
            mBroadcast = false;
        }

        public StatusTask(boolean broadcast)
        {
            mBroadcast = broadcast;
        }
        
        @Override
        protected void onPreExecute()
        {
            mTaskList.add(this);
            mProgressContainer.setVisibility(View.VISIBLE);
        }
        
        @Override
        protected Boolean doInBackground(IEspDeviceStatus... params)
        {
            if (params.length > 0)
            {
                IEspDeviceStatus status = params[0];
                return mUser.doActionPostDeviceStatus(mDevice, status, mBroadcast);
            }
            else
            {
                return mUser.doActionGetDeviceStatus(mDevice);
            }
        }
        
        @Override
        protected void onPostExecute(Boolean result)
        {
            onExecuteEnd(result);
            
            mProgressContainer.setVisibility(View.GONE);
            
            mTaskList.remove(this);
        }
        
        @Override
        protected void onCancelled()
        {
        }
    }
    
    protected abstract void onExecuteEnd(boolean suc);
    
    protected abstract View getContentView(LayoutInflater inflater);
    
    private OnDissmissedListener mOnDissmissedListener;
    
    public void setOnDissmissedListener(OnDissmissedListener listener)
    {
        mOnDissmissedListener = listener;
    }
}
