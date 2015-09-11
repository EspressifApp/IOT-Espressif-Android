package com.espressif.iot.ui.device;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import com.espressif.iot.R;
import com.espressif.iot.device.IEspDeviceLight;
import com.espressif.iot.model.longsocket2.EspLongSocket;
import com.espressif.iot.model.longsocket2.IEspLongSocket;
import com.espressif.iot.type.device.IEspDeviceState;
import com.espressif.iot.type.device.status.EspStatusLight;
import com.espressif.iot.type.device.status.IEspStatusEspnow;
import com.espressif.iot.type.device.status.IEspStatusLight;
import com.espressif.iot.ui.view.EspColorPicker;
import com.espressif.iot.ui.view.EspColorPicker.OnColorChangedListener;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Toast;

public class DeviceLightActivity extends DeviceActivityAbs implements OnClickListener, OnSeekBarChangeListener,
     OnColorChangedListener, OnFocusChangeListener
{
    // range of period is 1000~10000
    private static final int PERIOD_MIN = IEspDeviceLight.PERIOD_MIN;
    private static final int PERIOD_MAX = IEspDeviceLight.PERIOD_MAX;
    
    private int mRGBMax = IEspDeviceLight.RGB_MAX;
    
    private SeekBar mLightPeriodBar;
    private SeekBar mLightCWhiteBar;
    private SeekBar mLightWWhiteBar;
    private SeekBar mLightRedBar;
    private SeekBar mLightGreenBar;
    private SeekBar mLightBlueBar;
    private View mSeekBarContainer;
    
    private TextView mCWhiteText;
    private TextView mWWhiteText;
    private TextView mRedText;
    private TextView mGreenText;
    private TextView mBlueText;
    private TextView mPeriodText;
    
    private ImageView mColorPickerSwap;
    private EspColorPicker mColorPicker;
    
    private View mColorDisplay;
    
    private Button mConfirmBtn;
    private CheckBox mSwitch;
    
    private IEspDeviceLight mDeviceLight;
    
    protected View mLightLayout;
    
    private IEspLongSocket mLongSocket;
    
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        mDeviceLight = (IEspDeviceLight)mIEspDevice;
        
        boolean compatibility = isDeviceCompatibility();
        checkHelpModeLight(compatibility);
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
        
        mLightPeriodBar = (SeekBar)view.findViewById(R.id.light_period_bar);
        mLightPeriodBar.setMax(PERIOD_MAX - PERIOD_MIN);
        mLightPeriodBar.setOnSeekBarChangeListener(this);
        
        mLightRedBar = (SeekBar)view.findViewById(R.id.light_red_bar);
        mLightRedBar.setMax(mRGBMax);
        mLightRedBar.setOnSeekBarChangeListener(this);
        
        mLightGreenBar = (SeekBar)view.findViewById(R.id.light_green_bar);
        mLightGreenBar.setMax(mRGBMax);
        mLightGreenBar.setOnSeekBarChangeListener(this);
        
        mLightBlueBar = (SeekBar)view.findViewById(R.id.light_blue_bar);
        mLightBlueBar.setMax(mRGBMax);
        mLightBlueBar.setOnSeekBarChangeListener(this);
        
        mLightCWhiteBar = (SeekBar)view.findViewById(R.id.light_cwhite_bar);
        mLightCWhiteBar.setMax(mRGBMax);
        mLightCWhiteBar.setOnSeekBarChangeListener(this);
        
        mLightWWhiteBar = (SeekBar)view.findViewById(R.id.light_wwhite_bar);
        mLightWWhiteBar.setMax(mRGBMax);
        mLightWWhiteBar.setOnSeekBarChangeListener(this);
        
        mCWhiteText = (TextView)view.findViewById(R.id.light_cwhite_text);
        mCWhiteText.setText("0");
        mCWhiteText.setOnFocusChangeListener(this);
        mWWhiteText = (TextView)view.findViewById(R.id.light_wwhite_text);
        mWWhiteText.setText("0");
        mWWhiteText.setOnFocusChangeListener(this);
        mRedText = (TextView)view.findViewById(R.id.light_red_text);
        mRedText.setText("0");
        mRedText.setOnFocusChangeListener(this);
        mGreenText = (TextView)view.findViewById(R.id.light_green_text);
        mGreenText.setText("0");
        mGreenText.setOnFocusChangeListener(this);
        mBlueText = (TextView)view.findViewById(R.id.light_blue_text);
        mBlueText.setText("0");
        mBlueText.setOnFocusChangeListener(this);
        mPeriodText = (TextView)view.findViewById(R.id.light_period_text);
        mPeriodText.setText(PERIOD_MIN + "");
        mPeriodText.setOnFocusChangeListener(this);
        
        if (!mIEspDevice.getDeviceState().isStateLocal() && mIEspDevice.getDeviceState().isStateInternet())
        {
            View wwContainer = view.findViewById(R.id.light_wwhite_container);
            wwContainer.setVisibility(View.GONE);
            TextView cwTitle = (TextView)view.findViewById(R.id.light_cwhite_title);
            cwTitle.setText(R.string.esp_device_light_white);
        }
        
        mConfirmBtn = (Button)view.findViewById(R.id.light_confirm_btn);
        mConfirmBtn.setOnClickListener(this);
        mSwitch = (CheckBox)view.findViewById(R.id.light_switch);
        mSwitch.setOnClickListener(this);
        
        mLightLayout = view.findViewById(R.id.light_layout);
        
        mColorPickerSwap = (ImageView)view.findViewById(R.id.light_colorpicker_swap);
        mColorPickerSwap.setOnClickListener(this);
        mSeekBarContainer = view.findViewById(R.id.light_seekbar_container);
        mColorPicker = (EspColorPicker)view.findViewById(R.id.light_color_picker);
        mColorPicker.setOnColorChangeListener(this);
        
        return view;
    }
    
    private void clearEditFocus()
    {
        if (mPeriodText.hasFocus())
        {
            mPeriodText.clearFocus();
        }
        if (mRedText.hasFocus())
        {
            mRedText.clearFocus();
        }
        if (mGreenText.hasFocus())
        {
            mGreenText.clearFocus();
        }
        if (mBlueText.hasFocus())
        {
            mBlueText.clearFocus();
        }
        if (mCWhiteText.hasFocus())
        {
            mCWhiteText.clearFocus();
        }
        if (mWWhiteText.hasFocus())
        {
            mWWhiteText.clearFocus();
        }
    }
    
    @Override
    public void onClick(View view)
    {
        clearEditFocus();
        
        if (view == mConfirmBtn)
        {
            IEspStatusLight status = new EspStatusLight();
            status.setPeriod(getProgressLightPeriod());
            status.setRed(mLightRedBar.getProgress());
            status.setGreen(mLightGreenBar.getProgress());
            status.setBlue(mLightBlueBar.getProgress());
            status.setCWhite(mLightCWhiteBar.getProgress());
            status.setWWhite(mLightWWhiteBar.getProgress());
            
            executeStatus(status);
        }
        else if (view == mSwitch)
        {
            int seekValue = mSwitch.isChecked() ? mRGBMax : 0;
            
            mLightRedBar.setProgress(seekValue);
            mLightGreenBar.setProgress(seekValue);
            mLightBlueBar.setProgress(seekValue);
            mLightWWhiteBar.setProgress(seekValue);
            mLightCWhiteBar.setProgress(seekValue);
            
            IEspStatusLight status = new EspStatusLight();
            status.setPeriod(getProgressLightPeriod());
            status.setRed(seekValue);
            status.setGreen(seekValue);
            status.setBlue(seekValue);
            status.setCWhite(seekValue);
            status.setWWhite(seekValue);
            
            executeStatus(status);
        }
        else if (view == mColorPickerSwap)
        {
            if (checkHelpClickSwap())
            {
                return;
            }
            
            if (mDeviceLight.getIsMeshDevice() || isDeviceArray())
            {
                Toast.makeText(this, R.string.esp_device_light_continuous_control_forbidden, Toast.LENGTH_SHORT).show();
            }
            else
            {
                swapColorSelectView();
            }
        }
    }
    
    @Override
    public void onColorChangeStart(View v, int color)
    {
        mLongSocket = EspLongSocket.createInstance();
        mLongSocket.start();
        
        mColorDisplay.setBackgroundColor(color);
        
        mLightRedBar.setProgress(parseRGBtoLightValue(Color.red(color)));
        mLightGreenBar.setProgress(parseRGBtoLightValue(Color.green(color)));
        mLightBlueBar.setProgress(parseRGBtoLightValue(Color.blue(color)));
    }

    @Override
    public void onColorChanged(View v, int color)
    {
        mColorDisplay.setBackgroundColor(color);
        
        mLightRedBar.setProgress(parseRGBtoLightValue(Color.red(color)));
        mLightGreenBar.setProgress(parseRGBtoLightValue(Color.green(color)));
        mLightBlueBar.setProgress(parseRGBtoLightValue(Color.blue(color)));
        
        IEspStatusLight statusLight = new EspStatusLight();
        statusLight.setPeriod(getProgressLightPeriod());
        statusLight.setRed(parseRGBtoLightValue(Color.red(color)));
        statusLight.setGreen(parseRGBtoLightValue(Color.green(color)));
        statusLight.setBlue(parseRGBtoLightValue(Color.blue(color)));
        // clear CW and WW
        statusLight.setCWhite(0);
        statusLight.setWWhite(0);
        
        boolean isMeshDevice = mDeviceLight.getIsMeshDevice();
        Runnable disconnectedCallback = new Runnable()
        {
            
            @Override
            public void run()
            {
                runOnUiThread(new Runnable()
                {
                    
                    @Override
                    public void run()
                    {
                        new AlertDialog.Builder(DeviceLightActivity.this).setTitle(R.string.esp_device_light_disconnect_msg)
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener()
                            {
                                @Override
                                public void onClick(DialogInterface dialog, int which)
                                {
                                    DeviceLightActivity.this.finish();
                                }
                            })
                            .show();
                    }
                });
            }
            
        };
        
        String deviceKey = mDeviceLight.getKey();
        InetAddress inetAddress = mDeviceLight.getInetAddress();
        String bssid = mDeviceLight.getBssid();
        IEspDeviceState state = mDeviceLight.getDeviceState();
        
        if (isMeshDevice)
        {
            mLongSocket.addMeshStatus(deviceKey, inetAddress, bssid, statusLight, state, disconnectedCallback);
        }
        else
        {
            mLongSocket.addStatus(deviceKey, inetAddress, statusLight, state, disconnectedCallback);
        }
    }
    
    @Override
    public void onColorChangeEnd(View v, int color)
    {
        mLongSocket.stop();
    }
    
    private void swapColorSelectView()
    {
        int colorPickerVisibility = mSeekBarContainer.getVisibility();
        mSeekBarContainer.setVisibility(colorPickerVisibility == View.VISIBLE ? View.GONE : View.VISIBLE);
        mColorPicker.setVisibility(colorPickerVisibility);
    }
    
    private void executeStatus(IEspStatusLight status)
    {
        if (isDeviceArray())
        {
            mDeviceLight.setStatusLight(status);
        }
        
        executePost(status);
    }
    
    @Override
    protected void executePrepare()
    {
    }
    
    @Override
    protected void executeFinish(int command, boolean result)
    {
        IEspStatusLight status = mDeviceLight.getStatusLight();
        int period = status.getPeriod();
        int red = status.getRed();
        int green = status.getGreen();
        int blue = status.getBlue();
        int cwhite = status.getCWhite();
        int wwhite = status.getWWhite();
        
        setProgressLightPeriod(period);
        mRGBMax = calRGBMax(getProgressLightPeriod());
        setRGBSeekbarMax(mRGBMax);
        
        mLightRedBar.setProgress(red);
        mLightGreenBar.setProgress(green);
        mLightBlueBar.setProgress(blue);
        mLightCWhiteBar.setProgress(cwhite);
        mLightWWhiteBar.setProgress(wwhite);
        
        // Set switch on or off
        if (red > 0 || green > 0|| blue > 0 || cwhite > 0 || wwhite > 0)
        {
            mSwitch.setChecked(true);
        }
        else
        {
            mSwitch.setChecked(false);
        }
        
        checkHelpExecuteFinish(command, result);
        
        if (command == COMMAND_GET)
        {
            checkLowBatteryEspnow();
        }
    }
    
    private void checkLowBatteryEspnow()
    {
        List<CharSequence> lowBatteryMacs = new ArrayList<CharSequence>();
        List<IEspStatusEspnow> espStatuses = mDeviceLight.getEspnowStatusList();
        for (IEspStatusEspnow espStatus : espStatuses)
        {
            if (espStatus.isLowBattery())
            {
                lowBatteryMacs.add(espStatus.getMac());
            }
        }
        
        if (!lowBatteryMacs.isEmpty())
        {
            CharSequence[] items = new CharSequence[lowBatteryMacs.size()];
            for (int i = 0; i < lowBatteryMacs.size(); i++)
            {
                items[i] = lowBatteryMacs.get(i);
            }
            new AlertDialog.Builder(this).setTitle(R.string.esp_device_light_espnow_low_battery)
                .setPositiveButton(android.R.string.ok, null)
                .setItems(items, null)
                .show();
        }
    }
    
    private void setRGBSeekbarMax(int max)
    {
        mLightRedBar.setMax(max);
        mLightGreenBar.setMax(max);
        mLightBlueBar.setMax(max);
        mLightCWhiteBar.setMax(max);
        mLightWWhiteBar.setMax(max);
    }
    
    private int calRGBMax(int period)
    {
        return period*1000/45;
    }
    
    private int getProgressLightPeriod()
    {
        int period = mLightPeriodBar.getProgress();
        period += PERIOD_MIN;
        return period;
    }
    
    private void setProgressLightPeriod(int period)
    {
        period -= PERIOD_MIN;
        period = period > 0 ? period : 0;
        mLightPeriodBar.setProgress(period);
    }
    
    private int parseLightValuetoRGB(int value)
    {
        final int rgbMax = 255;
        int lightMax = mRGBMax;
        if (lightMax > rgbMax)
        {
            return rgbMax * value / mRGBMax;
        }
        else
        {
            return value;
        }
    }
    
    private int parseRGBtoLightValue(int rgb)
    {
        final int rgbMax = 255;
        final int lightMax = mRGBMax;
        
        return (rgb * lightMax) / rgbMax;
    }
    
    @Override
    public void onProgressChanged(SeekBar bar, int progress, boolean fromUser)
    {
        int cwhite = mLightCWhiteBar.getProgress();
        mCWhiteText.setText("" + cwhite);
        int wwhite = mLightWWhiteBar.getProgress();
        mWWhiteText.setText("" + wwhite);
        int red = mLightRedBar.getProgress();
        mRedText.setText("" + red);
        int green = mLightGreenBar.getProgress();
        mGreenText.setText("" + green);
        int blue = mLightBlueBar.getProgress();
        mBlueText.setText("" + blue);
        int period = getProgressLightPeriod();
        mPeriodText.setText("" + period);
        
        if (mSeekBarContainer.getVisibility() == View.VISIBLE)
        {
            int color = Color.rgb(parseLightValuetoRGB(red), parseLightValuetoRGB(green), parseLightValuetoRGB(blue));
            mColorDisplay.setBackgroundColor(color);
        }
    }
    
    @Override
    public void onStartTrackingTouch(SeekBar bar)
    {
    }
    
    @Override
    public void onStopTrackingTouch(SeekBar bar)
    {
        if (bar == mLightPeriodBar)
        {
            mRGBMax = calRGBMax(getProgressLightPeriod());
            setRGBSeekbarMax(mRGBMax);
        }
    }
    
    @Override
    public void onFocusChange(View v, boolean hasFocus)
    {
        if (v == mPeriodText)
        {
            if (!hasFocus)
            {
                onRGBFEdited(mPeriodText, mLightPeriodBar);
                
                mRGBMax = calRGBMax(getProgressLightPeriod());
                setRGBSeekbarMax(mRGBMax);
            }
        }
        else if (v == mRedText)
        {
            if (!hasFocus)
            {
                onRGBFEdited(mRedText, mLightRedBar);
            }
        }
        else if (v == mGreenText)
        {
            if (!hasFocus)
            {
                onRGBFEdited(mGreenText, mLightGreenBar);
            }
        }
        else if (v == mBlueText)
        {
            if (!hasFocus)
            {
                onRGBFEdited(mBlueText, mLightBlueBar);
            }
        }
        else if (v == mCWhiteText)
        {
            if (!hasFocus)
            {
                onRGBFEdited(mCWhiteText, mLightCWhiteBar);
            }
        }
        else if (v == mWWhiteText)
        {
            if (!hasFocus)
            {
                onRGBFEdited(mWWhiteText, mLightWWhiteBar);
            }
        }
    }
    
    private void onRGBFEdited(TextView textview, SeekBar seekbar)
    {
        CharSequence text = textview.getText();
        if (TextUtils.isEmpty(text))
        {
            return;
        }
        
        int value = Integer.parseInt(text.toString());
        if (seekbar == mLightPeriodBar)
        {
            value -= PERIOD_MIN;
        }
        if (value > seekbar.getMax())
        {
            value = seekbar.getMax();
        }
        seekbar.setProgress(value);
    }
    
    protected void checkHelpModeLight(boolean compatibility)
    {
    }
    
    protected void checkHelpExecuteFinish(int command, boolean result)
    {
    }
    
    protected boolean checkHelpClickSwap()
    {
        return false;
    }
}
