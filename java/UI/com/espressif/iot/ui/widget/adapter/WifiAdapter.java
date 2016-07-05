package com.espressif.iot.ui.widget.adapter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.espressif.iot.device.IEspDevice;
import com.espressif.iot.user.builder.BEspUser;
import com.espressif.iot.util.BSSIDUtil;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class WifiAdapter extends BaseAdapter
{
    private Context mContext;
    private final List<ScanResult> mWifiList;
    
    /**
     * The BSSID set of WIFI need filter
     */
    private Set<String> mFilter;
    
    public WifiAdapter(Context context, List<ScanResult> list)
    {
        mContext = context;
        mWifiList = list;
        
        mFilter = new HashSet<String>();
    }
    
    @Override
    public int getCount()
    {
        return mWifiList.size();
    }
    
    @Override
    public ScanResult getItem(int position)
    {
        return mWifiList.get(position);
    }
    
    @Override
    public long getItemId(int position)
    {
        return 0;
    }
    
    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        if (convertView == null)
        {
            convertView = View.inflate(mContext, android.R.layout.simple_list_item_1, null);
        }
        ScanResult sr = mWifiList.get(position);
        
        TextView wifiSsidTV = (TextView)convertView.findViewById(android.R.id.text1);
        
        String displayText = sr.SSID;
        // check whether the device is configured
        List<IEspDevice> deviceList = BEspUser.getBuilder().getInstance().getDeviceList();
        for (IEspDevice device : deviceList)
        {
            // when device activating fail, it will be stored in local db and be activating state,
            // at this situation, we don't let the displayText change to device name
            if (device.getDeviceState().isStateActivating())
            {
                continue;
            }
            if (BSSIDUtil.isEqualIgnore2chars(device.getBssid(), sr.BSSID))
            {
                displayText = device.getName();
                break;
            }
        }
        wifiSsidTV.setText(displayText);
        return convertView;
    }
    
    @Override
    public void notifyDataSetChanged()
    {
        filterWifi();
        
        super.notifyDataSetChanged();
    }
    
    @Override
    public void notifyDataSetInvalidated()
    {
        filterWifi();
        
        super.notifyDataSetInvalidated();
    }
    
    private void filterWifi()
    {
        for (int i = 0; i < mWifiList.size(); i++)
        {
            ScanResult sr = mWifiList.get(i);
            for (String filter : mFilter)
            {
                if (filter.equals(sr.BSSID))
                {
                    mWifiList.remove(i);
                    i--;
                }
            }
        }
    }
    
    /**
     * Filter the WIFI
     * 
     * @param bssid of WIFI
     */
    public void addFilter(String bssid)
    {
        if (!mFilter.contains(bssid))
        {
            mFilter.add(bssid);
            notifyDataSetChanged();
        }
    }
    
    /**
     * Remove the filter of the WIFI
     * 
     * @param bssid of WIFI
     */
    public void removeFilter(String bssid)
    {
        mFilter.remove(bssid);
    }
}
