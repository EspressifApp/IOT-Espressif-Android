package com.espressif.iot.ui.device.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;

import com.espressif.iot.R;
import com.espressif.iot.device.IEspDevice;
import com.espressif.iot.device.IEspDevicePlug;
import com.espressif.iot.device.IEspDeviceSSS;
import com.espressif.iot.model.help.statemachine.EspHelpStateMachine;
import com.espressif.iot.type.device.EspDeviceType;
import com.espressif.iot.type.device.status.EspStatusPlug;
import com.espressif.iot.type.device.status.IEspStatusPlug;
import com.espressif.iot.ui.softap_sta_support.SoftApStaSupportActivity;
import com.espressif.iot.user.IEspUser;
import com.espressif.iot.user.builder.BEspUser;

public class DevicePlugDialog implements EspDeviceDialogInterface, View.OnClickListener, OnDismissListener
{
    private IEspUser mUser;
    
    private Context mContext;
    
    private IEspDevice mDevice;
    
    private View mProgressContainer;
    
    private CheckBox mPlugCB;
    private CheckBox mControlChildCB;
    
    private AlertDialog mDialog;
    
    public DevicePlugDialog(Context context, IEspDevice device)
    {
        mUser = BEspUser.getBuilder().getInstance();
        mContext = context;
        mDevice = device;
    }
    
    @Override
    public void show()
    {
        LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.device_dialog_plug, null);
        
        mProgressContainer = view.findViewById(R.id.progress_container);
        mProgressContainer.setOnClickListener(this);
        
        mPlugCB = (CheckBox)view.findViewById(R.id.plug_switch);
        mPlugCB.setOnClickListener(this);
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
                .setCancelable(false)
                .setNegativeButton(R.string.esp_sss_device_dialog_exit, null)
                .show();
        mDialog.setOnDismissListener(this);
        
        new StatusTask().execute();
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
        if (v == mPlugCB)
        {
            IEspStatusPlug status = new EspStatusPlug();
            status.setIsOn(mPlugCB.isChecked());
            new StatusTask(mControlChildCB.isChecked()).execute(status);
        }
    }
    
    private class StatusTask extends AsyncTask<IEspStatusPlug, Void, Boolean>
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
            mDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setEnabled(false);
            mProgressContainer.setVisibility(View.VISIBLE);
        }
        
        @Override
        protected Boolean doInBackground(IEspStatusPlug... params)
        {
            if (params.length > 0)
            {
                IEspStatusPlug status = params[0];
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
            if (mDevice.getDeviceType() == EspDeviceType.PLUG)
            {
                IEspStatusPlug status;
                if (mDevice instanceof IEspDeviceSSS)
                {
                    status = (IEspStatusPlug)((IEspDeviceSSS)mDevice).getDeviceStatus();
                }
                else
                {
                    status = ((IEspDevicePlug)mDevice).getStatusPlug();
                }
                mPlugCB.setChecked(status.isOn());
            }
            
            mProgressContainer.setVisibility(View.GONE);
            mDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setEnabled(true);
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog)
    {
        EspHelpStateMachine helpMachine = EspHelpStateMachine.getInstance();
        if (helpMachine.isHelpModeUseSSSDevice())
        {
            helpMachine.transformState(true);
            ((SoftApStaSupportActivity)mContext).onHelpUseSSSDevice();
        }
    }
}
