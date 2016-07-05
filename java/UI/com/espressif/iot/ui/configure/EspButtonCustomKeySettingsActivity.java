package com.espressif.iot.ui.configure;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.ToggleButton;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.espressif.iot.R;
import com.espressif.iot.device.IEspDevice;
import com.espressif.iot.type.device.other.EspButtonKeySettings;
import com.espressif.iot.ui.main.EspActivityAbs;
import com.espressif.iot.ui.widget.view.EspColorPicker;
import com.espressif.iot.ui.widget.view.EspColorPicker.OnColorChangedListener;
import com.espressif.iot.user.IEspUser;
import com.espressif.iot.user.builder.BEspUser;
import com.espressif.iot.util.EspStrings;

public class EspButtonCustomKeySettingsActivity extends EspActivityAbs implements OnClickListener,
     OnColorChangedListener, OnSeekBarChangeListener, OnCheckedChangeListener, OnLongClickListener
{
    private final Logger log = Logger.getLogger(getClass());
    
    private IEspUser mUser;
    private IEspDevice mDevice;
    
    private List<Button> mKeys;
    private Button mKey1; // left up button
    private Button mKey2; // right up button
    private Button mKey3; // left down button
    private Button mKey4; // right down button
    private Button mKeyU; // up button
    private Button mKeyD; // down button
    private Button mKeyL; // left button
    private Button mKeyR; // right button
    
    private AlertDialog mColorPickerDialog;
    private View mColorDisplay;
    private EspColorPicker mColorPicker;
    private CheckBox mColorBroadCB;
    
    private AlertDialog mBrightnessDialog;
    private TextView mBrightnessTV;
    private SeekBar mBrightnessBar;
    private Switch mBrightnessSW;
    private CheckBox mBrightnessBroadCB;
    
    private AlertDialog mTimerDialog;
    private EditText mTimerTimeET;
    private CheckBox mTimerBroadCB;
    
    private AlertDialog mTurnOnOffDialog;
    private CheckBox mRevesingCB;
    private ToggleButton mOnOffTB;
    private CheckBox mOnOffBroadCB;
    
    private AlertDialog mFuncSelectDialog;
    private String[] mFuncs;
    
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.espbutton_custom_key_settings);
        
        Intent intent = getIntent();
        String deviceKey = intent.getStringExtra(EspStrings.Key.DEVICE_KEY_KEY);
        
        mUser = BEspUser.getBuilder().getInstance();
        mDevice = mUser.getUserDevice(deviceKey);
        
        mFuncs = getResources().getStringArray(R.array.esp_espbutton_actions);
        mKeys = new ArrayList<Button>();
        
        initButtonImg();
        
        initKeys();
        
        initDialogs();
        
        executeGetSettings();
    }
    
    private void initButtonImg()
    {
        Point point = new Point();
        getWindowManager().getDefaultDisplay().getSize(point);
        int _short = point.x < point.y ? point.x : point.y;
        ImageView buttonImg = (ImageView)findViewById(R.id.button_img);
        buttonImg.setMaxHeight(_short);
        buttonImg.setMaxWidth(_short);
        ImageView buttonCover = (ImageView)findViewById(R.id.button_cover);
        buttonCover.setMaxHeight(_short);
        buttonCover.setMaxWidth(_short);
    }
    
    private void initKeys()
    {
        mKey1 = (Button)findViewById(R.id.key_1);
        setButtonKeyAttrs(mKey1, 1);
        
        mKey2 = (Button)findViewById(R.id.key_2);
        setButtonKeyAttrs(mKey2, 2);
        
        mKey3 = (Button)findViewById(R.id.key_3);
        setButtonKeyAttrs(mKey3, 3);
        
        mKey4 = (Button)findViewById(R.id.key_4);
        setButtonKeyAttrs(mKey4, 4);
        
        mKeyU = (Button)findViewById(R.id.key_up);
        setButtonKeyAttrs(mKeyU, 5);
        
        mKeyD = (Button)findViewById(R.id.key_down);
        setButtonKeyAttrs(mKeyD, 6);
        
        mKeyL = (Button)findViewById(R.id.key_left);
        setButtonKeyAttrs(mKeyL, 7);
        
        mKeyR = (Button)findViewById(R.id.key_right);
        setButtonKeyAttrs(mKeyR, 8);
    }

    private void setButtonKeyAttrs(Button key, int id)
    {
        key.setOnClickListener(this);
        key.setOnLongClickListener(this);
        EspButtonKeySettings settings = new EspButtonKeySettings();
        settings.setId(id);
        settings.initActions();
        key.setTag(settings);
        
        mKeys.add(key);
    }
    
    private void initDialogs()
    {
        View colorView = View.inflate(this, R.layout.espbutton_color_picker, null);
        mColorDisplay = colorView.findViewById(R.id.color_display);
        mColorPicker = (EspColorPicker)colorView.findViewById(R.id.color_picker);
        mColorPicker.setOnColorChangeListener(this);
        mColorBroadCB = (CheckBox)colorView.findViewById(R.id.broadcast_cb);
        mColorPickerDialog = new AlertDialog.Builder(this).setTitle(R.string.esp_espbutton_custom_select_color)
            .setView(colorView)
            .create();
            
        View brightnessView = View.inflate(this, R.layout.espbutton_brightness, null);
        mBrightnessTV = (TextView)brightnessView.findViewById(R.id.percent_text);
        mBrightnessBar = (SeekBar)brightnessView.findViewById(R.id.percent_seekbar);
        mBrightnessBar.setOnSeekBarChangeListener(this);
        mBrightnessSW = (Switch)brightnessView.findViewById(R.id.brightness_indi);
        mBrightnessSW.setOnCheckedChangeListener(this);
        mBrightnessBroadCB = (CheckBox)brightnessView.findViewById(R.id.broadcast_cb);
        mBrightnessDialog = new AlertDialog.Builder(this).setTitle(R.string.esp_espbutton_custom_brightness_percent)
            .setView(brightnessView)
            .create();
            
        View timerView = View.inflate(this, R.layout.espbutton_timer, null);
        mTimerTimeET = (EditText)timerView.findViewById(R.id.timer_time_edit);
        mTimerBroadCB = (CheckBox)timerView.findViewById(R.id.broadcast_cb);
        mTimerDialog =
            new AlertDialog.Builder(this).setTitle(R.string.esp_espbutton_custion_timer).setView(timerView).create();
            
        View onOffView = View.inflate(this, R.layout.espbutton_turn_onoff, null);
        mRevesingCB = (CheckBox)onOffView.findViewById(R.id.turn_onoff_cb);
        mRevesingCB.setOnCheckedChangeListener(this);
        mOnOffTB = (ToggleButton)onOffView.findViewById(R.id.turn_onoff_tb);
        mOnOffTB.setOnCheckedChangeListener(this);
        mOnOffBroadCB = (CheckBox)onOffView.findViewById(R.id.broadcast_cb);
        // forbid set turn on/off start
        mRevesingCB.setChecked(true);
        mRevesingCB.setEnabled(false);
        // forbid set turn on/off end
        mTurnOnOffDialog =
            new AlertDialog.Builder(this).setTitle(R.string.esp_espbutton_custom_turnonoff).setView(onOffView).create();
    }
    
    private void showFuncSelectDialog(int checkedItem, FuncSelectDialogClickListener listener)
    {
        if (mFuncSelectDialog != null)
        {
            mFuncSelectDialog.dismiss();
            mFuncSelectDialog = null;
        }
        
        mFuncSelectDialog =
            new AlertDialog.Builder(this).setSingleChoiceItems(mFuncs, checkedItem, null)
                .setPositiveButton(android.R.string.ok, listener)
                .show();
    }
    
    @Override
    public void onClick(View v)
    {
        if (mKeys.contains(v))
        {
            EspButtonKeySettings keySettings = (EspButtonKeySettings)v.getTag();
            EspButtonKeySettings postSettings = new EspButtonKeySettings();
            postSettings.setId(keySettings.getId());
            postSettings.initShortPressAction();
            
            showFuncSelectDialog(keySettings.getShortPressAction().getFunc().ordinal(),
                new FuncSelectDialogClickListener(v, postSettings));
        }
    }
    
    @Override
    public boolean onLongClick(View v)
    {
        if (mKeys.contains(v))
        {
            EspButtonKeySettings keySettings = (EspButtonKeySettings)v.getTag();
            EspButtonKeySettings postSettings = new EspButtonKeySettings();
            postSettings.setId(keySettings.getId());
            postSettings.initLongPressAction();
            
            showFuncSelectDialog(keySettings.getLongPressAction().getFunc().ordinal(),
                new FuncSelectDialogClickListener(v, postSettings));
            return true;
        }
        
        return false;
    }
    
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
    {
        if (buttonView == mRevesingCB)
        {
            mOnOffTB.setVisibility(isChecked ? View.GONE : View.VISIBLE);
            
            // forbid set turn on/off start
            mOnOffTB.setVisibility(View.GONE);
            // forbid set turn on/off end
        }
        else if (buttonView == mBrightnessSW)
        {
            mBrightnessBar.setProgress(isChecked ? 1 : 0);
        }
    }
    
    private void setBrightnessPercent(int percent)
    {
        mBrightnessBar.setProgress(percent);
        mBrightnessTV.setText(getString(R.string.esp_espbutton_custom_brightness_text, percent));
    }
    
    @Override
    public void onColorChangeStart(View v, int color)
    {
        mColorDisplay.setBackgroundColor(color);
    }

    @Override
    public void onColorChanged(View v, int color)
    {
        mColorDisplay.setBackgroundColor(color);
    }

    @Override
    public void onColorChangeEnd(View v, int color)
    {
        mColorDisplay.setBackgroundColor(color);
        mColorDisplay.setTag(color);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
    {
        if (seekBar == mBrightnessBar && fromUser)
        {
            setBrightnessPercent(progress);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar)
    {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar)
    {
    }
    
    private EspButtonKeySettings.Action getPostPressAction(EspButtonKeySettings postSettings)
    {
        EspButtonKeySettings.Action pressAction;
        if (postSettings.getShortPressAction() != null)
        {
            pressAction = postSettings.getShortPressAction();
        }
        else
        {
            pressAction = postSettings.getLongPressAction();
        }
        
        return pressAction;
    }
    
    private class FuncSelectDialogClickListener implements DialogInterface.OnClickListener
    {
        private View mKeyBtn;
        private EspButtonKeySettings mPostSettings;
        
        public FuncSelectDialogClickListener(View key, EspButtonKeySettings postSettings)
        {
            mKeyBtn = key;
            mPostSettings = postSettings;
        }
        
        @Override
        public void onClick(DialogInterface dialog, int which)
        {
            AlertDialog alertDialog = (AlertDialog)dialog;
            
            EspButtonKeySettings keySettings = (EspButtonKeySettings)mKeyBtn.getTag();
            EspButtonKeySettings.Action keyAction;
            if (mPostSettings.getShortPressAction() != null)
            {
                keyAction = keySettings.getShortPressAction();
            }
            else
            {
                keyAction = keySettings.getLongPressAction();
            }
            
            EspButtonKeySettings.Action postAction = getPostPressAction(mPostSettings);
            
            int checked = alertDialog.getListView().getCheckedItemPosition();
            EspButtonKeySettings.Func func = EspButtonKeySettings.Func.values()[checked];
            postAction.setFunc(func);
            
            FuncDialogClickListener listener = new FuncDialogClickListener(mPostSettings);
            switch(func)
            {
                case NIL:
                    postAction.setBroadcast(false);
                    executePostSettings(mPostSettings);
                    break;
                case SET_COLOR:
                    mColorBroadCB.setChecked(keyAction.isBroadcast());
                    int color = Color.rgb(keyAction.getRed(), keyAction.getGreen(), keyAction.getBlue());
                    mColorDisplay.setBackgroundColor(color);
                    mColorDisplay.setTag(color);
                    
                    mColorPickerDialog.setButton(DialogInterface.BUTTON_POSITIVE,
                        getString(android.R.string.ok),
                        listener);
                    mColorPickerDialog.show();
                    break;
                case TURN_ONOFF:
                    mOnOffBroadCB.setChecked(keyAction.isBroadcast());
                    switch (keyAction.getTurnOnOff())
                    {
                        case EspButtonKeySettings.TURN_ONOFF:
                            mRevesingCB.setChecked(true);
                            break;
                        case EspButtonKeySettings.TURN_ON:
                            mRevesingCB.setChecked(false);
                            mOnOffTB.setChecked(true);
                            break;
                        case EspButtonKeySettings.TURN_OFF:
                            mRevesingCB.setChecked(false);
                            mOnOffTB.setChecked(false);
                            break;
                    }
                    
                    // forbid set turn on/off start
                    mRevesingCB.setChecked(true);
                    // forbid set turn on/off end
                    
                    mTurnOnOffDialog.setButton(DialogInterface.BUTTON_POSITIVE,
                        getString(android.R.string.ok),
                        listener);
                    mTurnOnOffDialog.show();
                    break;
                case SET_TIMER:
                    mTimerBroadCB.setChecked(keyAction.isBroadcast());
                    mTimerTimeET.setText("" + keyAction.getTimerTime());
                    
                    mTimerDialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(android.R.string.ok), listener);
                    mTimerDialog.show();
                    break;
                case SET_BRIGHTNESS:
                    mBrightnessBroadCB.setChecked(keyAction.isBroadcast());
                    mBrightnessSW.setChecked(keyAction.getBrightness() == 1);
                    
                    mBrightnessDialog.setButton(DialogInterface.BUTTON_POSITIVE,
                        getString(android.R.string.ok),
                        listener);
                    mBrightnessDialog.show();
                    break;
            }
        }
    }
    
    private class FuncDialogClickListener implements DialogInterface.OnClickListener
    {
        private EspButtonKeySettings mPostSettings;
        private EspButtonKeySettings.Action mPostAction;
        
        public FuncDialogClickListener(EspButtonKeySettings postSettings)
        {
            mPostSettings = postSettings;
            mPostAction = getPostPressAction(mPostSettings);
        }
        
        @Override
        public void onClick(DialogInterface dialog, int which)
        {
          if (dialog == mColorPickerDialog)
          {
              int color = (Integer)mColorDisplay.getTag();
              mPostAction.setRed(Color.red(color));
              mPostAction.setGreen(Color.green(color));
              mPostAction.setBlue(Color.blue(color));
              mPostAction.setBroadcast(mColorBroadCB.isChecked());
              executePostSettings(mPostSettings);
          }
          else if (dialog == mBrightnessDialog)
          {
              int brightness = mBrightnessBar.getProgress();
              mPostAction.setBrightness(brightness);
              mPostAction.setBroadcast(mBrightnessBroadCB.isChecked());
              executePostSettings(mPostSettings);
          }
          else if (dialog == mTimerDialog)
          {
              String timeStr = mTimerTimeET.getText().toString();
              long time = TextUtils.isEmpty(timeStr) ? 0 : Long.parseLong(timeStr);
              mPostAction.setTimerTime(time);
              mPostAction.setBroadcast(mTimerBroadCB.isChecked());
              executePostSettings(mPostSettings);
          }
          else if (dialog == mTurnOnOffDialog)
            {
                int onOff =
                    mRevesingCB.isChecked() ? EspButtonKeySettings.TURN_ONOFF
                        : (mOnOffTB.isChecked() ? EspButtonKeySettings.TURN_ON : EspButtonKeySettings.TURN_OFF);
                mPostAction.setTurnOnOff(onOff);
                mPostAction.setBroadcast(mOnOffBroadCB.isChecked());
                executePostSettings(mPostSettings);
            }
        }
    }
    
    private class KeyActionGetTask extends AsyncTask<Void, Void, List<EspButtonKeySettings>>
    {
        private Activity mActivity;
        
        private ProgressDialog mProgressDialog;
        
        public KeyActionGetTask(Activity activity)
        {
            mActivity = activity;
        }
        
        @Override
        protected void onPreExecute()
        {
            mProgressDialog = new ProgressDialog(mActivity);
            mProgressDialog.setMessage(getString(R.string.esp_espbutton_custom_get_settings));
            mProgressDialog.setCancelable(false);
            mProgressDialog.show();
            
            log.debug("onPreExecute() KeyActionGetTask");
        }
        
        @Override
        protected List<EspButtonKeySettings> doInBackground(Void... params)
        {
            return mUser.doActionEspButtonKeyActionGet(mDevice);
        }
        
        @Override
        protected void onPostExecute(List<EspButtonKeySettings> result)
        {
            mProgressDialog.dismiss();
            mProgressDialog = null;
            
            if (result != null)
            {
                for (EspButtonKeySettings settings : result)
                {
                    setKeyStatus(settings);
                }
                
                for (Button key : mKeys)
                {
                    if (key.getTag() != null)
                    {
                        log.info(key.getTag().toString());
                    }
                }
            }
            
            log.debug("onPostExecute() KeyActionGetTask result = " + result);
        }
    }
    
    private class KeyActionSetTask extends AsyncTask<Void, Void, Boolean>
    {
        private Activity mActivity;
        private EspButtonKeySettings mPostSettings;
        
        private ProgressDialog mProgressDialog;
        
        public KeyActionSetTask(Activity activity, EspButtonKeySettings settings)
        {
            mActivity = activity;
            mPostSettings = settings;
        }
        
        @Override
        protected void onPreExecute()
        {
            mProgressDialog = new ProgressDialog(mActivity);
            mProgressDialog.setMessage(getString(R.string.esp_espbutton_custom_post_settings));
            mProgressDialog.setCancelable(false);
            mProgressDialog.show();
            
            log.debug("onPreExecute() KeyActionSetTask post = " + mPostSettings.toString());
        }
        
        @Override
        protected Boolean doInBackground(Void... params)
        {
            return mUser.doActionEspButtonKeyActionSet(mDevice, mPostSettings);
        }
        
        @Override
        protected void onPostExecute(Boolean result)
        {
            mProgressDialog.dismiss();
            mProgressDialog = null;
            
            if (result)
            {
                setKeyStatus(mPostSettings);
            }
            
            log.debug("onPostExecute() KeyActionSetTask result = " + result);
        }
    }
    
    private void executeGetSettings()
    {
        new KeyActionGetTask(this).execute();
    }
    
    private void executePostSettings(EspButtonKeySettings settings)
    {
        new KeyActionSetTask(this, settings).execute();
    }
    
    private void setKeyStatus(EspButtonKeySettings settings)
    {
        for (Button key : mKeys)
        {
            EspButtonKeySettings keySettings = (EspButtonKeySettings)key.getTag();
            if (keySettings.getId() == settings.getId())
            {
                if (settings.getShortPressAction() != null)
                {
                    EspButtonKeySettings.Action shortAction = settings.getShortPressAction();
                    EspButtonKeySettings.Action keyShortAction = keySettings.getShortPressAction();
                    setKeySettingsAction(keyShortAction, shortAction);
                }
                
                if (settings.getLongPressAction() != null)
                {
                    EspButtonKeySettings.Action longAction = settings.getLongPressAction();
                    EspButtonKeySettings.Action keyLongAction = keySettings.getLongPressAction();
                    setKeySettingsAction(keyLongAction, longAction);
                }
                
                break;
            }
        }
    }
    
    private void setKeySettingsAction(EspButtonKeySettings.Action keyAction, EspButtonKeySettings.Action setAction)
    {
        EspButtonKeySettings.Func func = setAction.getFunc();
        keyAction.setFunc(func);
        keyAction.setBroadcast(setAction.isBroadcast());
        switch(func)
        {
            case NIL:
                break;
            case SET_COLOR:
                keyAction.setRed(setAction.getRed());
                keyAction.setGreen(setAction.getGreen());
                keyAction.setBlue(setAction.getBlue());
                break;
            case SET_TIMER:
                keyAction.setTimerTime(setAction.getTimerTime());
                break;
            case TURN_ONOFF:
                keyAction.setTurnOnOff(setAction.getTurnOnOff());
                break;
            case SET_BRIGHTNESS:
                keyAction.setBrightness(setAction.getBrightness());
                break;
        }
    }
}
