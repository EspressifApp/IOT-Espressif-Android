package com.espressif.iot.ui.device.soundbox;

import com.espressif.iot.R;
import com.espressif.iot.device.IEspDeviceSoundbox;
import com.espressif.iot.type.device.other.EspAudio;
import com.espressif.iot.type.device.status.EspStatusSoundbox;
import com.espressif.iot.type.device.status.IEspStatusSoundbox;
import com.espressif.iot.ui.device.DeviceActivityAbs;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;

/*
 * DeviceSoundboxActivity - PlatformBaseFragment - TrackBaseFragment
 */
public class DeviceSoundboxActivity extends DeviceActivityAbs
    implements OnClickListener, OnSeekBarChangeListener, OnItemSelectedListener {
    private IEspDeviceSoundbox mSoundbox;

    private Button mPlayBtn;
    private Button mPauseBtn;
    private SeekBar mVolumeBar;

    private Spinner mPlatSpinner;
    private static final String[] PLATFORMS = new String[] {"Ximalaya", "Qingting"};
    private PlatformXimaFragment mXimaFm;
    private PlatformBaseFragment mCurrentFm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

         mSoundbox = (IEspDeviceSoundbox)mIEspDevice;

        new Handler().post(new Runnable() {

            @Override
            public void run() {
                mXimaFm = new PlatformXimaFragment();
                mXimaFm.setSoundbox(mSoundbox);

                getFragmentManager().beginTransaction()
                    .add(R.id.container, mXimaFm, mXimaFm.getFragmentTag())
                    .show(mXimaFm)
                    .commit();
                mCurrentFm = mXimaFm;
            }
        });

        setRefreshIcon();
    }

    @Override
    protected View initControlView() {
        View view = View.inflate(this, R.layout.device_activity_soundbox, null);

        mPlayBtn = (Button)view.findViewById(R.id.play_btn);
        mPlayBtn.setOnClickListener(this);
        mPauseBtn = (Button)view.findViewById(R.id.pause_btn);
        mPauseBtn.setOnClickListener(this);

        mVolumeBar = (SeekBar)view.findViewById(R.id.volume_bar);
        mVolumeBar.setOnSeekBarChangeListener(this);

        mPlatSpinner = (Spinner)view.findViewById(R.id.platform_spinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, PLATFORMS);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mPlatSpinner.setAdapter(adapter);
        mPlatSpinner.setOnItemSelectedListener(this);
        mPlatSpinner.setVisibility(View.GONE);

        return view;
    }

    private void setRefreshIcon() {
        ImageView icon = new ImageView(this);
        icon.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        icon.setScaleType(ScaleType.CENTER_INSIDE);
        icon.setImageResource(android.R.drawable.ic_menu_rotate);
        icon.setBackgroundResource(R.drawable.esp_activity_icon_background);
        icon.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mCurrentFm.refresh();
            }
        });
        setTitleContentView(icon);
    }

    @Override
    public void onClick(View v) {
        if (v == mPlayBtn) {
            IEspStatusSoundbox status =
                generateStatus(IEspStatusSoundbox.ACTION_PLAY, IEspStatusSoundbox.PLAY_STATUS_PLAYING);
            executePost(status);
        } else if (v == mPauseBtn) {
            IEspStatusSoundbox status =
                generateStatus(IEspStatusSoundbox.ACTION_PLAY, IEspStatusSoundbox.PLAY_STATUS_PAUSE);
            executePost(status);
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        int volume = seekBar.getProgress() + 50;
        if (volume != mSoundbox.getStatusSoundbox().getVolume()) {
            IEspStatusSoundbox status = generateStatus(IEspStatusSoundbox.ACTION_VOLUME, volume);
            executePost(status);
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        PlatformBaseFragment showFm = null;
        PlatformBaseFragment hideFm = null;
        switch (position) {
            case 0:
                showFm = mXimaFm;
                break;
            case 1:
                hideFm = mXimaFm;
                break;
        }

        getFragmentManager().beginTransaction().show(showFm).hide(hideFm).commit();
        mCurrentFm = showFm;
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    @Override
    protected void executePrepare() {
    }

    @Override
    protected void executeFinish(int command, boolean result) {
    }

    private IEspStatusSoundbox generateStatus(int action, Object param) {
        IEspStatusSoundbox status = new EspStatusSoundbox();
        status.setAction(action);
        switch (action) {
            case IEspStatusSoundbox.ACTION_AUDIO:
                EspAudio audio = (EspAudio)param;
                status.setAudio(audio);
                break;
            case IEspStatusSoundbox.ACTION_PLAY:
                int playStatus = (Integer)param;
                status.setPlayStatus(playStatus);
                break;
            case IEspStatusSoundbox.ACTION_VOLUME:
                int volume = (Integer)param;
                status.setVolume(volume);
                break;
            default:
                return null;
        }

        return status;
    }

    public void postAudio(EspAudio audio) {
        IEspStatusSoundbox status = generateStatus(IEspStatusSoundbox.ACTION_AUDIO, audio);
        executePost(status);
    }
}
