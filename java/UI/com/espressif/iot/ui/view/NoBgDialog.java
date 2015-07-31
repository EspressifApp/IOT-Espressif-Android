package com.espressif.iot.ui.view;

import com.espressif.iot.R;

import android.app.AlertDialog;
import android.content.Context;

public class NoBgDialog extends AlertDialog
{
    public NoBgDialog(Context context)
    {
        super(context, R.style.TransparentBgDialog);
    }
}
