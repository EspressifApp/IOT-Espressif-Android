package com.espressif.iot.ui.device;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.espressif.iot.R;
import com.espressif.iot.device.IEspDeviceLight;
import com.espressif.iot.model.longsocket2.EspLongSocket;
import com.espressif.iot.model.longsocket2.IEspLongSocket;
import com.espressif.iot.type.device.IEspDeviceState;
import com.espressif.iot.type.device.other.EspLightRecord;
import com.espressif.iot.type.device.status.EspStatusLight;
import com.espressif.iot.type.device.status.IEspStatusEspnow;
import com.espressif.iot.type.device.status.IEspStatusLight;
import com.espressif.iot.ui.view.EspColorPicker;
import com.espressif.iot.ui.view.EspColorPicker.OnColorChangedListener;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
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

    private static Logger log = Logger.getLogger(com.espressif.iot.ui.device.DeviceLightActivity.class);
    
    // range of period is 1000~10000
    private static final int PERIOD_MIN = IEspDeviceLight.PERIOD_MIN;
    private static final int PERIOD_MAX = IEspDeviceLight.PERIOD_MAX;
    // range of norm is 0~1
    private static final double NORM_MIN = 0;
    private static final double NORM_MAX = 1;
    
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
    
    private EspLightRecord mLightStatus;
    
    protected View mLightLayout;
    
    private IEspLongSocket mLongSocket;
    
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        mDeviceLight = (IEspDeviceLight)mIEspDevice;
        mLightStatus = EspLightRecord.createFakeInstance();
        
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
        View view = View.inflate(this, R.layout.device_activity_light, null);
        mColorDisplay = view.findViewById(R.id.light_color_display);
        mColorDisplay.setBackgroundColor(Color.BLACK);
        
        mLightPeriodBar = (SeekBar)view.findViewById(R.id.light_period_bar);
        mLightPeriodBar.setMax(PERIOD_MAX - PERIOD_MIN);
        mLightPeriodBar.setOnSeekBarChangeListener(this);
        
        int seekbarMax = EspLightRecord.getRgbwMax(PERIOD_MIN);
        mLightRedBar = (SeekBar)view.findViewById(R.id.light_red_bar);
        mLightRedBar.setMax(seekbarMax);
        mLightRedBar.setOnSeekBarChangeListener(this);
        
        mLightGreenBar = (SeekBar)view.findViewById(R.id.light_green_bar);
        mLightGreenBar.setMax(seekbarMax);
        mLightGreenBar.setOnSeekBarChangeListener(this);
        
        mLightBlueBar = (SeekBar)view.findViewById(R.id.light_blue_bar);
        mLightBlueBar.setMax(seekbarMax);
        mLightBlueBar.setOnSeekBarChangeListener(this);
        
        mLightCWhiteBar = (SeekBar)view.findViewById(R.id.light_cwhite_bar);
        mLightCWhiteBar.setMax(seekbarMax);
        mLightCWhiteBar.setOnSeekBarChangeListener(this);
        
        mLightWWhiteBar = (SeekBar)view.findViewById(R.id.light_wwhite_bar);
        mLightWWhiteBar.setMax(seekbarMax);
        mLightWWhiteBar.setOnSeekBarChangeListener(this);
        
        mCWhiteText = (TextView)view.findViewById(R.id.light_cwhite_text);
        mCWhiteText.setText("0.00");
        mCWhiteText.setOnFocusChangeListener(this);
        mWWhiteText = (TextView)view.findViewById(R.id.light_wwhite_text);
        mWWhiteText.setText("0.00");
        mWWhiteText.setOnFocusChangeListener(this);
        mRedText = (TextView)view.findViewById(R.id.light_red_text);
        mRedText.setText("0.00");
        mRedText.setOnFocusChangeListener(this);
        mGreenText = (TextView)view.findViewById(R.id.light_green_text);
        mGreenText.setText("0.00");
        mGreenText.setOnFocusChangeListener(this);
        mBlueText = (TextView)view.findViewById(R.id.light_blue_text);
        mBlueText.setText("0.00");
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
    
    private static int restrict(int value, int min, int max)
    {
        return (value < min ? min : (value < max ? value : max));
    }
    
    private void setLightPeriod(int period)
    {
        period = restrict(period, PERIOD_MIN, PERIOD_MAX) - PERIOD_MIN;
        mLightPeriodBar.setProgress(period);
    }
    
    private int getLightPeriod()
    {
        int period = mLightPeriodBar.getProgress() + PERIOD_MIN;
        return period;
    }
    
    private void setLightPeriodStr(String periodStr)
    {
        int period = Integer.parseInt(periodStr);
        period = restrict(period, PERIOD_MIN, PERIOD_MAX);
        mPeriodText.setText("" + period);
    }
    
    private String getLightPeriodStr()
    {
        String periodStr = mPeriodText.getText().toString();
        int period = Integer.parseInt(periodStr);
        period = restrict(period, PERIOD_MIN, PERIOD_MAX);
        periodStr = Integer.toString(period);
        return periodStr;
    }
    
    private void setLightSeekbarRedMax(int max)
    {
        mLightRedBar.setMax(max);
    }
    
    private void setLightSeekbarRed(int red)
    {
        mLightRedBar.setProgress(red);
    }
    
    private int getLightSeekbarRed()
    {
        return mLightRedBar.getProgress();
    }
    
    private String getLightNormRedStr()
    {
        return mRedText.getText().toString();
    }
    
    private void setLightNormRedStr(String red_norm_str)
    {
        mRedText.setText(red_norm_str);
    }
    
    private void setLightSeekbarGreenMax(int max)
    {
        mLightGreenBar.setMax(max);
    }
    
    private void setLightSeekbarGreen(int green)
    {
        mLightGreenBar.setProgress(green);
    }
    
    private int getLightSeekbarGreen()
    {
        return mLightGreenBar.getProgress();
    }
    
    private String getLightNormGreenStr()
    {
        return mGreenText.getText().toString();
    }
    
    private void setLightNormGreenStr(String green_norm_str)
    {
        mGreenText.setText(green_norm_str);
    }
    
    private void setLightSeekbarBlueMax(int max)
    {
        mLightBlueBar.setMax(max);
    }
    
    private void setLightSeekbarBlue(int blue)
    {
        mLightBlueBar.setProgress(blue);
    }
    
    private int getLightSeekbarBlue()
    {
        return mLightBlueBar.getProgress();
    }
    
    private String getLightNormBlueStr()
    {
        return mBlueText.getText().toString();
    }
    
    private void setLightNormBlueStr(String blue_norm_str)
    {
        mBlueText.setText(blue_norm_str);
    }
    
    private void setLightSeekbarCwMax(int max)
    {
        mLightCWhiteBar.setMax(max);
    }
    
    private void setLightSeekbarCw(int cw)
    {
        mLightCWhiteBar.setProgress(cw);
    }
    
    private int getLightSeekbarCw()
    {
        return mLightCWhiteBar.getProgress();
    }
    
    private String getLightNormCwStr()
    {
        return mCWhiteText.getText().toString();
    }
    
    private void setLightNormCwStr(String cw_norm_str)
    {
        mCWhiteText.setText(cw_norm_str);
    }
    
    private void setLightSeekbarWwMax(int max)
    {
        mLightWWhiteBar.setMax(max);
    }
    
    private void setLightSeekbarWw(int ww)
    {
        mLightWWhiteBar.setProgress(ww);
    }
    
    private int getLightSeekbarWw()
    {
        return mLightWWhiteBar.getProgress();
    }
    
    private String getLightNormWwStr()
    {
        return mWWhiteText.getText().toString();
    }
    
    private void setLightNormWwStr(String ww_norm_str)
    {
        mWWhiteText.setText(ww_norm_str);
    }
    
    private void __updateLightPeriodUI(int period_prev, int period, int period_min, int period_max)
    {
        period = restrict(period, period_min, period_max);
        log.debug("__updateLightPeriodUI() period_prev:" + period_prev + ",period:" + period);
        // update UI when period change
        if (period != period_prev)
        {
            log.debug("__updateLightPeriodUI() period != period_prev");
            // update period
            setLightPeriod(period);
            setLightPeriodStr("" + period);
            // update rgbw value
            final int rgbw_min_prev = 0;
            final double norm_rgbw_min = NORM_MIN;
            final double norm_rgbw_max = NORM_MAX;
            int rgbw_max_prev = EspLightRecord.getRgbwMax(period_prev);
            int rgbw_max = EspLightRecord.getRgbwMax(period);
            // update red
            int red_seekbar_prev = mLightStatus.getSeekbarRed();
            String red_norm_str = EspLightRecord.getNormStrBySeekbar(red_seekbar_prev, rgbw_min_prev, rgbw_max_prev);
            double red_norm = Double.parseDouble(red_norm_str);
            int red_seekbar = EspLightRecord.getSeekbarByNorm(red_norm, norm_rgbw_min, norm_rgbw_max, rgbw_max);
            setLightSeekbarRedMax(rgbw_max);
            setLightSeekbarRed(red_seekbar);
            // update green
            int green_seekbar_prev = mLightStatus.getSeekbarGreen();
            String green_norm_str = EspLightRecord.getNormStrBySeekbar(green_seekbar_prev, rgbw_min_prev, rgbw_max_prev);
            double green_norm = Double.parseDouble(green_norm_str);
            int green_seekbar = EspLightRecord.getSeekbarByNorm(green_norm, norm_rgbw_min, norm_rgbw_max, rgbw_max);
            setLightSeekbarGreenMax(rgbw_max);
            setLightSeekbarGreen(green_seekbar);
            // update blue
            int blue_seekbar_prev = mLightStatus.getSeekbarBlue();
            String blue_norm_str = EspLightRecord.getNormStrBySeekbar(blue_seekbar_prev, rgbw_min_prev, rgbw_max_prev);
            double blue_norm = Double.parseDouble(blue_norm_str);
            int blue_seekbar = EspLightRecord.getSeekbarByNorm(blue_norm, norm_rgbw_min, norm_rgbw_max, rgbw_max);
            setLightSeekbarBlueMax(rgbw_max);
            setLightSeekbarBlue(blue_seekbar);
            // update cw
            int cw_seekbar_prev = mLightStatus.getSeekbarCw();
            String cw_norm_str = EspLightRecord.getNormStrBySeekbar(cw_seekbar_prev, rgbw_min_prev, rgbw_max_prev);
            double cw_norm = Double.parseDouble(cw_norm_str);
            int cw_seekbar = EspLightRecord.getSeekbarByNorm(cw_norm, norm_rgbw_min, norm_rgbw_max, rgbw_max);
            setLightSeekbarCwMax(rgbw_max);
            setLightSeekbarCw(cw_seekbar);
            // update ww
            int ww_seekbar_prev = mLightStatus.getSeekbarWw();
            String ww_norm_str = EspLightRecord.getNormStrBySeekbar(ww_seekbar_prev, rgbw_min_prev, rgbw_max_prev);
            double ww_norm = Double.parseDouble(ww_norm_str);
            int ww_seekbar = EspLightRecord.getSeekbarByNorm(ww_norm, norm_rgbw_min, norm_rgbw_max, rgbw_max);
            setLightSeekbarWwMax(rgbw_max);
            setLightSeekbarWw(ww_seekbar);
        }
    }
    
    private void updateLightPeriodUIByText()
    {
        int period_prev = mLightStatus.getPeriod();
        String period_str = getLightPeriodStr();
        int period = Integer.parseInt(period_str);
        int period_min = PERIOD_MIN;
        int period_max = PERIOD_MAX;
        log.debug("updateLightPeriodUIByText()" + ",period_prev:" + period_prev + ",period:" + period);
        __updateLightPeriodUI(period_prev, period, period_min, period_max);
    }
    
    private void updateLightPeridTextBySeekbar()
    {
        int period = getLightPeriod();
        setLightPeriodStr("" + period);
    }
    
    private void updateLightPeriodUIBySeekbar()
    {
        int period_prev = mLightStatus.getPeriod();
        int period = getLightPeriod();
        int period_min = PERIOD_MIN;
        int period_max = PERIOD_MAX;
        log.debug("updateLightPeriodUIBySeekbar() period:" + period + ",period_prev:" + period_prev);
        __updateLightPeriodUI(period_prev, period, period_min, period_max);
    }
    
    private void __updateLightUIBySeekbar(boolean[] types, int seekbar)
    {
        int period = getLightPeriod();
        final int seekbar_rgbw_min = 0;
        int seekbar_rgbw_max = EspLightRecord.getRgbwMax(period);
        String norm_str = EspLightRecord.getNormStrBySeekbar(seekbar, seekbar_rgbw_min, seekbar_rgbw_max);
        String log_info = null;
        if (log.isDebugEnabled())
        {
            log_info =
                "period:" + period + ",seekbar:" + seekbar + ",seekbar_rgbw_max:" + seekbar_rgbw_max + ",norm_str:"
                    + norm_str;
        }
        // red
        if (types[0])
        {
            log.debug("__updateLightUIBySeekbar() red, " + log_info);
            setLightSeekbarRed(seekbar);
            setLightNormRedStr(norm_str);
        }
        // green
        else if (types[1])
        {
            log.debug("__updateLightUIBySeekbar() green, " + log_info);
            setLightSeekbarGreen(seekbar);
            setLightNormGreenStr(norm_str);
        }
        // blue
        else if (types[2])
        {
            log.debug("__updateLightUIBySeekbar() blue, " + log_info);
            setLightSeekbarBlue(seekbar);
            setLightNormBlueStr(norm_str);
        }
        // cw
        else if (types[3])
        {
            log.debug("__updateLightUIBySeekbar() cw, " + log_info);
            setLightSeekbarCw(seekbar);
            setLightNormCwStr(norm_str);
        }
        // ww
        else if (types[4])
        {
            log.debug("__updateLightUIBySeekbar() ww, " + log_info);
            setLightSeekbarWw(seekbar);
            setLightNormWwStr(norm_str);
        }
    }
    
    private void updateLightRedUIBySeekbar()
    {
        int seekbar_red = getLightSeekbarRed();
        boolean[] types = new boolean[] {true, false, false, false, false};
        __updateLightUIBySeekbar(types, seekbar_red);
    }
    
    private void updateLightGreenUIBySeekbar()
    {
        int seekbar_green = getLightSeekbarGreen();
        boolean[] types = new boolean[] {false, true, false, false, false};
        __updateLightUIBySeekbar(types, seekbar_green);
    }
    
    private void updateLightBlueUIBySeekbar()
    {
        int seekbar_blue = getLightSeekbarBlue();
        boolean[] types = new boolean[] {false, false, true, false, false};
        __updateLightUIBySeekbar(types, seekbar_blue);
    }
    
    private void updateLightCwUIBySeekbar()
    {
        int seekbar_cw = getLightSeekbarCw();
        boolean[] types = new boolean[] {false, false, false, true, false};
        __updateLightUIBySeekbar(types, seekbar_cw);
    }
    
    private void updateLightWwUIBySeekbar()
    {
        int seekbar_ww = getLightSeekbarWw();
        boolean[] types = new boolean[] {false, false, false, false, true};
        __updateLightUIBySeekbar(types, seekbar_ww);
    }
    
    private void __updateLightUIByNorm(boolean types[], String norm_str)
    {
        int period = getLightPeriod();
        int seekbar_rgbw_max = EspLightRecord.getRgbwMax(period);
        final double norm_rgbw_min = NORM_MIN;
        final double norm_rgbw_max = NORM_MAX;
        double norm = Double.parseDouble(norm_str);
        norm_str = EspLightRecord.getNormStrByNorm(norm, norm_rgbw_min, norm_rgbw_max);
        int seekbar = EspLightRecord.getSeekbarByNorm(norm, norm_rgbw_min, norm_rgbw_max, seekbar_rgbw_max);
        
        String log_info = null;
        if (log.isDebugEnabled())
        {
            log_info = "norm:" + norm + ",norm_str:" + norm_str + "seekbar:" + seekbar;
        }
        // red
        if (types[0])
        {
            log.debug("__updateLightUIByNorm() red, " + log_info);
            setLightNormRedStr(norm_str);
            setLightSeekbarRed(seekbar);
        }
        // green
        else if (types[1])
        {
            log.debug("__updateLightUIByNorm() green, " + log_info);
            setLightNormGreenStr(norm_str);
            setLightSeekbarGreen(seekbar);
        }
        // blue
        else if (types[2])
        {
            log.debug("__updateLightUIByNorm() blue, " + log_info);
            setLightNormBlueStr(norm_str);
            setLightSeekbarBlue(seekbar);
        }
        // cw
        else if (types[3])
        {
            log.debug("__updateLightUIByNorm() cw, " + log_info);
            setLightNormCwStr(norm_str);
            setLightSeekbarCw(seekbar);
        }
        // ww
        else if (types[4])
        {
            log.debug("__updateLightUIByNorm() ww, " + log_info);
            setLightNormWwStr(norm_str);
            setLightSeekbarWw(seekbar);
        }
    }
    
    private void updateLightRedUIByNorm()
    {
        String norm_red_str = getLightNormRedStr();
        boolean[] types = new boolean[] {true, false, false, false, false};
        __updateLightUIByNorm(types, norm_red_str);
    }
    
    private void updateLightGreenUIByNorm()
    {
        String norm_green_str = getLightNormGreenStr();
        boolean[] types = new boolean[] {false, true, false, false, false};
        __updateLightUIByNorm(types, norm_green_str);
    }
    
    private void updateLightBlueUIByNorm()
    {
        String norm_blue_str = getLightNormBlueStr();
        boolean[] types = new boolean[] {false, false, true, false, false};
        __updateLightUIByNorm(types, norm_blue_str);
    }
    
    private void updateLightCwUIByNorm()
    {
        String norm_cw_str = getLightNormCwStr();
        boolean[] types = new boolean[] {false, false, false, true, false};
        __updateLightUIByNorm(types, norm_cw_str);
    }
    
    private void updateLightWwUIByNorm()
    {
        String norm_ww_str = getLightNormWwStr();
        boolean[] types = new boolean[] {false, false, false, false, true};
        __updateLightUIByNorm(types, norm_ww_str);
    }
    
    private void updateLightStatusByColor(int color)
    {
        // get color value
        int period = getLightPeriod();
        int color_red = Color.red(color);
        int color_green = Color.green(color);
        int color_blue = Color.blue(color);
        // update light status
        log.info("updateLightStatusByColor() mLightStatus.updateByColor");
        mLightStatus.updateByColor(period, color_red, color_green, color_blue);
        // get seekbar value
        int seekbar_red = mLightStatus.getSeekbarRed();
        int seekbar_green = mLightStatus.getSeekbarGreen();
        int seekbar_blue = mLightStatus.getSeekbarBlue();
        int seekbar_cw = mLightStatus.getSeekbarCw();
        int seekbar_ww = mLightStatus.getSeekbarWw();
        // update UI by seekbar
        setLightSeekbarRed(seekbar_red);
        setLightSeekbarGreen(seekbar_green);
        setLightSeekbarBlue(seekbar_blue);
        setLightSeekbarCw(seekbar_cw);
        setLightSeekbarWw(seekbar_ww);
        updateLightRedUIBySeekbar();
        updateLightGreenUIBySeekbar();
        updateLightBlueUIBySeekbar();
        updateLightCwUIBySeekbar();
        updateLightWwUIBySeekbar();
    }
    
    private void updateLightStatusByEspStatusLight(boolean isGet, IEspStatusLight statusLight)
    {
        // get post value
        int period = statusLight.getPeriod();
        log.debug("updateLightStatusByEspStatusLight() statusLight:" + statusLight);
        int post_red = statusLight.getRed();
        int post_green = statusLight.getGreen();
        int post_blue = statusLight.getBlue();
        int post_cw = statusLight.getCWhite();
        int post_ww = statusLight.getWWhite();
        
        // update light status
        log.info("updateLightStatusByEspStatusLight() mLightStatus.updateByPost");
        mLightStatus.updateByPost(period, post_red, post_green, post_blue, post_cw, post_ww);
        
        // get seekbar value
        int seekbar_period = mLightStatus.getPeriod();
        int seekbar_red = mLightStatus.getSeekbarRed();
        int seekbar_green = mLightStatus.getSeekbarGreen();
        int seekbar_blue = mLightStatus.getSeekbarBlue();
        int seekbar_cw = mLightStatus.getSeekbarCw();
        int seekbar_ww = mLightStatus.getSeekbarWw();
        
        // update period
        setLightPeriod(seekbar_period);
        updateLightPeridTextBySeekbar();
        
        // update red,green,blue,cw,ww seekbar and text
        final double norm_rgbw_min = NORM_MIN;
        final double norm_rgbw_max = NORM_MAX;
        int rgbw_max_prev = EspLightRecord.getRgbwMax(seekbar_period);
        int rgbw_max = EspLightRecord.getRgbwMax(period);
        // update red seekbar and text
        int red_seekbar_prev = seekbar_red;
        final int rgbw_min_prev = 0;
        String red_norm_str = EspLightRecord.getNormStrBySeekbar(red_seekbar_prev, rgbw_min_prev, rgbw_max_prev);
        double red_norm = Double.parseDouble(red_norm_str);
        int red_seekbar = EspLightRecord.getSeekbarByNorm(red_norm, norm_rgbw_min, norm_rgbw_max, rgbw_max);
        setLightSeekbarRedMax(rgbw_max);
        setLightSeekbarRed(red_seekbar);
        updateLightRedUIBySeekbar();
        // update green seekbar and text
        int green_seekbar_prev = seekbar_green;
        String green_norm_str = EspLightRecord.getNormStrBySeekbar(green_seekbar_prev, rgbw_min_prev, rgbw_max_prev);
        double green_norm = Double.parseDouble(green_norm_str);
        int green_seekbar = EspLightRecord.getSeekbarByNorm(green_norm, norm_rgbw_min, norm_rgbw_max, rgbw_max);
        setLightSeekbarGreenMax(rgbw_max);
        setLightSeekbarGreen(green_seekbar);
        updateLightGreenUIBySeekbar();
        // update blue seekbar and text
        int blue_seekbar_prev = seekbar_blue;
        String blue_norm_str = EspLightRecord.getNormStrBySeekbar(blue_seekbar_prev, rgbw_min_prev, rgbw_max_prev);
        double blue_norm = Double.parseDouble(blue_norm_str);
        int blue_seekbar = EspLightRecord.getSeekbarByNorm(blue_norm, norm_rgbw_min, norm_rgbw_max, rgbw_max);
        setLightSeekbarBlueMax(rgbw_max);
        setLightSeekbarBlue(blue_seekbar);
        updateLightBlueUIBySeekbar();
        // update cw seekbar and text
        int cw_seekbar_prev = seekbar_cw;
        String cw_norm_str = EspLightRecord.getNormStrBySeekbar(cw_seekbar_prev, rgbw_min_prev, rgbw_max_prev);
        double cw_norm = Double.parseDouble(cw_norm_str);
        int cw_seekbar = EspLightRecord.getSeekbarByNorm(cw_norm, norm_rgbw_min, norm_rgbw_max, rgbw_max);
        setLightSeekbarCwMax(rgbw_max);
        setLightSeekbarCw(cw_seekbar);
        updateLightCwUIBySeekbar();
        // update ww seekbar and text
        int ww_seekbar_prev = seekbar_ww;
        String ww_norm_str = EspLightRecord.getNormStrBySeekbar(ww_seekbar_prev, rgbw_min_prev, rgbw_max_prev);
        double ww_norm = Double.parseDouble(ww_norm_str);
        int ww_seekbar = EspLightRecord.getSeekbarByNorm(ww_norm, norm_rgbw_min, norm_rgbw_max, rgbw_max);
        setLightSeekbarWwMax(rgbw_max);
        setLightSeekbarWw(ww_seekbar);
        updateLightWwUIBySeekbar();
    }
    
    private void undoLightStatus()
    {
        log.info("undoLightStatus()");
        // undo light status
        mLightStatus.undoStatus();
        
        // get seekbar value
        int seekbar_red = mLightStatus.getSeekbarRed();
        int seekbar_green = mLightStatus.getSeekbarGreen();
        int seekbar_blue = mLightStatus.getSeekbarBlue();
        int seekbar_cw = mLightStatus.getSeekbarCw();
        int seekbar_ww = mLightStatus.getSeekbarWw();
        
        // update UI by seekbar and text
        setLightSeekbarRed(seekbar_red);
        setLightSeekbarGreen(seekbar_green);
        setLightSeekbarBlue(seekbar_blue);
        setLightSeekbarCw(seekbar_cw);
        setLightSeekbarWw(seekbar_ww);
        updateLightRedUIBySeekbar();
        updateLightGreenUIBySeekbar();
        updateLightBlueUIBySeekbar();
        updateLightCwUIBySeekbar();
        updateLightWwUIBySeekbar();
    }
    
    @Override
    public void onClick(View view)
    {
        clearEditFocus();
        
        if (view == mConfirmBtn || view == mSwitch)
        {
            int period = getLightPeriod();
            if (view == mSwitch)
            {
                // set seekbar to 0 or max
                int seekbar = mSwitch.isChecked() ? EspLightRecord.getRgbwMax(period) : 0;
                setLightSeekbarRed(seekbar);
                setLightSeekbarGreen(seekbar);
                setLightSeekbarBlue(seekbar);
                setLightSeekbarCw(seekbar);
                setLightSeekbarWw(seekbar);
            }
            
            int seekbar_red = getLightSeekbarRed();
            int seekbar_green = getLightSeekbarGreen();
            int seekbar_blue = getLightSeekbarBlue();
            int seekbar_cw = getLightSeekbarCw();
            int seekbar_ww = getLightSeekbarWw();
            
            int post_red = EspLightRecord.getPostBySeekbar(period, seekbar_red);
            int post_green = EspLightRecord.getPostBySeekbar(period, seekbar_green);
            int post_blue = EspLightRecord.getPostBySeekbar(period, seekbar_blue);
            int post_cw = EspLightRecord.getPostBySeekbar(period, seekbar_cw);
            int post_ww = EspLightRecord.getPostBySeekbar(period, seekbar_ww);
            
            IEspStatusLight status = new EspStatusLight();
            // set EspStatusLight
            status.setPeriod(period);
            status.setRed(post_red);
            status.setGreen(post_green);
            status.setBlue(post_blue);
            status.setCWhite(post_cw);
            status.setWWhite(post_ww);
            updateLightStatusByEspStatusLight(true, status);
            // execute status
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
    }

    @Override
    public void onColorChanged(View v, int color)
    {
        mColorDisplay.setBackgroundColor(color);
        updateLightStatusByColor(color);
        
        int period = mLightStatus.getPeriod();
        int post_red = mLightStatus.getPostRed();
        int post_green = mLightStatus.getPostGreen();
        int post_blue = mLightStatus.getPostBlue();
        int post_cw = mLightStatus.getPostCw();
        int post_ww = mLightStatus.getPostWw();
        IEspStatusLight statusLight = new EspStatusLight();
        statusLight.setPeriod(period);
        statusLight.setRed(post_red);
        statusLight.setGreen(post_green);
        statusLight.setBlue(post_blue);
        statusLight.setCWhite(post_cw);
        statusLight.setWWhite(post_ww);
        
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
        
        log.debug("executeStatus() status:" + status);
        executePost(status);
    }
    
    @Override
    protected void executePrepare()
    {
    }
    
    @Override
    protected void executeFinish(int command, boolean result)
    {
        IEspStatusLight statusLight = mDeviceLight.getStatusLight();
        
        if (command == COMMAND_GET)
        {
            log.debug("executeFinish() command == COMMAND_GET");
            int period = statusLight.getPeriod();
            int post_red = statusLight.getRed();
            int post_green = statusLight.getGreen();
            int post_blue = statusLight.getBlue();
            int post_cw = statusLight.getCWhite();
            int post_ww = statusLight.getWWhite();
            mLightStatus = EspLightRecord.createInstanceByPost(period, post_red, post_green, post_blue, post_cw, post_ww);
        }
        
        if (result)
        {
            if (command == COMMAND_GET)
            {
                log.debug("1. executeFinish() result && command == COMMAND_GET");
                updateLightStatusByEspStatusLight(true, statusLight);
            }
        }
        else
        {
            log.debug("executeFinish() !result && command != COMMAND_GET");
            undoLightStatus();
        }
        
        int seekbar_red = getLightSeekbarRed();
        int seekbar_green = getLightSeekbarGreen();
        int seekbar_blue = getLightSeekbarBlue();
        int seekbar_cw = getLightSeekbarCw();
        int seekbar_ww = getLightSeekbarWw();
        // Set switch on or off
        if (seekbar_red > 0 || seekbar_green > 0 || seekbar_blue > 0 || seekbar_cw > 0 || seekbar_ww > 0)
        {
            mSwitch.setChecked(true);
        }
        else
        {
            mSwitch.setChecked(false);
        }
        
        checkHelpExecuteFinish(command, result);
        
        if (command == COMMAND_GET && result)
        {
            log.debug("2. executeFinish() command == COMMAND_GET && result");
            checkLowBatteryEspnow();
        }
    }
    
    private void checkLowBatteryEspnow()
    {
        log.debug("checkLowBatteryEspnow()");
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
    
    @Override
    public void onProgressChanged(SeekBar bar, int progress, boolean fromUser)
    {
        if (fromUser)
        {
            if (bar == mLightPeriodBar)
            {
                updateLightPeridTextBySeekbar();
            }
            else if (bar == mLightRedBar)
            {
                log.debug("onProgressChanged() mLightRedBar");
                updateLightRedUIBySeekbar();
            }
            else if (bar == mLightGreenBar)
            {
                log.debug("onProgressChanged() mLightGreenBar");
                updateLightGreenUIBySeekbar();
            }
            else if (bar == mLightBlueBar)
            {
                log.debug("onProgressChanged() mLightBlueBar");
                updateLightBlueUIBySeekbar();
            }
            else if (bar == mLightCWhiteBar)
            {
                log.debug("onProgressChanged() mLightCWhiteBar");
                updateLightCwUIBySeekbar();
            }
            else if (bar == mLightWWhiteBar)
            {
                log.debug("onProgressChanged() mLightWWhiteBar");
                updateLightWwUIBySeekbar();
            }
        }
        
        if (mSeekBarContainer.getVisibility() == View.VISIBLE && bar != mLightPeriodBar)
        {
            // update color
            int period = getLightPeriod();
            int seekbar_red = getLightSeekbarRed();
            int color_red = EspLightRecord.getColorBySeekbarPeriod(seekbar_red, period);
            int seekbar_green = getLightSeekbarGreen();
            int color_green = EspLightRecord.getColorBySeekbarPeriod(seekbar_green, period);
            int seekbar_blue = getLightSeekbarBlue();
            int color_blue = EspLightRecord.getColorBySeekbarPeriod(seekbar_blue, period);
            int color = Color.rgb(color_red, color_green, color_blue);
            log.debug("onProgressChanged(),period:" + period + ",seekbar_red:" + seekbar_red + ",seekbar_green:"
                + seekbar_green + ",seekbar_blue:" + seekbar_blue + ",color_red:" + color_red + ",color_green:"
                + color_green + ",color_blue" + color_blue);
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
            log.debug("onProgressChanged() mLightPeriodBar");
            updateLightPeriodUIBySeekbar();
        }
    }
    
    @Override
    public void onFocusChange(View v, boolean hasFocus)
    {
        if (!hasFocus)
        {
            if (v == mPeriodText)
            {
                log.debug("onFocusChange() mPeriodText");
                updateLightPeriodUIByText();
            }
            
            else if (v == mRedText)
            {
                log.debug("onFocusChange() mRedText");
                updateLightRedUIByNorm();
            }
            else if (v == mGreenText)
            {
                log.debug("onFocusChange() mGreenText");
                updateLightGreenUIByNorm();
            }
            else if (v == mBlueText)
            {
                log.debug("onFocusChange() mBlueText");
                updateLightBlueUIByNorm();
            }
            else if (v == mCWhiteText)
            {
                log.debug("onFocusChange() mCWhiteText");
                updateLightCwUIByNorm();
            }
            else if (v == mWWhiteText)
            {
                log.debug("onFocusChange() mWWhiteText");
                updateLightWwUIByNorm();
            }
        }
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
