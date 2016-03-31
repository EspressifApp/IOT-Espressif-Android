package com.espressif.iot.ui.device;

import java.util.ArrayList;
import java.util.List;

import com.espressif.iot.R;
import com.espressif.iot.device.IEspDevicePlugs;
import com.espressif.iot.type.device.EspPlugsAperture;
import com.espressif.iot.type.device.status.EspStatusPlugs;
import com.espressif.iot.type.device.status.IEspStatusPlugs;
import com.espressif.iot.type.device.status.IEspStatusPlugs.IAperture;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class DevicePlugsActivity extends DeviceActivityAbs implements OnItemClickListener
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
        if (compatibility)
        {
            executeGet();
        }
    }
    
    @Override
    protected View initControlView()
    {
        View view = View.inflate(this, R.layout.device_activity_plugs, null);
        
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
}
