package com.espressif.iot.ui.device.timer;

import java.util.List;

import org.apache.log4j.Logger;

import com.espressif.iot.R;
import com.espressif.iot.device.IEspDevice;
import com.espressif.iot.device.IEspDevicePlugs;
import com.espressif.iot.type.device.status.IEspStatusPlugs.IAperture;
import com.espressif.iot.type.device.timer.EspDeviceTimer;
import com.espressif.iot.user.IEspUser;
import com.espressif.iot.user.builder.BEspUser;
import com.espressif.iot.util.EspStrings;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class DeviceTimersActivity extends Activity implements OnItemClickListener, OnItemLongClickListener
{
    private final Logger log = Logger.getLogger(DeviceTimersActivity.class);
    
    private IEspDevice mDevice;
    
    private IEspUser mUser;
    
    private ListView mTimerListView;
    
    private List<EspDeviceTimer> mTimerList;
    
    private TimerAdapter mTimerAdapter;
    
    private final static int MENU_ID_ADD = 0;
    
    private final static int REQUEST_TIMER_EDIT = 0;
    
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        Intent intent = getIntent();
        String deviceKey = intent.getStringExtra(EspStrings.Key.DEVICE_KEY_KEY);
        
        mUser = BEspUser.getBuilder().getInstance();
        mDevice = mUser.getUserDevice(deviceKey);
        
        setContentView(R.layout.device_timers_activity);
        
        mTimerList = mDevice.getTimerList();
        mTimerListView = (ListView)findViewById(R.id.timers_list);
        mTimerAdapter = new TimerAdapter(this);
        mTimerListView.setAdapter(mTimerAdapter);
        mTimerListView.setOnItemClickListener(this);
        mTimerListView.setOnItemLongClickListener(this);
        
        executeGet();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Add new timer menu
        menu.add(Menu.NONE, MENU_ID_ADD, 0, R.string.esp_device_timer_menu_add)
            .setIcon(R.drawable.esp_menu_icon_add)
            .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        
        return super.onCreateOptionsMenu(menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case MENU_ID_ADD:
                switch(mDevice.getDeviceType())
                {
                    case PLUG:
                        showSelcetTimerTypeDialog();
                        return true;
                    case PLUGS:
                        showSelcetPlugsApertrueDialog();
                        return true;
                    default:
                        break;
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
    {
        EspDeviceTimer timer = mTimerList.get(position);
        Intent intent = getTimerEditIntent(timer.getTimerType(), timer.getId());
        switch (mDevice.getDeviceType())
        {
            case PLUGS:
                String action = timer.getTimerActions().get(0).getAction();
                String value = action.substring(action.length() - IEspDevicePlugs.TIMER_TAIL_LENGTH, action.length());
                Bundle bundle = new Bundle();
                bundle.putString(EspStrings.Key.DEVICE_TIMER_PLUGS_VALUE_KEY, value);
                intent.putExtra(EspStrings.Key.DEVICE_TIMER_BUNDLE_KEY, bundle);
                break;
            default:
                break;
        }
        
        startActivityForResult(intent, REQUEST_TIMER_EDIT);
    }
    
    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id)
    {
        final EspDeviceTimer timer = mTimerList.get(position);
        new AlertDialog.Builder(this).setItems(R.array.esp_device_timer_oprate_options,
            new DialogInterface.OnClickListener()
            {
                
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    final int item_delete = 0;
                    switch (which)
                    {
                        case item_delete:
                            executeDelete(timer);
                            break;
                    }
                }
            }).show();
        return true;
    }
    
    private void showSelcetPlugsApertrueDialog()
    {
        final IEspDevicePlugs plugs = (IEspDevicePlugs)mDevice;
        final List<IAperture> apertures = plugs.getApertureList();
        final String[] titles = new String[apertures.size()];
        final boolean[] selects = new boolean[apertures.size()];
        for (int i = 0; i < apertures.size(); i++)
        {
            IAperture aperture = apertures.get(i);
            titles[i] = aperture.getTitle();
            selects[i] = false;
        }
        
        new AlertDialog.Builder(this).setTitle(plugs.getName())
            .setMultiChoiceItems(titles, selects, new DialogInterface.OnMultiChoiceClickListener()
            {
                
                @Override
                public void onClick(DialogInterface dialog, int which, boolean isChecked)
                {
                }
            })
            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener()
            {
                
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    int valueSum = 0;
                    for (int i = 0; i < selects.length; i++)
                    {
                        if (selects[i])
                        {
                            valueSum += (1 << i);
                        }
                    }
                    
                    if (valueSum > 0)
                    {
                        String value = toHexString(valueSum, IEspDevicePlugs.TIMER_TAIL_LENGTH);
                        showSelcetTimerTypeDialog(value);
                    }
                }
            })
            .show();
    }
    
    private void showSelcetTimerTypeDialog()
    {
        showSelcetTimerTypeDialog(null);
    }
    
    private void showSelcetTimerTypeDialog(String value)
    {
        AddTimerListener listener;
        if (TextUtils.isEmpty(value))
        {
            listener = new AddTimerListener();
        }
        else
        {
            listener = new AddTimerListener(value);
        }
        new AlertDialog.Builder(DeviceTimersActivity.this).setItems(R.array.esp_device_timer_type, listener).show();
    }
    
    private final int ITEM_TIMER_FIXEDTIME = 0;
    private final int ITEM_TIMER_LOOP_PERIOD = 1;
    private final int ITEM_TIMER_LOOP_IN_WEED = 2;
    
    private class AddTimerListener implements DialogInterface.OnClickListener
    {
        private String mValue;
        
        public AddTimerListener()
        {
        }
        
        public AddTimerListener(String value)
        {
            mValue = value;
        }
        
        @Override
        public void onClick(DialogInterface dialog, int which)
        {
            Activity activity = DeviceTimersActivity.this;
            Intent intent = getTimerCreateIntent(which);
            if (!TextUtils.isEmpty(mValue))
            {
                Bundle bundle = new Bundle();
                bundle.putString(EspStrings.Key.DEVICE_TIMER_PLUGS_VALUE_KEY, mValue);
                intent.putExtra(EspStrings.Key.DEVICE_TIMER_BUNDLE_KEY, bundle);
            }
            activity.startActivityForResult(intent, REQUEST_TIMER_EDIT);
        }
        
    }
    
    private Intent getTimerCreateIntent(int timerType)
    {
        Class<?> cls;
        switch (timerType)
        {
            case ITEM_TIMER_FIXEDTIME:
                
                switch(mDevice.getDeviceType())
                {
                    case PLUG:
                        cls = DevicePlugFixedTimeTimerEditActivity.class;
                        break;
                    case PLUGS:
                        cls = DevicePlugsFixedTimeTimerEditActivity.class;
                        break;
                    default:
                        cls = null;
                        break;
                }
                break;
            case ITEM_TIMER_LOOP_PERIOD:
                switch(mDevice.getDeviceType())
                {
                    case PLUG:
                        cls = DevicePlugLoopPeriodTimerEditActivity.class;
                        break;
                    case PLUGS:
                        cls = DevicePlugsLoopPeriodTimerEditActivity.class;
                        break;
                    default:
                        cls = null;
                        break;
                }
                break;
            case ITEM_TIMER_LOOP_IN_WEED:
                switch(mDevice.getDeviceType())
                {
                    case PLUG:
                        cls = DevicePlugLoopInWeekTimerEditActivity.class;
                        break;
                    case PLUGS:
                        cls = DevicePlugsLoopInWeekTimerEditActivity.class;
                        break;
                    default:
                        cls = null;
                        break;
                }
                break;
            default:
                cls = null;
                break;
        }
        Intent intent = new Intent(this, cls);
        intent.putExtra(EspStrings.Key.DEVICE_KEY_KEY, mDevice.getKey());
        return intent;
    }
    
    private Intent getTimerCreateIntent(String timerType)
    {
        int type = -1;
        if (timerType.equals(EspDeviceTimer.TIMER_TYPE_FIXEDTIME))
        {
            type = ITEM_TIMER_FIXEDTIME;
        }
        else if (timerType.equals(EspDeviceTimer.TIMER_TYPE_LOOP_IN_WEEK))
        {
            type = ITEM_TIMER_LOOP_IN_WEED;
        }
        else if (timerType.equals(EspDeviceTimer.TIMER_TYPE_LOOP_PERIOD))
        {
            type = ITEM_TIMER_LOOP_PERIOD;
        }
        
        return getTimerCreateIntent(type);
    }
    
    private Intent getTimerEditIntent(String timerType, long timerId)
    {
        Intent intent = getTimerCreateIntent(timerType);
        intent.putExtra(EspStrings.Key.DEVICE_TIMER_ID_KEY, timerId);
        return intent;
    }
    
    private class TimerAdapter extends BaseAdapter
    {
        private LayoutInflater mInflater;
        
        public TimerAdapter(Activity activity)
        {
            mInflater = activity.getLayoutInflater();
        }
        
        @Override
        public int getCount()
        {
            return mTimerList.size();
        }
        
        @Override
        public EspDeviceTimer getItem(int position)
        {
            return mTimerList.get(position);
        }
        
        @Override
        public long getItemId(int position)
        {
            return mTimerList.get(position).getId();
        }
        
        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            if (convertView == null)
            {
                convertView = mInflater.inflate(android.R.layout.simple_list_item_1, parent, false);
            }
            
            TextView textView = (TextView)convertView.findViewById(android.R.id.text1);
            textView.setText(mTimerList.get(position).toString());
            return convertView;
        }
        
    }
    
    private class GetTask extends AsyncTask<Void, Void, Boolean>
    {
        private ProgressDialog mDialog;
        
        @Override
        protected void onPreExecute()
        {
            mDialog = new ProgressDialog(DeviceTimersActivity.this);
            mDialog.setMessage(getString(R.string.esp_device_task_dialog_message));
            mDialog.setCancelable(false);
            mDialog.show();
        }
        
        @Override
        protected Boolean doInBackground(Void... params)
        {
            return mUser.doActionDeviceTimerGet(mDevice);
        }
        
        protected void onPostExecute(Boolean result)
        {
            mDialog.dismiss();
            mDialog = null;
            
            log.debug("Get Device Timers result = " + result);
            if (result)
            {
                mTimerAdapter.notifyDataSetChanged();
            }
        };
    }
    
    private class DeleteTask extends AsyncTask<Long, Void, Boolean>
    {
        private EspDeviceTimer mTimer;
        
        private ProgressDialog mDialog;
        
        public DeleteTask(EspDeviceTimer timer)
        {
            mTimer = timer;
        }
        
        @Override
        protected void onPreExecute()
        {
            mDialog = new ProgressDialog(DeviceTimersActivity.this);
            mDialog.setMessage(getString(R.string.esp_device_task_dialog_message));
            mDialog.setCancelable(false);
            mDialog.show();
        }
        
        @Override
        protected Boolean doInBackground(Long... params)
        {
            return mUser.doActionDeviceTimerDelete(mDevice, mTimer.getId());
        }
        
        @Override
        protected void onPostExecute(Boolean result)
        {
            mDialog.dismiss();
            mDialog = null;
            
            log.debug("Delete Device Timer result = " + result);
            Activity activity = DeviceTimersActivity.this;
            if (result)
            {
                mTimerList.remove(mTimer);
                mTimerAdapter.notifyDataSetChanged();
                Toast.makeText(activity, R.string.esp_device_timer_delete_result_success, Toast.LENGTH_LONG).show();
            }
            else
            {
                Toast.makeText(activity, R.string.esp_device_timer_delete_result_failed, Toast.LENGTH_LONG).show();
            }
        }
    }
    
    private void executeGet()
    {
        new GetTask().execute();
    }
    
    private void executeDelete(EspDeviceTimer timer)
    {
        new DeleteTask(timer).execute();
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == REQUEST_TIMER_EDIT)
        {
            if (resultCode == RESULT_OK)
            {
                executeGet();
            }
        }
    }
    
    protected String toHexString(int value, int minLength)
    {
        StringBuilder result = new StringBuilder(Integer.toHexString(value));
        while (result.length() < minLength)
        {
            result.insert(0, 0);
        }
        
        return result.toString();
    }
}
