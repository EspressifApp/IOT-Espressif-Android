package com.espressif.iot.base.time;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class TimeBroadcastReceiver extends BroadcastReceiver
{
    
    @Override
    public void onReceive(Context context, Intent intent)
    {
        String action = intent.getAction();
        if (action.equals(Intent.ACTION_TIME_CHANGED))
        {
            UITimeManager.getInstance().setIsTimeValid(false);
        }
        else if (action.equals(Intent.ACTION_DATE_CHANGED))
        {
            UITimeManager.getInstance().setIsTimeValid(false);
        }
    }
}
