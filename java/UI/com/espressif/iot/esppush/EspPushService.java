package com.espressif.iot.esppush;

import java.util.List;

import com.espressif.iot.R;
import com.espressif.iot.base.application.EspApplication;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;

public class EspPushService extends Service
{
    /**
     * 
     * @param context
     * @return whether EspPushService is alive background
     */
    public static boolean isAlive(Context context)
    {
        ActivityManager am = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningServiceInfo> list = am.getRunningServices(Integer.MAX_VALUE);
        boolean isServiceRunning = false;
        String clsName = "com.espressif.iot.esppush.EspPushService";
        for (RunningServiceInfo info : list) {
            String serviceCls = info.service.getClassName();
            if (serviceCls.equalsIgnoreCase(clsName)) {
                isServiceRunning = true;
                break;
            }
        }
        return isServiceRunning;
    }
    
    /**
     * Start EspPushService
     * 
     * @param context
     */
    public static void start(Context context)
    {
        Intent intent = new Intent(context, EspPushService.class);
        context.startService(intent);
    }
    
    /**
     * Stop EspPushService
     * 
     * @param context
     */
    public static void stop(Context context)
    {
        Intent intent = new Intent(context, EspPushService.class);
        context.stopService(intent);
    }
    
    private EspPushClient mPushClient;
    
    private AlarmManager mAlarmManager;
    private static final long HEART_BEAT_INTERVAL = 60000; // one minute
    private static final String ACTION_HEART_BEAT = "esppush_action_heart_beating";
    private PendingIntent mHeartBeatIntent;
    
    private ConnectivityManager mConnectivityManager;
    
    private ServiceCast mReceiver;
    
    private NotificationManager mNotificationManager;
    private int mNotificationId;
    
    @Override
    public void onCreate()
    {
        super.onCreate();
        
        mPushClient = new EspPushClient(this);
        
        mConnectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        
        mNotificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationId = 1;
        
        mReceiver = new ServiceCast();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        filter.addAction(EspPushClient.ESPPUSH_ACTION_RECEIVE_MESSAGE);
        filter.addAction(ACTION_HEART_BEAT);
        registerReceiver(mReceiver, filter);
        
        mAlarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        mHeartBeatIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_HEART_BEAT), 0);
        mAlarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime()
            + HEART_BEAT_INTERVAL, HEART_BEAT_INTERVAL, mHeartBeatIntent);
    }
    
    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }
    
    @Override
    public void onDestroy()
    {
        super.onDestroy();
        
        mAlarmManager.cancel(mHeartBeatIntent);
        unregisterReceiver(mReceiver);
        mPushClient.disconnect();
    }
    
    private class ServiceCast extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            final String action = intent.getAction();
            if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION))
            {
                // Network changed
                if (isNetworkAvailable())
                {
                    if (!mPushClient.isConnteted())
                    {
                        mPushClient.connect();
                    }
                }
                else
                {
                    mPushClient.disconnect();
                }
            }
            else if (action.equals(EspPushClient.ESPPUSH_ACTION_RECEIVE_MESSAGE))
            {
                // Receive new message from server
                String notificationMsg = intent.getStringExtra(EspPushClient.ESPPUSH_KEY_MESSAGE);
                notification(notificationMsg);
            }
            else if (action.equals(ACTION_HEART_BEAT))
            {
                // Alarm heart beating
                if (mPushClient.isConnteted())
                {
                    mPushClient.ping();
                }
                else if (isNetworkAvailable())
                {
                    mPushClient.connect();
                }
            }
        }
        
    }
    
    private boolean isNetworkAvailable()
    {
        NetworkInfo info = mConnectivityManager.getActiveNetworkInfo();
        return (info != null && info.isAvailable());
    }
    
    private void notification(String message)
    {
        Intent intent = new Intent(this, EspApplication.getEspUIActivity());
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
        Notification notification =
            new NotificationCompat.Builder(this).setContentTitle(getString(R.string.app_name))
                .setContentText(message)
                .setSmallIcon(R.drawable.app_icon)
                .setDefaults(Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .build();
        mNotificationManager.notify(mNotificationId++, notification);
    }
}
