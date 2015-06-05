package com.espressif.iot.ui.device.dialog;

import android.content.DialogInterface;

public interface EspDeviceDialogInterface extends DialogInterface
{
    void show();
    
    public interface OnDissmissedListener
    {
        void onDissmissed(DialogInterface dialog);
    }
    
    void setOnDissmissedListener(OnDissmissedListener listener);
}
