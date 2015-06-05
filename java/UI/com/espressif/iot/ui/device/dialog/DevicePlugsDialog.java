package com.espressif.iot.ui.device.dialog;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.espressif.iot.R;
import com.espressif.iot.device.IEspDevice;
import com.espressif.iot.device.IEspDevicePlugs;
import com.espressif.iot.device.IEspDeviceSSS;
import com.espressif.iot.type.device.EspDeviceType;
import com.espressif.iot.type.device.EspPlugsAperture;
import com.espressif.iot.type.device.status.EspStatusPlugs;
import com.espressif.iot.type.device.status.IEspStatusPlugs;
import com.espressif.iot.type.device.status.IEspStatusPlugs.IAperture;

public class DevicePlugsDialog extends DeviceDialogAbs implements OnItemClickListener
{
    private ListView mApertureListView;
    private List<IAperture> mApertureList;
    private ApertureAdapter mApertureAdapter;
    private CheckBox mControlChildCB;
    
    public DevicePlugsDialog(Context context, IEspDevice device)
    {
        super(context, device);
    }
    
    @Override
    public void cancel()
    {
        if (mDialog != null)
        {
            mDialog.cancel();
        }
    }
    
    @Override
    public void dismiss()
    {
        if (mDialog != null)
        {
            mDialog.dismiss();
        }
    }
    
    @Override
    protected View getContentView(LayoutInflater inflater)
    {
        View view = inflater.inflate(R.layout.device_activity_plugs, null);
        
        mControlChildCB = (CheckBox)view.findViewById(R.id.control_child_cb);
        mControlChildCB.setVisibility(mDevice.getIsMeshDevice() ? View.VISIBLE : View.GONE);
        mControlChildCB = (CheckBox)view.findViewById(R.id.control_child_cb);
        if (mDevice.getDeviceType() == EspDeviceType.ROOT)
        {
            mControlChildCB.setChecked(true);
            mControlChildCB.setVisibility(View.GONE);
        }
        
        mApertureListView = (ListView)view.findViewById(R.id.aperture_list);
        IEspStatusPlugs status;
        if (mDevice instanceof IEspDeviceSSS)
        {
            status = (IEspStatusPlugs)((IEspDeviceSSS)mDevice).getDeviceStatus();
        }
        else
        {
            status = ((IEspDevicePlugs)mDevice).getStatusPlugs();
        }
        mApertureList = status.getStatusApertureList();
        mApertureAdapter = new ApertureAdapter();
        mApertureListView.setAdapter(mApertureAdapter);
        mApertureListView.setOnItemClickListener(this);
        
        return view;
    }
    
    @Override
    protected void onExecuteEnd(boolean suc)
    {
        mApertureAdapter.notifyDataSetChanged();
    }
    
    private class ApertureAdapter extends BaseAdapter
    {
        private class ViewHolder
        {
            ImageView icon;
            
            TextView title;
            
            TextView notes;
            
            ImageView status;
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
                LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.device_plugs_aperture, parent, false);
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
        
        new StatusTask(mControlChildCB.isChecked()).execute(status);
    }
}
