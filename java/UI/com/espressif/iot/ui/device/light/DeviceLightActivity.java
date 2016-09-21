package com.espressif.iot.ui.device.light;

import org.apache.log4j.Logger;

import com.espressif.iot.R;
import com.espressif.iot.action.device.common.upgrade.EspDeviceUpgradeParser;
import com.espressif.iot.action.device.common.upgrade.IEspDeviceUpgradeInfo;
import com.espressif.iot.command.device.IEspCommandLight;
import com.espressif.iot.device.IEspDeviceLight;
import com.espressif.iot.type.device.status.EspStatusLight;
import com.espressif.iot.type.device.status.IEspStatusLight;
import com.espressif.iot.ui.configure.EspButtonCustomKeySettingsActivity;
import com.espressif.iot.ui.device.DeviceBaseActivity;
import com.espressif.iot.ui.task.DeviceStatusTask;
import com.espressif.iot.ui.widget.view.ColorView;
import com.espressif.iot.ui.widget.view.EspRefreshableLayout;
import com.espressif.iot.ui.widget.view.EspRefreshableLayout.OnRefreshListener;
import com.espressif.iot.util.ColorUtil;
import com.espressif.iot.util.EspStrings;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class DeviceLightActivity extends DeviceBaseActivity
    implements OnRefreshListener, OnCheckedChangeListener, OnSeekBarChangeListener, OnClickListener {
    private static final Logger log = Logger.getLogger(DeviceLightActivity.class);

    private static final int[] COLORS =
        new int[] {Color.WHITE, 0xffff0000, 0xffffff00, 0xff00ff00, 0xff00ffff, 0xff0000ff, 0xffff00ff};

    private static final int BRIGHTNESS_MAX = 100;
    private static final int BRIGHTNESS_TRANSPARENT_PROGRESS = 100;
    private static final int BRIGHTNESS_COLOR_BRIGHT = Color.WHITE;
    private static final int BRIGHTNESS_COLOR_DARK = Color.BLACK;

    private static final int RGB_MAX = 255;

    private static final int MENU_ID_ESPBUTTON_SETTINGS = 0x2001;

    private IEspDeviceLight mLight;

    private EspRefreshableLayout mRefreshLayout;

    private CheckBox mOnOffCB;

    private TextView mTitleTV;
    private ImageView mMenuIV;
    private ColorView mColorView;
    private EspHorizontalListView mListView;
    private SeekBar mBrightnessBar;

    private DeviceStatusTask mStatusTask;

    private IEspStatusLight mCurrentStatus;
    private int mOnColor;

    private DeviceLightActivity mActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mLight = (IEspDeviceLight)mDevice;
        mActivity = this;

        setContentView(R.layout.device_activity_light_main);

        mRefreshLayout = (EspRefreshableLayout)findViewById(R.id.refresh_layout);
        mRefreshLayout.setOnRefreshListener(this);
        mRefreshLayout.setRefreshable(isLightVersionNew());
        mRefreshLayout.setRefreshable(false);

        mOnOffCB = (CheckBox)findViewById(R.id.light_on_off);

        mColorView = (ColorView)findViewById(R.id.light_color_view);

        mTitleTV = (TextView)findViewById(R.id.device_name);
        mTitleTV.setTextColor(mColorView.getColor());
        mTitleTV.setText(mDevice.getName());

        mCurrentStatus = new EspStatusLight();

        mMenuIV = (ImageView)findViewById(R.id.device_menu);
        mMenuIV.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                createDeviceMenuItems(v);
            }
        });
        if (isDeviceArray()) {
            mMenuIV.setVisibility(View.GONE);
        }

        mListView = (EspHorizontalListView)findViewById(R.id.list);
        ColorAdapter adapter = new ColorAdapter(this);
        mListView.setAdapter(adapter);
        mListView.setOnItemViewSelectedListener(new EspHorizontalListView.OnItemViewSelectedListener() {

            @Override
            public void onItemViewSelected(View view) {
                log.debug("Color list color selected");
                ColorView cv = (ColorView)view;
                int color = cv.getColor();
                mBrightnessBar.setProgress(BRIGHTNESS_TRANSPARENT_PROGRESS);
                setLightColor(color);
                updateCurrentStatusLight(color);
                executeColorTask(color);
            }
        });
        new Handler().post(new Runnable() {

            @Override
            public void run() {
                mListView.adjustChildrenLocation();
            }
        });

        mBrightnessBar = (SeekBar)findViewById(R.id.brightness_bar);
        mBrightnessBar.setMax(BRIGHTNESS_MAX);
        mBrightnessBar.setProgress(BRIGHTNESS_TRANSPARENT_PROGRESS);
        mBrightnessBar.setOnSeekBarChangeListener(this);

        updateOnColor(mLight.getStatusLight());
        if (!isDeviceArray() && isDeviceCompatibility()) {
            executeGetTaks();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mStatusTask != null) {
            mStatusTask.cancel(true);
        }
    }

    @Override
    protected void onCreateMenuItems(Menu menu) {
        if (mDevice.getIsMeshDevice() && !isDeviceArray()) {
            menu.add(Menu.NONE, MENU_ID_ESPBUTTON_SETTINGS, 1, R.string.esp_device_light_menu_espbutton_settings)
                .setEnabled(mDevice.getDeviceState().isStateLocal());
        }
    }

    @Override
    protected boolean onSelectMenuItem(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_ID_ESPBUTTON_SETTINGS:
                Intent intent = new Intent(this, EspButtonCustomKeySettingsActivity.class);
                intent.putExtra(EspStrings.Key.DEVICE_KEY_KEY, mDevice.getKey());
                startActivity(intent);
                return true;
        }

        return super.onSelectMenuItem(item);
    }

    @Override
    public void onRefresh() {
        mRefreshLayout.notifyRefreshComplete();
        mOnOffCB.setChecked(!mOnOffCB.isChecked());
        if (mOnOffCB.isChecked()) {
            executeTurnOn();
        } else {
            executeTurnOff();
        }
    }

    @Override
    public void onClick(View v) {
        if (v == mColorView) {
            mOnOffCB.setChecked(!mOnOffCB.isChecked());
            if (mOnOffCB.isChecked()) {
                executeTurnOn();
            } else {
                executeTurnOff();
            }
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (buttonView == mOnOffCB) {
            if (isChecked) {
                mOnOffCB.setText(R.string.esp_device_light_off);
            } else {
                mOnOffCB.setText(R.string.esp_device_light_on);
            }
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
            int offset = Math.abs(progress - BRIGHTNESS_TRANSPARENT_PROGRESS);
            float percent = (float)offset / 100f;
            int shadeColor =
                progress > BRIGHTNESS_TRANSPARENT_PROGRESS ? BRIGHTNESS_COLOR_BRIGHT : BRIGHTNESS_COLOR_DARK;
            int baseColor = 0;
            if (mCurrentStatus.getStatus() == IEspStatusLight.STATUS_BRIGHT) {
                baseColor = BRIGHTNESS_COLOR_BRIGHT;
            } else if (mCurrentStatus.getStatus() == IEspStatusLight.STATUS_COLOR) {
                baseColor = Color.rgb(mCurrentStatus.getRed(), mCurrentStatus.getGreen(), mCurrentStatus.getBlue());
            } else {
                log.warn("Shouldn't run here");
            }
            int color = ColorUtil.getGradientColor(baseColor, shadeColor, percent);
            setLightColor(color);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        log.debug("onStopTrackingTouch Set brightness");
        int color = mColorView.getColor();
        executeColorTask(color);
    }

    private boolean isLightVersionNew() {
        if (mDevice.getRom_version() == null) {
            return false;
        }

        IEspDeviceUpgradeInfo info =
            EspDeviceUpgradeParser.getInstance().parseUpgradeInfo(IEspCommandLight.PROTOCOL_NEW_VERSION);
        int protocolVersionValue = info.getVersionValue();

        info = EspDeviceUpgradeParser.getInstance().parseUpgradeInfo(mDevice.getRom_version());
        int deviceVersionValue = info.getVersionValue();

        return deviceVersionValue >= protocolVersionValue;
    }

    private class ColorAdapter extends EspHorizontalListView.InfiniteAdapter {
        private Activity mActivity;

        public ColorAdapter(Activity activity) {
            mActivity = activity;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = new ColorView(mActivity);
                int width = mActivity.getResources().getDimensionPixelSize(R.dimen.esp_light_colors_views_width);
                int height = ViewGroup.LayoutParams.MATCH_PARENT;
                int margin = mActivity.getResources().getDimensionPixelSize(R.dimen.esp_light_colors_views_margin);
                ViewGroup.MarginLayoutParams mlp = new ViewGroup.MarginLayoutParams(width, height);
                mlp.setMargins(margin, margin, margin, margin);
                convertView.setLayoutParams(mlp);
            }

            ColorView view = (ColorView)convertView;
            int colorIndex = position % COLORS.length;
            view.setColor(COLORS[colorIndex]);
            return convertView;
        }
    }

    private void setLightColor(int color) {
        mColorView.setColor(color);
        mTitleTV.setTextColor(color);
    }

    private void executeGetTaks() {
        log.debug("executeGetTaks()");
        executeDeviceTask(null);
    }

    private void executeColorTask(int color) {
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        log.debug("executeColorTask red = " + red + " green = " + green + " blue = " + blue);
        executeDeviceTask(LightUtils.generateStatus(IEspStatusLight.STATUS_COLOR, red, green, blue));
    }

    private void executeDeviceTask(IEspStatusLight status) {
        log.debug("executeDeviceTask");
        if (mStatusTask != null) {
            log.debug("cancel last task");
            mStatusTask.cancel(true);
        }

        mStatusTask = new DeviceStatusTask(mDevice);
        if (status == null) {
            log.debug("execute get task");
            mStatusTask.setOnTaskExecuteListener(new DeviceStatusTask.OnTaskExecuteListener() {

                @Override
                public void onPreExecute() {
                }

                @Override
                public void onPostExecute(Boolean result) {
                    if (result) {
                        updateLightStatus();
                        updateCurrentStatusLight(mLight.getStatusLight());
                    }
                    updateOnColor(mLight.getStatusLight());
                    int status = mLight.getStatusLight().getStatus();
                    mOnOffCB.setChecked(status != IEspStatusLight.STATUS_OFF);
                    setOnOffListener();
                }

                @Override
                public void onCancelled() {
                    setOnOffListener();
                }
            });
            mStatusTask.execute();
        } else {
            log.debug("execute post task");
            switch (status.getStatus()) {
                case IEspStatusLight.STATUS_BRIGHT:
                    mOnColor = Color.rgb(status.getWhite(), status.getWhite(), status.getWhite());
                    mOnOffCB.setChecked(true);
                    break;
                case IEspStatusLight.STATUS_COLOR:
                    mOnColor = Color.rgb(status.getRed(), status.getGreen(), status.getBlue());
                    mOnOffCB.setChecked(true);
                    break;
                default:
                    break;
            }
            mStatusTask.execute(status);
        }
    }

    private void executeTurnOn() {
        log.debug("executeTurnOn");
        if (mStatusTask != null) {
            log.debug("cancel last task");
            mStatusTask.cancel(true);
        }

        int onRed = Color.red(mOnColor);
        int onGreen = Color.green(mOnColor);
        int onBlue = Color.blue(mOnColor);
        IEspStatusLight onStatus = LightUtils.generateStatus(IEspStatusLight.STATUS_COLOR, onRed, onGreen, onBlue);
        if (onStatus.getStatus() == IEspStatusLight.STATUS_COLOR) {
            setLightColor(mOnColor);
        } else if (onStatus.getStatus() == IEspStatusLight.STATUS_BRIGHT) {
            setLightColor(mOnColor);
            int progress = mCurrentStatus.getWhite() / RGB_MAX * BRIGHTNESS_MAX;
            mBrightnessBar.setProgress(progress);
        }
        mStatusTask = new DeviceStatusTask(mDevice);
        mStatusTask.execute(LightUtils.generateStatus(IEspStatusLight.STATUS_ON));
    }

    private void executeTurnOff() {
        log.debug("executeTurnOff");
        if (mStatusTask != null) {
            log.debug("cancel last task");
            mStatusTask.cancel(true);
        }

        setLightColor(BRIGHTNESS_COLOR_DARK);
        mStatusTask = new DeviceStatusTask(mDevice);
        mStatusTask.execute(LightUtils.generateStatus(IEspStatusLight.STATUS_OFF));
    }

    private void setOnOffListener() {
        if (isLightVersionNew()) {
            mOnOffCB.setOnCheckedChangeListener(mActivity);
            mColorView.setOnClickListener(mActivity);
        }
    }

    private void updateLightStatus() {
        IEspStatusLight lightStatus = mLight.getStatusLight();
        int status = lightStatus.getStatus();
        switch (status) {
            case IEspStatusLight.STATUS_OFF:
                mOnOffCB.setText(R.string.esp_device_light_off);
                mOnOffCB.setChecked(false);
                setLightColor(BRIGHTNESS_COLOR_DARK);
                break;

            case IEspStatusLight.STATUS_ON:
                mOnOffCB.setText(R.string.esp_device_light_on);
                mOnOffCB.setChecked(true);
                break;

            case IEspStatusLight.STATUS_COLOR:
                mOnOffCB.setText(R.string.esp_device_light_on);
                mOnOffCB.setChecked(true);
                setLightColor(Color.rgb(lightStatus.getRed(), lightStatus.getGreen(), lightStatus.getBlue()));
                break;

            case IEspStatusLight.STATUS_BRIGHT:
                mOnOffCB.setText(R.string.esp_device_light_on);
                mOnOffCB.setChecked(true);
                setLightColor(Color.rgb(lightStatus.getWhite(), lightStatus.getWhite(), lightStatus.getWhite()));
                mBrightnessBar.setProgress(lightStatus.getWhite() * BRIGHTNESS_MAX / RGB_MAX );
                break;
        }
    }

    private void updateOnColor(IEspStatusLight statusLight) {
        int status = statusLight.getStatus();
        int red = statusLight.getRed();
        int green = statusLight.getGreen();
        int blue = statusLight.getBlue();
        int white = statusLight.getWhite();
        switch (status) {
            case IEspStatusLight.STATUS_OFF:
                if (red == green && red == blue && red == 0) {
                    mOnColor = Color.rgb(white, white, white);
                } else {
                    mOnColor = Color.rgb(red, green, blue);
                }
                break;
            case IEspStatusLight.STATUS_COLOR:
                mOnColor = Color.rgb(red, green, blue);
                break;
            case IEspStatusLight.STATUS_BRIGHT:
                mOnColor = Color.rgb(white, white, white);
                break;
        }
    }

    private void updateCurrentStatusLight(int color) {
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        if (red == green && red == blue) {
            mCurrentStatus.setStatus(IEspStatusLight.STATUS_BRIGHT);
            mCurrentStatus.setWhite(red);
        } else {
            mCurrentStatus.setStatus(IEspStatusLight.STATUS_COLOR);
            mCurrentStatus.setRed(red);
            mCurrentStatus.setGreen(green);
            mCurrentStatus.setBlue(blue);
        }
    }

    private void updateCurrentStatusLight(IEspStatusLight statusLight) {
        log.debug("updateCurrentStatusLight = " + statusLight.toString());
        mCurrentStatus.setStatus(statusLight.getStatus());
        mCurrentStatus.setRed(statusLight.getRed());
        mCurrentStatus.setGreen(statusLight.getGreen());
        mCurrentStatus.setBlue(statusLight.getBlue());
        mCurrentStatus.setWhite(statusLight.getWhite());
    }
}
