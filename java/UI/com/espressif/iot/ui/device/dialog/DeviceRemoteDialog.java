package com.espressif.iot.ui.device.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.espressif.iot.R;
import com.espressif.iot.device.IEspDevice;
import com.espressif.iot.type.device.EspDeviceType;
import com.espressif.iot.type.device.status.EspStatusRemote;
import com.espressif.iot.type.device.status.IEspStatusRemote;

public class DeviceRemoteDialog extends DeviceDialogAbs implements EspDeviceDialogInterface, View.OnClickListener,
    OnDismissListener
{
    private AlertDialog mDialog;
    
    private View mProgressContainer;
    
    private EditText mAddressEdt;
    private EditText mCommandEdt;
    private EditText mRepeatEdt;
    
    private Button mConfirmBtn;
    private CheckBox mControlChildCB;
    
    public DeviceRemoteDialog(Context context, IEspDevice device)
    {
        super(context, device);
    }
    
    @Override
    public void show()
    {
        LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.device_dialog_remote, null);
        
        mProgressContainer = view.findViewById(R.id.progress_container);
        mProgressContainer.setVisibility(View.GONE);
        mProgressContainer.setOnClickListener(this);
        
        mAddressEdt = (EditText)view.findViewById(R.id.remote_address_edit);
        mCommandEdt = (EditText)view.findViewById(R.id.remote_command_edit);
        mRepeatEdt = (EditText)view.findViewById(R.id.remote_repeat_edit);
        
        mConfirmBtn = (Button)view.findViewById(R.id.remote_confirm_btn);
        mConfirmBtn.setOnClickListener(this);
        mControlChildCB = (CheckBox)view.findViewById(R.id.control_child_cb);
        mControlChildCB.setVisibility(mDevice.getIsMeshDevice() ? View.VISIBLE : View.GONE);
        if (mDevice.getDeviceType() == EspDeviceType.ROOT)
        {
            mControlChildCB.setChecked(true);
            mControlChildCB.setVisibility(View.GONE);
        }
        
        mDialog =
            new AlertDialog.Builder(mContext).setTitle(mDevice.getName())
                .setView(view)
                .setNegativeButton(R.string.esp_sss_device_dialog_exit, null)
                .setCancelable(false)
                .show();
        mDialog.setOnDismissListener(this);
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
    public void onClick(View v)
    {
        if (v == mConfirmBtn)
        {
            String addStr = mAddressEdt.getText().toString();
            String cmdStr = mCommandEdt.getText().toString();
            String repStr = mRepeatEdt.getText().toString();
            if (TextUtils.isEmpty(addStr) || TextUtils.isEmpty(cmdStr) || TextUtils.isEmpty(repStr))
            {
                return;
            }
            
            IEspStatusRemote status = new EspStatusRemote();
            status.setAddress(Integer.parseInt(addStr));
            status.setCommand(Integer.parseInt(cmdStr));
            status.setRepeat(Integer.parseInt(repStr));
            new StatusTask(mControlChildCB.isChecked()).execute(status);
        }
    }
    
    private class StatusTask extends AsyncTask<IEspStatusRemote, Void, Boolean>
    {
        private boolean mBroadcast;
        
        public StatusTask(boolean broadcast)
        {
            mBroadcast = broadcast;
        }
        
        @Override
        protected void onPreExecute()
        {
            mDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setEnabled(false);
            mProgressContainer.setVisibility(View.VISIBLE);
        }
        
        @Override
        protected Boolean doInBackground(IEspStatusRemote... params)
        {
            IEspStatusRemote status = params[0];
            return mUser.doActionPostDeviceStatus(mDevice, status, mBroadcast);
        }
        
        @Override
        protected void onPostExecute(Boolean result)
        {
            if (mDevice.getDeviceType() == EspDeviceType.REMOTE)
            {
                if (result)
                {
                    Toast.makeText(mContext, R.string.esp_device_remote_post_success, Toast.LENGTH_LONG).show();
                }
                else
                {
                    Toast.makeText(mContext, R.string.esp_device_remote_post_failed, Toast.LENGTH_LONG).show();
                }
            }
            
            mProgressContainer.setVisibility(View.GONE);
            mDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setEnabled(true);
        }
    }

    @Override
    protected void onExecuteEnd(boolean suc)
    {
    }

    @Override
    protected View getContentView(LayoutInflater inflater)
    {
        return null;
    }
}
