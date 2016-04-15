/*
 * Copyright (C) 2008 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.zxing.qrcode.decoding;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import com.google.zxing.qrcode.camera.CameraManager;
import com.google.zxing.qrcode.ui.ShareCaptureActivity;
import com.google.zxing.qrcode.view.ViewfinderResultPointCallback;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import java.lang.ref.WeakReference;
import java.util.Vector;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * This class handles all the messaging which comprises the state machine for capture.
 * 
 * @author dswitkin@google.com (Daniel Switkin)
 */
public final class CaptureActivityHandler extends Handler
{
    
    private static final Logger log = Logger.getLogger(CaptureActivityHandler.class);
    
    static
    {
        log.setLevel(Level.OFF);
    }
    
    private final WeakReference<ShareCaptureActivity> mActivity;
    
    private final DecodeThread decodeThread;
    
    private State state;
    
    private enum State
    {
        PREVIEW, SUCCESS, DONE
    }
    
    public CaptureActivityHandler(ShareCaptureActivity activity, Vector<BarcodeFormat> decodeFormats,
        String characterSet)
    {
        mActivity = new WeakReference<ShareCaptureActivity>(activity);
        decodeThread =
            new DecodeThread(activity, decodeFormats, characterSet, new ViewfinderResultPointCallback(
                activity.getViewfinderView()));
        decodeThread.start();
        state = State.SUCCESS;
        
        // Start ourselves capturing previews and decoding.
        CameraManager.get().startPreview();
        restartPreviewAndDecode(activity);
    }
    
    @Override
    public void handleMessage(Message message)
    {
        ShareCaptureActivity activity = mActivity.get();
        if (activity == null) {
            return;
        }

        switch (message.what)
        {
            case HandlerMsg.MSG_AUTO_FOCUS:
                // Logger.d(TAG, "Got auto-focus message");
                // When one auto focus pass finishes, start another. This is the
                // closest thing to
                // continuous AF. It does seem to hunt a bit, but I'm not sure what
                // else to do.
                if (state == State.PREVIEW)
                {
                    CameraManager.get().requestAutoFocus(this, HandlerMsg.MSG_AUTO_FOCUS);
                }
                break;
            case HandlerMsg.MSG_RESTART_PREVIEW:
                log.debug("Got restart preview message");
                restartPreviewAndDecode(activity);
                break;
            case HandlerMsg.MSG_DECODE_SUCCEEDED:
                log.debug("Got decode succeeded message");
                state = State.SUCCESS;
                Bundle bundle = message.getData();
                Bitmap barcode = bundle == null ? null : (Bitmap)bundle.getParcelable(DecodeThread.BARCODE_BITMAP);
                activity.handleDecode((Result)message.obj, barcode);
                break;
            case HandlerMsg.MSG_DECODE_FAILED:
                // We're decoding as fast as possible, so when one decode fails,
                // start another.
                state = State.PREVIEW;
                CameraManager.get().requestPreviewFrame(decodeThread.getHandler(), HandlerMsg.MSG_DECODE);
                break;
            case HandlerMsg.MSG_RETURN_SCAN_RESULT:
                log.debug("Got return scan result message");
                activity.setResult(Activity.RESULT_OK, (Intent)message.obj);
                activity.finish();
                break;
            case HandlerMsg.MSG_LAUNCH_PRODUCT_QUERY:
                log.debug("Got product query message");
                String url = (String)message.obj;
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                activity.startActivity(intent);
                break;
        }
    }
    
    public void quitSynchronously()
    {
        state = State.DONE;
        CameraManager.get().stopPreview();
        Message quit = Message.obtain(decodeThread.getHandler(), HandlerMsg.MSG_QUIT);
        quit.sendToTarget();
        try
        {
            decodeThread.join();
        }
        catch (InterruptedException e)
        {
            // continue
        }
        
        // Be absolutely sure we don't send any queued up messages
        removeMessages(HandlerMsg.MSG_DECODE_SUCCEEDED);
        removeMessages(HandlerMsg.MSG_DECODE_FAILED);
    }
    
    private void restartPreviewAndDecode(ShareCaptureActivity activity)
    {
        if (state == State.SUCCESS)
        {
            state = State.PREVIEW;
            CameraManager.get().requestPreviewFrame(decodeThread.getHandler(), HandlerMsg.MSG_DECODE);
            CameraManager.get().requestAutoFocus(this, HandlerMsg.MSG_AUTO_FOCUS);
            activity.drawViewfinder();
        }
    }
    
}
