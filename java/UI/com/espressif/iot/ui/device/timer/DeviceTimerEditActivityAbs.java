package com.espressif.iot.ui.device.timer;

import org.json.JSONObject;

import com.espressif.iot.R;
import com.espressif.iot.device.IEspDevice;
import com.espressif.iot.type.device.timer.EspDeviceTimer;
import com.espressif.iot.type.device.timer.EspDeviceTimerJSONKey;
import com.espressif.iot.user.IEspUser;
import com.espressif.iot.user.builder.BEspUser;
import com.espressif.iot.util.EspDefaults;
import com.espressif.iot.util.EspStrings;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public abstract class DeviceTimerEditActivityAbs extends Activity implements EspDeviceTimerJSONKey
{
    protected IEspDevice mDevice;
    
    protected IEspUser mUser;
    
    /**
     * The timer need edit. If null, create a new timer.
     */
    protected EspDeviceTimer mTimer;
    
    protected String[] mActionTitles;
    protected String[] mActionValues;
    
    /**
     * Get the JSONObject which need Post
     * 
     * @return
     */
    abstract protected JSONObject getPostJSON();
    
    /**
     * If edit exist timer, set the data on ContentView;
     */
    abstract protected void setGotData();
    
    protected final static int MENU_ID_SAVE = 0;
    
    protected Bundle mIntentBundle;
    
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        mUser = BEspUser.getBuilder().getInstance();
        
        Intent intent = getIntent();
        String deviceKey = intent.getStringExtra(EspStrings.Key.DEVICE_KEY_KEY);
        mDevice = mUser.getUserDevice(deviceKey);
        long timerId = intent.getLongExtra(EspStrings.Key.DEVICE_TIMER_ID_KEY, EspDefaults.DEVICE_TIMER_ID);
        if (timerId >= 0)
        {
            for (int i = 0; i < mDevice.getTimerList().size(); i++)
            {
                EspDeviceTimer timer = mDevice.getTimerList().get(i);
                if (timer.getId() == timerId)
                {
                    mTimer = timer;
                    break;
                }
            }
        }
        
        mIntentBundle = intent.getBundleExtra(EspStrings.Key.DEVICE_TIMER_BUNDLE_KEY);
        
        mActionTitles = getActionTitles();
        mActionValues = getActionValues();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        menu.add(Menu.NONE, MENU_ID_SAVE, 0, R.string.esp_device_timer_menu_save)
            .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return super.onCreateOptionsMenu(menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case MENU_ID_SAVE:
                JSONObject json = getPostJSON();
                if (json != null) {
                    new SaveTask().execute(json);
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    /**
     * if the time is less than 10, add head '0'. EX: 8 -> 08
     * 
     * @param time
     * @return
     */
    protected String matchingTimeStr(int time)
    {
        String str = time + "";
        if (str.length() < 2)
        {
            str = "0" + str;
        }
        return str;
    }
    
    /**
     * Post the new edited timer to server
     */
    private class SaveTask extends AsyncTask<JSONObject, Void, Boolean>
    {
        
        private ProgressDialog mDialog;
        
        @Override
        protected void onPreExecute()
        {
            mDialog = new ProgressDialog(DeviceTimerEditActivityAbs.this);
            mDialog.setMessage(getString(R.string.esp_device_task_dialog_message));
            mDialog.setCancelable(false);
            mDialog.show();
        }
        
        @Override
        protected Boolean doInBackground(JSONObject... params)
        {
            JSONObject timerJSON = params[0];
            return mUser.doActionDeviceTimerPost(mDevice, timerJSON);
        }
        
        protected void onPostExecute(Boolean result)
        {
            mDialog.dismiss();
            mDialog = null;
            
            Activity activity = DeviceTimerEditActivityAbs.this;
            if (result)
            {
                Toast.makeText(activity, R.string.esp_device_timer_save_result_success, Toast.LENGTH_LONG).show();
                setResult(RESULT_OK);
                finish();
            }
            else
            {
                Toast.makeText(activity, R.string.esp_device_timer_save_result_failed, Toast.LENGTH_LONG).show();
            }
        }
    }
    
    private String[] getActionTitles()
    {
        switch (mDevice.getDeviceType())
        {
            case PLUG:
                return getResources().getStringArray(R.array.esp_device_plug_timer_actions);
            case PLUGS:
                return getResources().getStringArray(R.array.esp_device_plugs_timer_actions);
            default:
                return null;
        }
    }
    
    private String[] getActionValues()
    {
        switch (mDevice.getDeviceType())
        {
            case PLUG:
                return getResources().getStringArray(R.array.esp_device_plug_timer_action_values);
            case PLUGS:
                return getResources().getStringArray(R.array.esp_device_plugs_timer_action_values);
            default:
                return null;
        }
    }
}
