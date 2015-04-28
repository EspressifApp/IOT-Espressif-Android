package com.espressif.iot.ui.device;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import com.espressif.iot.R;
import com.espressif.iot.device.IEspDevicePlugs;
import com.espressif.iot.help.ui.IEspHelpUIUsePlugs;
import com.espressif.iot.type.device.EspPlugsAperture;
import com.espressif.iot.type.device.status.EspStatusPlugs;
import com.espressif.iot.type.device.status.IEspStatusPlugs;
import com.espressif.iot.type.device.status.IEspStatusPlugs.IAperture;
import com.espressif.iot.type.help.HelpStepUsePlug;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class DevicePlugsActivity extends DeviceActivityAbs implements OnItemClickListener, IEspHelpUIUsePlugs
{
    private IEspDevicePlugs mDevicePlugs;
    
    private ListView mApertureListView;
    private ApertureAdapter mApertureAdapter;
    private List<IAperture> mApertureList;
    
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        boolean compatibility = isDeviceCompatibility();
        if (mHelpMachine.isHelpModeUsePlugs())
        {
            mHelpMachine.transformState(compatibility);
            HelpHandler helpHandler = new HelpHandler(this);
            helpHandler.sendEmptyMessageDelayed(0, 100);
        }
        if (compatibility)
        {
            executeGet();
        }
    }
    
    @Override
    protected View initControlView()
    {
        View view = getLayoutInflater().inflate(R.layout.device_activity_plugs, null);
        
        mDevicePlugs = (IEspDevicePlugs)mIEspDevice;
        mApertureList = mDevicePlugs.getApertureList();
        mApertureListView = (ListView)view.findViewById(R.id.aperture_list);
        mApertureAdapter = new ApertureAdapter(this);
        mApertureListView.setAdapter(mApertureAdapter);
        mApertureListView.setOnItemClickListener(this);
        
        return view;
    }
    
    @Override
    protected void executePrepare()
    {
    }
    
    @Override
    protected void executeFinish(int command, boolean result)
    {
        mApertureAdapter.notifyDataSetChanged();
        
        if (command == COMMAND_GET && !result)
        {
            Toast.makeText(this, R.string.esp_device_plugs_get_status_failed, Toast.LENGTH_LONG).show();
        }
        
        if (mHelpMachine.isHelpModeUsePlugs() && command == COMMAND_POST)
        {
            mHelpMachine.transformState(true);
            onHelpUsePlugs();
        }
    }
    
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
    {
        boolean isOn = !mApertureList.get(position).isOn();
        
        IEspStatusPlugs status = new EspStatusPlugs();
        List<IAperture> statusApertureList = new ArrayList<IAperture>();
        for (int i = 0; i < mApertureList.size(); i++)
        {
            IAperture aperture = mApertureList.get(i);
            IAperture statusAperture = new EspPlugsAperture(aperture.getId());
            statusAperture.setOn(i == position ? isOn : aperture.isOn());
            
            statusApertureList.add(statusAperture);
        }
        status.setStatusApertureList(statusApertureList);
        
        executePost(status);
    }
    
    private class ViewHolder
    {
        ImageView icon;
        
        TextView title;
        
        TextView notes;
        
        ImageView status;
    }
    
    private class ApertureAdapter extends BaseAdapter
    {
        private Activity mActivity;
        
        public ApertureAdapter(Activity activity)
        {
            mActivity = activity;
        }
        
        @Override
        public int getCount()
        {
            return mApertureList.size();
        }
        
        @Override
        public IAperture getItem(int position)
        {
            return mApertureList.get(position);
        }
        
        @Override
        public long getItemId(int position)
        {
            return getItem(position).getId();
        }
        
        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            View view;
            ViewHolder holder;
            if (convertView == null)
            {
                view = mActivity.getLayoutInflater().inflate(R.layout.device_plugs_aperture, parent, false);
                holder = new ViewHolder();
                holder.icon = (ImageView)view.findViewById(R.id.aperture_icon);
                holder.title = (TextView)view.findViewById(R.id.aperture_title);
                holder.notes = (TextView)view.findViewById(R.id.aperture_notes);
                holder.status = (ImageView)view.findViewById(R.id.aperture_status);
                view.setTag(holder);
            }
            else
            {
                view = convertView;
                holder = (ViewHolder)view.getTag();
            }
            
            IAperture aperture = getItem(position);
            holder.icon.setBackgroundResource(R.drawable.esp_icon_plugs_aperture);
            holder.title.setText(aperture.getTitle());
            int statusIcon = aperture.isOn() ? R.drawable.esp_plug_small_on : R.drawable.esp_plug_small_off;
            holder.status.setBackgroundResource(statusIcon);
            holder.notes.setVisibility(View.GONE);
            
            return view;
        }
        
    }
    
    @Override
    public void onHelpUsePlugs()
    {
        clearHelpContainer();
        
        HelpStepUsePlug step = HelpStepUsePlug.valueOf(mHelpMachine.getCurrentStateOrdinal());
        switch (step)
        {
            case START_USE_HELP:
            case FAIL_FOUND_PLUG:
            case FIND_ONLINE:
            case NO_PLUG_ONLINE:
            case PLUG_SELECT:
                break;
            
            case PLUG_NOT_COMPATIBILITY:
                mHelpMachine.exit();
                setResult(RESULT_EXIT_HELP_MODE);
                break;
            case PLUG_CONTROL:
                highlightHelpView(mApertureListView);
                setHelpHintMessage(R.string.esp_help_use_plugs_tap_icon_msg);
                break;
            case PLUG_CONTROL_FAILED:
                highlightHelpView(mApertureListView);
                setHelpHintMessage(R.string.esp_help_use_plugs_control_failed_msg);
                mHelpMachine.retry();
                break;
            case SUC:
                setHelpFrameDark();
                setHelpHintMessage(R.string.esp_help_use_plugs_success_msg);
                setHelpButtonVisible(HELP_BUTTON_EXIT, true);
                break;
        }
    }
    
    private static class HelpHandler extends Handler
    {
        private WeakReference<DevicePlugsActivity> mActivity;
        
        public HelpHandler(DevicePlugsActivity activity)
        {
            mActivity = new WeakReference<DevicePlugsActivity>(activity);
        }
        
        @Override
        public void handleMessage(Message msg)
        {
            DevicePlugsActivity activity = mActivity.get();
            if (activity == null)
            {
                return;
            }
            
            activity.onHelpUsePlugs();
        }
    }
}
