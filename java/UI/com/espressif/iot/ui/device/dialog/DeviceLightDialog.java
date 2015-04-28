package com.espressif.iot.ui.device.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.graphics.Color;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

import com.espressif.iot.R;
import com.espressif.iot.device.IEspDevice;
import com.espressif.iot.device.IEspDeviceLight;
import com.espressif.iot.device.IEspDeviceSSS;
import com.espressif.iot.model.help.statemachine.EspHelpStateMachine;
import com.espressif.iot.type.device.EspDeviceType;
import com.espressif.iot.type.device.status.EspStatusLight;
import com.espressif.iot.type.device.status.IEspStatusLight;
import com.espressif.iot.ui.softap_sta_support.SoftApStaSupportActivity;
import com.espressif.iot.user.IEspUser;
import com.espressif.iot.user.builder.BEspUser;

public class DeviceLightDialog implements EspDeviceDialogInterface, View.OnClickListener, OnSeekBarChangeListener,
    OnDismissListener
{
    // range of freq is 100~500
    private static final int FREQ_MIN = IEspDeviceLight.FREQ_MIN;
    private static final int FREQ_MAX = IEspDeviceLight.FREQ_MAX;
    private static final int RGB_MAX = IEspDeviceLight.RGB_MAX;
    
    private IEspUser mUser;
    
    private Context mContext;
    
    private IEspDevice mDevice;
    
    private AlertDialog mDialog;
    
    private View mProgressContainer;
    
    private SeekBar mLightFreqBar;
    private SeekBar mLightRedBar;
    private SeekBar mLightGreenBar;
    private SeekBar mLightBlueBar;
    
    private View mColorDisplay;
    
    private Button mConfirmBtn;
    private CheckBox mControlChildCB;
    private CheckBox mSwitch;
    
    public DeviceLightDialog(Context context, IEspDevice device)
    {
        mUser = BEspUser.getBuilder().getInstance();
        mContext = context;
        mDevice = device;
    }
    
    @Override
    public void show()
    {
        LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.device_dialog_light, null);
        
        mProgressContainer = view.findViewById(R.id.progress_container);
        mProgressContainer.setVisibility(View.GONE);
        mProgressContainer.setOnClickListener(this);
        
        mColorDisplay = view.findViewById(R.id.light_color_display);
        
        mLightFreqBar = (SeekBar)view.findViewById(R.id.light_freq_bar);
        mLightFreqBar.setMax(FREQ_MAX - FREQ_MIN);
        mLightFreqBar.setOnSeekBarChangeListener(this);
        
        mLightRedBar = (SeekBar)view.findViewById(R.id.light_red_bar);
        mLightRedBar.setMax(RGB_MAX);
        mLightRedBar.setOnSeekBarChangeListener(this);
        
        mLightGreenBar = (SeekBar)view.findViewById(R.id.light_green_bar);
        mLightGreenBar.setMax(RGB_MAX);
        mLightGreenBar.setOnSeekBarChangeListener(this);
        
        mLightBlueBar = (SeekBar)view.findViewById(R.id.light_blue_bar);
        mLightBlueBar.setMax(RGB_MAX);
        mLightBlueBar.setOnSeekBarChangeListener(this);
        
        mConfirmBtn = (Button)view.findViewById(R.id.light_confirm_btn);
        mConfirmBtn.setOnClickListener(this);
        mControlChildCB = (CheckBox)view.findViewById(R.id.control_child_cb);
        mControlChildCB.setVisibility(mDevice.getIsMeshDevice() ? View.VISIBLE : View.GONE);
        if (mDevice.getDeviceType() == EspDeviceType.ROOT)
        {
            mControlChildCB.setChecked(true);
            mControlChildCB.setVisibility(View.GONE);
        }
        mSwitch = (CheckBox)view.findViewById(R.id.light_switch);
        mSwitch.setOnClickListener(this);
        
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
        if (v == mConfirmBtn)
        {
            IEspStatusLight status = new EspStatusLight();
            status.setFreq(getProgressLightFreq());
            status.setRed(mLightRedBar.getProgress());
            status.setGreen(mLightGreenBar.getProgress());
            status.setBlue(mLightBlueBar.getProgress());
            
            new StatusTask(mControlChildCB.isChecked()).execute(status);
        }
        else if (v == mSwitch)
        {
            int seekValue = mSwitch.isChecked() ? RGB_MAX : 0;
            
            mLightRedBar.setProgress(seekValue);
            mLightGreenBar.setProgress(seekValue);
            mLightBlueBar.setProgress(seekValue);
            
            IEspStatusLight status = new EspStatusLight();
            status.setFreq(getProgressLightFreq());
            status.setRed(seekValue);
            status.setGreen(seekValue);
            status.setBlue(seekValue);
            
            new StatusTask(mControlChildCB.isChecked()).execute(status);
        }
    }
    
    private class StatusTask extends AsyncTask<IEspStatusLight, Void, Boolean>
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
        protected Boolean doInBackground(IEspStatusLight... params)
        {
            if (params.length > 0)
            {
                IEspStatusLight status = params[0];
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
            if (mDevice.getDeviceType() == EspDeviceType.LIGHT) {
                IEspStatusLight status;
                if (mDevice instanceof IEspDeviceSSS)
                {
                    status = (IEspStatusLight)((IEspDeviceSSS)mDevice).getDeviceStatus();
                }
                else
                {
                    status = ((IEspDeviceLight)mDevice).getStatusLight();
                }
                int freq = status.getFreq();
                int red = status.getRed();
                int green = status.getGreen();
                int blue = status.getBlue();
                
                setProgressLightFreq(freq);
                mLightRedBar.setProgress(red);
                mLightGreenBar.setProgress(green);
                mLightBlueBar.setProgress(blue);
            }
            
            mProgressContainer.setVisibility(View.GONE);
            mDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setEnabled(true);
        }
    }
    
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
    {
        int color = Color.rgb(mLightRedBar.getProgress(), mLightGreenBar.getProgress(), mLightBlueBar.getProgress());
        mColorDisplay.setBackgroundColor(color);
    }
    
    @Override
    public void onStartTrackingTouch(SeekBar seekBar)
    {
    }
    
    @Override
    public void onStopTrackingTouch(SeekBar seekBar)
    {
    }
    
    private int getProgressLightFreq()
    {
        int freq = mLightFreqBar.getProgress();
        freq += FREQ_MIN;
        return freq;
    }
    
    private void setProgressLightFreq(int freq)
    {
        freq -= FREQ_MIN;
        freq = freq > 0 ? freq : 0;
        mLightFreqBar.setProgress(freq);
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
