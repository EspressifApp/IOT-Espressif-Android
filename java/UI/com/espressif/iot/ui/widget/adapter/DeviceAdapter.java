package com.espressif.iot.ui.widget.adapter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.espressif.iot.R;
import com.espressif.iot.device.IEspDevice;
import com.espressif.iot.type.device.IEspDeviceState;
import com.espressif.iot.util.DeviceUtil;

public class DeviceAdapter extends BaseAdapter
{
    private LayoutInflater mInflater;
    
    private List<IEspDevice> mDeviceList;
    private Set<IEspDevice> mEditCheckedDevices;
    
    private boolean mEditable = false;
    
    private OnEditCheckedChangeListener mEditCheckedChangeListener;
    
    public interface OnEditCheckedChangeListener
    {
        void onEditCheckedChanged(CheckBox checkBox, IEspDevice device, boolean isChecked);
    }
    
    public DeviceAdapter(Activity activity, List<IEspDevice> list)
    {
        mInflater = activity.getLayoutInflater();
        
        mDeviceList = list;
        mEditCheckedDevices = new HashSet<IEspDevice>();
    }
    
    @Override
    public int getCount()
    {
        return mDeviceList.size();
    }
    
    @Override
    public Object getItem(int position)
    {
        return mDeviceList.get(position);
    }
    
    @Override
    public long getItemId(int position)
    {
        return mDeviceList.get(position).getId();
    }
    
    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        final IEspDevice device = mDeviceList.get(position);
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.device_layout, parent, false);
        }
        convertView.setTag(device);

        // Set icon
        ImageView iconIV = (ImageView)convertView.findViewById(R.id.device_icon);
        iconIV.setBackgroundResource(DeviceUtil.getDeviceIconRes(device.getDeviceType()));

        // Set device name
        TextView nameTV = (TextView)convertView.findViewById(R.id.device_name);
        nameTV.setText(device.getName());

        // Set state
        IEspDeviceState state = device.getDeviceState();
        TextView statusTV = (TextView)convertView.findViewById(R.id.device_status_text);
        switch (state.getDeviceState()) {
            case UPGRADING_LOCAL:
                statusTV.setText(R.string.esp_ui_status_upgrading_local);
                break;
            case UPGRADING_INTERNET:
                statusTV.setText(R.string.esp_ui_status_upgrading_online);
                break;
            case OFFLINE:
                statusTV.setText(R.string.esp_ui_status_offline);
                break;
            case NEW:
            case ACTIVATING:
                statusTV.setText(R.string.esp_ui_status_activating);
                break;
            case LOCAL:
                statusTV.setText(R.string.esp_ui_status_local);
                break;
            case INTERNET:
                statusTV.setText(R.string.esp_ui_status_online);
                break;

            case CLEAR:
            case CONFIGURING:
            case DELETED:
            case RENAMED:
                // shouldn't goto here
                statusTV.setText(state.getDeviceState().toString());
                break;
        }
        statusTV.append(" | " + device.getDeviceType());

        ImageView localStatusIV = (ImageView)convertView.findViewById(R.id.device_status1);
        localStatusIV.setBackgroundResource(R.drawable.esp_device_status_local);
        localStatusIV.setVisibility(state.isStateLocal() ? View.VISIBLE : View.GONE);

        ImageView cloudStatusIV = (ImageView)convertView.findViewById(R.id.device_status2);
        cloudStatusIV.setVisibility(device.isActivated() ? View.VISIBLE : View.GONE);
        if (state.isStateInternet()) {
            cloudStatusIV.setBackgroundResource(R.drawable.esp_device_status_cloud);
        } else {
            cloudStatusIV.setBackgroundResource(R.drawable.esp_device_status_offline);
        }

        CheckBox editCB = (CheckBox)convertView.findViewById(R.id.edit_check);
        editCB.setChecked(mEditCheckedDevices.contains(device) ? true : false);
        editCB.setOnClickListener(new CheckClickListener(device));
        editCB.setVisibility(mEditable ? View.VISIBLE : View.GONE);

        return convertView;
    }

    private class CheckClickListener implements View.OnClickListener {
        private IEspDevice mDevice;

        public CheckClickListener(IEspDevice device) {
            mDevice = device;
        }

        @Override
        public void onClick(View v) {
            CheckBox cb = (CheckBox)v;
            boolean isChecked = cb.isChecked();
            if (isChecked) {
                mEditCheckedDevices.add(mDevice);
            } else {
                mEditCheckedDevices.remove(mDevice);
            }

            if (mEditCheckedChangeListener != null) {
                mEditCheckedChangeListener.onEditCheckedChanged(cb, mDevice, isChecked);
            }
        }

    }

    /**
     * Set the devices could be checked
     * 
     * @param editable
     */
    public void setEditable(boolean editable)
    {
        mEditable = editable;
    }
    
    /**
     * Whether the devices could set checked
     * 
     * @return
     */
    public boolean isEditable()
    {
        return mEditable;
    }
    
    /**
     * Get the checked devices
     * 
     * @return
     */
    public Set<IEspDevice> getEditCheckedDevices()
    {
        return mEditCheckedDevices;
    }
    
    public void setOnEditCheckedChangeListener(OnEditCheckedChangeListener listener)
    {
        mEditCheckedChangeListener = listener;
    }
    
    /**
     * Set all devices checked
     */
    public void selectAllDevice() {
        mEditCheckedDevices.addAll(mDeviceList);
        notifyDataSetChanged();
    }
    
    /**
     * Clear all devices' checked state
     */
    public void cancelSelectAllDevice() {
        mEditCheckedDevices.clear();
        notifyDataSetChanged();
    }
}
