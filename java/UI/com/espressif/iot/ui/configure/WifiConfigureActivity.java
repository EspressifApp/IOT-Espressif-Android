package com.espressif.iot.ui.configure;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.espressif.iot.R;
import com.espressif.iot.db.IOTApDBManager;
import com.espressif.iot.object.db.IApDB;
import com.espressif.iot.type.net.WifiCipherType;
import com.espressif.iot.ui.main.EspActivityAbs;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class WifiConfigureActivity extends EspActivityAbs
{
    private final Logger log = Logger.getLogger(WifiConfigureActivity.class);
    
    private List<ApInfo> mApInfoList;
    
    private ApInfoAdapter mApInfoAdapter;
    
    private ListView mWifiListView;
    
    private IOTApDBManager mIOTApDBManager;
    
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        __init();
        
        setTitle(R.string.esp_wifi_edit);
    }
    
    private void __init()
    {
        mIOTApDBManager = IOTApDBManager.getInstance();
        
        setContentView(R.layout.wifi_configure_activity);
        
        mWifiListView = (ListView)findViewById(R.id.wifi_listview);
        mApInfoAdapter = new ApInfoAdapter(this);
        mApInfoList = new ArrayList<ApInfo>();
        List<IApDB> apDBList = mIOTApDBManager.getAllApDBList();
        for (IApDB apDB : apDBList)
        {
            ApInfo apInfo =
                new ApInfo(apDB.getBssid(), apDB.getSsid(), apDB.getPassword(), WifiCipherType.WIFICIPHER_INVALID);
            mApInfoList.add(apInfo);
        }
        // set adapter
        mWifiListView.setAdapter(mApInfoAdapter);
    }
    
    private class ApInfoAdapter extends BaseAdapter
    {
        
        private Activity mActivity;
        
        public ApInfoAdapter(Activity activity)
        {
            mActivity = activity;
        }
        
        @Override
        public int getCount()
        {
            return mApInfoList.size();
        }
        
        @Override
        public Object getItem(int position)
        {
            return mApInfoList.get(position);
        }
        
        @Override
        public long getItemId(int position)
        {
            return 0;
        }
        
        @Override
        public View getView(final int position, View convertView, ViewGroup parent)
        {
            if (convertView == null)
            {
                LayoutInflater inflater = mActivity.getLayoutInflater();
                convertView = inflater.inflate(R.layout.wifi_layout, parent, false);
            }
            
            final ApInfo apInfo = mApInfoList.get(position);
            TextView wifiTV = (TextView)convertView.findViewById(R.id.wifi_ssid);
            wifiTV.setText(apInfo.ssid);
            ImageView wifiIcon = (ImageView)convertView.findViewById(R.id.wifi_icon);
            wifiIcon.setImageResource(R.drawable.esp_device_signal_4);
            Button editBtn = (Button)convertView.findViewById(R.id.wifi_edit);
            editBtn.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    final EditText edt = new EditText(mActivity);
                    ViewGroup.LayoutParams lp =
                        new ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
                    edt.setLayoutParams(lp);
                    new AlertDialog.Builder(mActivity).setTitle(R.string.esp_wifi_pwd_edit_title)
                        .setView(edt)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                ApInfo apInfo = mApInfoList.get(position);
                                log.info("apInfo: " + apInfo + " is edited");
                                String password = edt.getText().toString();
                                ApInfo newApInfo = new ApInfo(apInfo.bssid, apInfo.ssid, password, apInfo.type);
                                // replace apInfo
                                mApInfoList.remove(position);
                                mApInfoList.add(position, newApInfo);
                                mIOTApDBManager.updatePassword(apInfo.ssid, password);
                                mApInfoAdapter.notifyDataSetChanged();
                            }
                            
                        })
                        .setNegativeButton(android.R.string.cancel, null)
                        .show();
                }
            });
            Button deleteBtn = (Button)convertView.findViewById(R.id.wifi_delete);
            deleteBtn.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    new AlertDialog.Builder(mActivity).setTitle(R.string.esp_wifi_pwd_delete)
                        .setMessage(R.string.esp_wifi_pwd_delete_sellected_message)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener()
                        {
                            
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                log.info("apInfo: " + mApInfoList.get(position) + " is deleted");
                                mIOTApDBManager.delete(apInfo.ssid);
                                mApInfoList.remove(position);
                                mApInfoAdapter.notifyDataSetChanged();
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, null)
                        .show();
                }
            });
            return convertView;
        }
        
    }
    
}
