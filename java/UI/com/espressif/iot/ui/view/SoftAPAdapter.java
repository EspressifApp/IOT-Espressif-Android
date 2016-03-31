package com.espressif.iot.ui.view;

import java.util.List;

import com.espressif.iot.R;
import com.espressif.iot.device.IEspDeviceNew;

import android.app.Activity;
import android.net.wifi.WifiManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class SoftAPAdapter extends BaseAdapter {
    private Activity mActivity;
    private List<IEspDeviceNew> mSoftApList;

    public SoftAPAdapter(Activity activity, List<IEspDeviceNew> softApList) {
        mActivity = activity;
        mSoftApList = softApList;
    }

    @Override
    public int getCount() {
        return mSoftApList.size();
    }

    @Override
    public IEspDeviceNew getItem(int position) {
        return mSoftApList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = View.inflate(mActivity, R.layout.device_layout, null);
        }

        IEspDeviceNew deviceNew = mSoftApList.get(position);
        convertView.setTag(deviceNew);

        TextView deviceNameTV = (TextView)convertView.findViewById(R.id.device_name);
        String displayText = deviceNew.getSsid();
        deviceNameTV.setText(displayText);

        ImageView deviceIconIV = (ImageView)convertView.findViewById(R.id.device_icon);
        deviceIconIV.setImageResource(R.drawable.esp_wifi_signal);
        deviceIconIV.getDrawable().setLevel(WifiManager.calculateSignalLevel(deviceNew.getRssi(), 5));

        TextView deviceRssiTV = (TextView)convertView.findViewById(R.id.device_status_text);
        deviceRssiTV.setText("RSSI: " + deviceNew.getRssi());

        ImageView meshIcon = (ImageView)convertView.findViewById(R.id.device_status1);
        boolean isMesh = deviceNew.getIsMeshDevice();
        if (isMesh) {
            meshIcon.setBackgroundResource(R.drawable.esp_icon_mesh);
        } else {
            meshIcon.setBackgroundResource(0);
        }

        return convertView;
    }

}
