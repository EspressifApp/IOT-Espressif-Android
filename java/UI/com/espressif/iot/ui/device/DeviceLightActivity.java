package com.espressif.iot.ui.device;

import com.espressif.iot.R;
import com.espressif.iot.device.IEspDeviceLight;
import com.espressif.iot.help.ui.IEspHelpUIUseLight;
import com.espressif.iot.type.device.status.EspStatusLight;
import com.espressif.iot.type.device.status.IEspStatusLight;
import com.espressif.iot.type.help.HelpStepUseLight;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class DeviceLightActivity extends DeviceActivityAbs implements OnClickListener, OnSeekBarChangeListener,
    IEspHelpUIUseLight
{
    // range of freq is 100~500
    private static final int FREQ_MIN = IEspDeviceLight.FREQ_MIN;
    private static final int FREQ_MAX = IEspDeviceLight.FREQ_MAX;
    private static final int RGB_MAX = IEspDeviceLight.RGB_MAX;
    
    private SeekBar mLightFreqBar;
    private SeekBar mLightRedBar;
    private SeekBar mLightGreenBar;
    private SeekBar mLightBlueBar;
    
    private View mColorDisplay;
    
    private Button mConfirmBtn;
    private CheckBox mControlChildCB;
    private CheckBox mSwitch;
    
    private IEspDeviceLight mDeviceLight;
    
    private View mLightLayout;
    
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        mDeviceLight = (IEspDeviceLight)mIEspDevice;
        
        boolean compatibility = isDeviceCompatibility();
        if (mHelpMachine.isHelpModeUseLight())
        {
            mHelpMachine.transformState(compatibility);
            onHelpUseLight();
        }
        if (compatibility)
        {
            executeGet();
        }
    }
    
    @Override
    protected View initControlView()
    {
        View view = getLayoutInflater().inflate(R.layout.device_activity_light, null);
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
        mControlChildCB.setVisibility(mIEspDevice.getIsMeshDevice() ? View.VISIBLE : View.GONE);
        mSwitch = (CheckBox)view.findViewById(R.id.light_switch);
        mSwitch.setOnClickListener(this);
        
        mLightLayout = view.findViewById(R.id.light_layout);
        
        return view;
    }
    
    @Override
    public void onClick(View view)
    {
        if (view == mConfirmBtn)
        {
            IEspStatusLight status = new EspStatusLight();
            status.setFreq(getProgressLightFreq());
            status.setRed(mLightRedBar.getProgress());
            status.setGreen(mLightGreenBar.getProgress());
            status.setBlue(mLightBlueBar.getProgress());
            
            executeStatus(status);
        }
        else if (view == mSwitch)
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
            
            executeStatus(status);
        }
    }
    
    private void executeStatus(IEspStatusLight status)
    {
        if (mIEspDevice.getIsMeshDevice() && mControlChildCB.isChecked())
        {
            executePost(status, true);
        }
        else
        {
            executePost(status);
        }
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
    public void onProgressChanged(SeekBar bar, int progress, boolean fromUser)
    {
        int color = Color.rgb(mLightRedBar.getProgress(), mLightGreenBar.getProgress(), mLightBlueBar.getProgress());
        mColorDisplay.setBackgroundColor(color);
    }
    
    @Override
    public void onStartTrackingTouch(SeekBar bar)
    {
    }
    
    @Override
    public void onStopTrackingTouch(SeekBar bar)
    {
    }
    
    @Override
    protected void executePrepare()
    {
    }
    
    @Override
    protected void executeFinish(int command, boolean result)
    {
        IEspStatusLight status = mDeviceLight.getStatusLight();
        int freq = status.getFreq();
        int red = status.getRed();
        int green = status.getGreen();
        int blue = status.getBlue();
        
        setProgressLightFreq(freq);
        mLightRedBar.setProgress(red);
        mLightGreenBar.setProgress(green);
        mLightBlueBar.setProgress(blue);
        
        if (mHelpMachine.isHelpModeUseLight() && command == COMMAND_POST)
        {
            mHelpMachine.transformState(result);
            onHelpUseLight();
        }
    }

    @Override
    public void onHelpUseLight()
    {
        clearHelpContainer();
        
        HelpStepUseLight step = HelpStepUseLight.valueOf(mHelpMachine.getCurrentStateOrdinal());
        switch (step)
        {
            case START_USE_HELP:
            case FAIL_FOUND_LIGHT:
            case FIND_ONLINE:
            case NO_LIGHT_ONLINE:
            case LIGHT_SELECT:
                break;
                
            case LIGHT_NOT_COMPATIBILITY:
                mHelpMachine.exit();
                setResult(RESULT_EXIT_HELP_MODE);
                break;
            case LIGHT_CONTROL:
                highlightHelpView(mLightLayout);
                setHelpHintMessage(R.string.esp_help_use_light_control_msg);
                break;
            case LIGHT_CONTROL_FAILED:
                highlightHelpView(mLightLayout);
                setHelpHintMessage(R.string.esp_help_use_light_control_failed_msg);
                mHelpMachine.retry();
                break;
            case SUC:
                setHelpFrameDark();
                setHelpHintMessage(R.string.esp_help_use_light_success_msg);
                setHelpButtonVisible(HELP_BUTTON_EXIT, true);
                break;
        }
    }

}
