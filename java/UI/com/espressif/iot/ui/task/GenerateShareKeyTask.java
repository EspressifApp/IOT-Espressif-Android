package com.espressif.iot.ui.task;

import com.espressif.iot.R;
import com.espressif.iot.ui.widget.dialog.NoBgDialog;
import com.espressif.iot.user.IEspUser;
import com.espressif.iot.user.builder.BEspUser;
import com.google.zxing.qrcode.ui.QRImageHelper;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ImageView;

public class GenerateShareKeyTask extends AsyncTask<String, Void, String> {

    private Activity mActivity;
    private IEspUser mUser;

    private ProgressDialog mDialog;

    public GenerateShareKeyTask(Activity activity) {
        mActivity = activity;
        mUser = BEspUser.getBuilder().getInstance();
    }

    @Override
    protected void onPreExecute() {
        mDialog = new ProgressDialog(mActivity);
        mDialog.setMessage(mActivity.getString(R.string.esp_device_share_progress_message));
        mDialog.setCancelable(false);
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.show();
    }

    @Override
    protected String doInBackground(String... params) {
        String ownerDeviceKey = params[0];
        return mUser.doActionGenerateShareKey(ownerDeviceKey);
    }

    @Override
    protected void onPostExecute(String result) {
        if (result == null) {
            // Generate share key from server failed
            mDialog.setMessage(mActivity.getString(R.string.esp_device_share_result_failed));
            mDialog.setCancelable(true);
            mDialog.setCanceledOnTouchOutside(true);
        } else {
            // Generate share key from server success
            mDialog.dismiss();
            mDialog = null;
            showQRCodeDialog(result);
        }
    }

    private void showQRCodeDialog(String shareKey) {
        String qrUrl = QRImageHelper.createDeviceKeyUrl(shareKey);
        final ImageView qrImage = (ImageView)View.inflate(mActivity, R.layout.qr_code_image, null);
        final Bitmap qrBmp = QRImageHelper.createQRImage(qrUrl, mActivity);
        qrImage.setImageBitmap(qrBmp);

        AlertDialog dialog = new NoBgDialog(mActivity);
        dialog.setView(qrImage);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setOnDismissListener(new OnDismissListener() {

            @Override
            public void onDismiss(DialogInterface dialog) {
                qrImage.setImageBitmap(null);
                qrBmp.recycle();
            }
        });
        dialog.show();
    }
}
