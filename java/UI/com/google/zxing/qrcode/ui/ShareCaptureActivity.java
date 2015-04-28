package com.google.zxing.qrcode.ui;

import java.io.IOException;
import java.util.Vector;

import org.apache.log4j.Logger;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
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
import com.espressif.iot.user.IEspUser;
import com.espressif.iot.user.builder.BEspUser;
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
        if (barcode != null)
        {
            Drawable drawable = new BitmapDrawable(getResources(), barcode);
            dialog.setIcon(drawable);
        }
        dialog.setTitle(R.string.esp_qrcode_capture_device);
        ShareDialogListener clickListener = new ShareDialogListener(obj.getText());
        dialog.setNegativeButton(android.R.string.cancel, clickListener);
        dialog.setPositiveButton(android.R.string.ok, clickListener);
        dialog.show();
    }
    
    private class ShareDialogListener implements DialogInterface.OnClickListener
    {
        private final String mShareKey;
        
        public ShareDialogListener(String shareKey)
        {
            mShareKey = shareKey;
        }
        
        @Override
        public void onClick(DialogInterface dialog, int which)
        {
            switch (which)
            {
                case DialogInterface.BUTTON_POSITIVE:
                    new GetShareAsyncTask().execute(mShareKey);
                    break;
                case DialogInterface.BUTTON_NEGATIVE:
                    finish();
                    break;
            }
            
        }
    }
    
    private class GetShareAsyncTask extends AsyncTask<String, Object, Boolean>
    {
        
        private ProgressDialog mProgressDialog;
        
        @Override
        protected void onPreExecute()
        {
            mProgressDialog = new ProgressDialog(ShareCaptureActivity.this);
            mProgressDialog.setMessage(getString(R.string.esp_qrcode_capturing_message));
            mProgressDialog.setCanceledOnTouchOutside(false);
            mProgressDialog.show();
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
            mProgressDialog.dismiss();
            mProgressDialog = null;
            Activity activity = ShareCaptureActivity.this;
            int toastMsg;
            if (result)
            {
                toastMsg = R.string.esp_qrcode_capture_result_success;
            }
            else
            {
                toastMsg = R.string.esp_qrcode_capture_result_failed;
            }
            Toast.makeText(activity, toastMsg, Toast.LENGTH_LONG).show();
            activity.finish();
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