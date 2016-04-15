package com.google.zxing.qrcode.ui;

import java.io.IOException;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.widget.Toast;

import com.espressif.iot.R;
import com.espressif.iot.ui.configure.EspButtonConfigureActivity;
import com.espressif.iot.user.IEspUser;
import com.espressif.iot.user.builder.BEspUser;
import com.espressif.iot.util.EspStrings;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import com.google.zxing.qrcode.camera.CameraManager;
import com.google.zxing.qrcode.decoding.CaptureActivityHandler;
import com.google.zxing.qrcode.decoding.InactivityTimer;
import com.google.zxing.qrcode.view.ViewfinderView;

public class ShareCaptureActivity extends Activity implements Callback
{
    
    private static final Logger log = Logger.getLogger(ShareCaptureActivity.class);
    
    private CaptureActivityHandler handler;
    
    private ViewfinderView viewfinderView;
    
    private boolean hasSurface;
    
    private Vector<BarcodeFormat> decodeFormats;
    
    private String characterSet;
    
    private InactivityTimer inactivityTimer;
    
    private MediaPlayer mediaPlayer;
    
    private boolean playBeep;
    
    private static final float BEEP_VOLUME = 0.10f;
    
    private boolean vibrate;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.share_capture_activity);
        CameraManager.init(getApplication());
        viewfinderView = (ViewfinderView)findViewById(R.id.viewfinder_view);
        hasSurface = false;
        inactivityTimer = new InactivityTimer(this);
    }
    
    @Override
    protected void onResume()
    {
        super.onResume();
        SurfaceView surfaceView = (SurfaceView)findViewById(R.id.preview_view);
        SurfaceHolder surfaceHolder = surfaceView.getHolder();
        if (hasSurface)
        {
            initCamera(surfaceHolder);
        }
        else
        {
            surfaceHolder.addCallback(this);
        }
        decodeFormats = null;
        characterSet = null;
        
        playBeep = true;
        AudioManager audioService = (AudioManager)getSystemService(AUDIO_SERVICE);
        if (audioService.getRingerMode() != AudioManager.RINGER_MODE_NORMAL)
        {
            playBeep = false;
        }
        initBeepSound();
        vibrate = true;
    }
    
    @Override
    protected void onPause()
    {
        super.onPause();
        if (handler != null)
        {
            handler.quitSynchronously();
            handler = null;
        }
        CameraManager.get().closeDriver();
    }
    
    @Override
    protected void onDestroy()
    {
        inactivityTimer.shutdown();
        super.onDestroy();

        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
    }
    
    private void initCamera(SurfaceHolder surfaceHolder)
    {
        try
        {
            CameraManager.get().openDriver(surfaceHolder);
        }
        catch (IOException ioe)
        {
            return;
        }
        catch (RuntimeException e)
        {
            return;
        }
        if (handler == null)
        {
            handler = new CaptureActivityHandler(this, decodeFormats, characterSet);
        }
    }
    
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
    {
        
    }
    
    @Override
    public void surfaceCreated(SurfaceHolder holder)
    {
        if (!hasSurface)
        {
            hasSurface = true;
            initCamera(holder);
        }
        
    }
    
    @Override
    public void surfaceDestroyed(SurfaceHolder holder)
    {
        hasSurface = false;
        
    }
    
    public ViewfinderView getViewfinderView()
    {
        return viewfinderView;
    }
    
    public Handler getHandler()
    {
        return handler;
    }
    
    public void drawViewfinder()
    {
        viewfinderView.drawViewfinder();
        
    }
    
    public void handleDecode(final Result obj, Bitmap barcode)
    {
        inactivityTimer.onActivity();
        playBeepSoundAndVibrate();
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        JSONObject qrContentJSON = getSharedContent(obj.getText());
        ShareDialogListener clickListener = new ShareDialogListener(this, qrContentJSON);
        dialog.setNegativeButton(android.R.string.cancel, clickListener);
        dialog.setPositiveButton(android.R.string.ok, clickListener);
        setDialogMessage(dialog, qrContentJSON);
        dialog.show();
    }
    
    private JSONObject getSharedContent(String qrConent)
    {
        try
        {
            JSONObject contentJSON = new JSONObject(qrConent);
            return contentJSON;
        }
        catch (JSONException e)
        {
            e.printStackTrace();
            return null;
        }
    }
    
    private void setDialogMessage(AlertDialog.Builder builder, JSONObject qrConentJSON)
    {
        if (qrConentJSON == null)
        {
            builder.setMessage(R.string.esp_qrcode_decode_failed);
            return;
        }
        
        try
        {
            String qrType = qrConentJSON.getString(QRImageHelper.KEY_QR_TYPE);
            if (qrType.equals(QRImageHelper.TYPE_SHARE_DEVICE_KEY))
            {
                builder.setMessage(R.string.esp_qrcode_capture_device);
            }
            else if (qrType.equals(QRImageHelper.TYPE_BUTTON_INFO))
            {
                builder.setMessage(R.string.esp_espbutton_qr_dialog_title);
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
            builder.setMessage(R.string.esp_qrcode_decode_failed);
        }
    }
    
    private class ShareDialogListener implements DialogInterface.OnClickListener
    {
        private Activity mActivity;
        private final JSONObject mSharedContent;
        
        public ShareDialogListener(Activity activity, JSONObject sharedContent)
        {
            mActivity = activity;
            mSharedContent = sharedContent;
        }
        
        @Override
        public void onClick(DialogInterface dialog, int which)
        {
            switch (which)
            {
                case DialogInterface.BUTTON_POSITIVE:
                    doPositive();
                    break;
                case DialogInterface.BUTTON_NEGATIVE:
                    finish();
                    break;
            }
            
        }

        private void doPositive()
        {
            if (mSharedContent == null)
            {
                finish();
                return;
            }
            
            try
            {
                String qrType = mSharedContent.getString(QRImageHelper.KEY_QR_TYPE);
                if (qrType.equals(QRImageHelper.TYPE_SHARE_DEVICE_KEY))
                {
                    String shareDeviceKey = mSharedContent.getString(QRImageHelper.KEY_SHARE_DEVICE_KEY);
                    new GetShareDeviceAsyncTask(mActivity).execute(shareDeviceKey);
                }
                else if (qrType.equals(QRImageHelper.TYPE_BUTTON_INFO))
                {
                    String buttonTempKey = mSharedContent.getString(QRImageHelper.KEY_BUTTON_TEMP_KEY);
                    String buttonMacAddress = mSharedContent.getString(QRImageHelper.KEY_BUTTON_MAC_ADDRESS);
                    Intent intent = new Intent(ShareCaptureActivity.this, EspButtonConfigureActivity.class);
                    intent.putExtra(EspStrings.Key.KEY_ESPBUTTON_TEMP_KEY, buttonTempKey);
                    intent.putExtra(EspStrings.Key.KEY_ESPBUTTON_MAC, buttonMacAddress);
                    startActivity(intent);
                    finish();
                }
            }
            catch (JSONException e)
            {
                e.printStackTrace();
            }
        }
    }
    
    private abstract class ProgressDialogAsyncTask extends AsyncTask<String, Object, Boolean>
    {
        protected Activity mActivity;
        
        protected ProgressDialog mProgressDialog;
        
        public ProgressDialogAsyncTask(Activity activity)
        {
            mActivity = activity;
        }
        
        @Override
        protected void onPreExecute()
        {
            mProgressDialog = new ProgressDialog(mActivity);
            mProgressDialog.setMessage(getString(R.string.esp_qrcode_capturing_message));
            mProgressDialog.setCanceledOnTouchOutside(false);
            mProgressDialog.show();
        }
        
        @Override
        protected void onPostExecute(Boolean result)
        {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }
    
    private class GetShareDeviceAsyncTask extends ProgressDialogAsyncTask
    {
        public GetShareDeviceAsyncTask(Activity activity)
        {
            super(activity);
        }
        
        @Override
        protected Boolean doInBackground(String... params)
        {
            String shareKey = params[0];
            log.debug("shareKey is : " + shareKey);
            IEspUser user = BEspUser.getBuilder().getInstance();
            return user.doActionActivateSharedDevice(shareKey);
        }
        
        @Override
        protected void onPostExecute(Boolean result)
        {
            super.onPostExecute(result);
            
            int toastMsg;
            if (result)
            {
                toastMsg = R.string.esp_qrcode_capture_result_success;
            }
            else
            {
                toastMsg = R.string.esp_qrcode_capture_result_failed;
            }
            Toast.makeText(mActivity, toastMsg, Toast.LENGTH_LONG).show();
            mActivity.setResult(RESULT_OK);
            mActivity.finish();
        }
    }
    
    private void initBeepSound()
    {
        if (playBeep && mediaPlayer == null)
        {
            // The volume on STREAM_SYSTEM is not adjustable, and users found it
            // too loud,
            // so we now play on the music stream.
            setVolumeControlStream(AudioManager.STREAM_MUSIC);
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setOnCompletionListener(beepListener);
            
            AssetFileDescriptor file = getResources().openRawResourceFd(R.raw.beep);
            try
            {
                mediaPlayer.setDataSource(file.getFileDescriptor(), file.getStartOffset(), file.getLength());
                file.close();
                mediaPlayer.setVolume(BEEP_VOLUME, BEEP_VOLUME);
                mediaPlayer.prepare();
            }
            catch (IOException e)
            {
                mediaPlayer = null;
            }
        }
    }
    
    private static final long VIBRATE_DURATION = 200L;
    
    private void playBeepSoundAndVibrate()
    {
        if (playBeep && mediaPlayer != null)
        {
            mediaPlayer.start();
        }
        if (vibrate)
        {
            Vibrator vibrator = (Vibrator)getSystemService(VIBRATOR_SERVICE);
            vibrator.vibrate(VIBRATE_DURATION);
        }
    }
    
    /**
     * When the beep has finished playing, rewind to queue up another one.
     */
    private final OnCompletionListener beepListener = new OnCompletionListener()
    {
        public void onCompletion(MediaPlayer mediaPlayer)
        {
            mediaPlayer.seekTo(0);
        }
    };
    
}