package com.espressif.iot.ui.view;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class WifiAdapter extends BaseAdapter
{
    private Context mContext;
    private final List<ScanResult> mWifiList;
    
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
            LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(android.R.layout.simple_list_item_1, null);
        }
        ScanResult sr = mWifiList.get(position);
        
        TextView wifiSsidTV = (TextView)convertView.findViewById(android.R.id.text1);
        wifiSsidTV.setText(sr.SSID);
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
    
    public void addFilter(String bssid)
    {
        if (!mFilter.contains(bssid))
        {
            mFilter.add(bssid);
            notifyDataSetChanged();
        }
    }
    
    public void removeFilter(String bssid)
    {
        mFilter.remove(bssid);
    }
}
