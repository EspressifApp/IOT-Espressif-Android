package com.espressif.iot.ui.softap_sta_support;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.espressif.iot.R;
import com.espressif.iot.base.api.EspBaseApiUtil;
import com.espressif.iot.device.IEspDeviceNew;
import com.espressif.iot.help.ui.IEspHelpUISSSUseDevice;
import com.espressif.iot.model.help.statemachine.EspHelpStateMachine;
import com.espressif.iot.type.device.DeviceInfo;
import com.espressif.iot.type.help.HelpStepSSSUseDevice;
import com.espressif.iot.user.IEspUser;
import com.espressif.iot.user.builder.BEspUser;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class SSSFragmentConfigure extends Fragment implements OnRefreshListener<ListView>, OnItemClickListener,
    IEspHelpUISSSUseDevice
{
    private final Logger log = Logger.getLogger(SSSFragmentConfigure.class);
    
    private SoftApStaSupportActivity mActivity;
    
    private IEspUser mUser;
    
    private PullToRefreshListView mListView;
    
    private List<IEspDeviceNew> mSoftApList;
    
    private SoftAPAdapter mSoftAPAdapter;
    
    private EspHelpStateMachine mHelpMachine;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        mActivity = (SoftApStaSupportActivity)getActivity();
        mUser = BEspUser.getBuilder().getInstance();
        
        View view = inflater.inflate(R.layout.sss_pull_to_refresh_listview, null);
        mListView = (PullToRefreshListView)view.findViewById(R.id.pull_to_refresh_listview);
        mSoftApList = new Vector<IEspDeviceNew>();
        mSoftApList.addAll(mUser.scanSoftapDeviceList(false));
        sortByRssi(mSoftApList);
        mSoftAPAdapter = new SoftAPAdapter();
        mListView.setAdapter(mSoftAPAdapter);
        mListView.setOnRefreshListener(this);
        mListView.setOnItemClickListener(this);
        
        return view;
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        
        mHelpMachine = EspHelpStateMachine.getInstance();
    }
    
    private class SoftAPAdapter extends BaseAdapter
    {
        
        @Override
        public int getCount()
        {
            return mSoftApList.size();
        }
        
        @Override
        public Object getItem(int position)
        {
            return mSoftApList.get(position);
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
                convertView = mActivity.getLayoutInflater().inflate(android.R.layout.simple_list_item_2, null);
            }
            
            TextView nameTV = (TextView)convertView.findViewById(android.R.id.text1);
            TextView statusTV = (TextView)convertView.findViewById(android.R.id.text2);
            
            IEspDeviceNew device = mSoftApList.get(position);
            nameTV.setText(device.getName());
            statusTV.setText("rssi: " + device.getRssi());
            
            return convertView;
        }
        
    }
    
    private class ScanSoftAPTask extends AsyncTask<Void, Void, List<IEspDeviceNew>>
    {
        
        @Override
        protected List<IEspDeviceNew> doInBackground(Void... params)
        {
            return mUser.scanSoftapDeviceList(false);
        }
        
        @Override
        protected void onPostExecute(List<IEspDeviceNew> result)
        {
            mSoftApList.clear();
            mSoftApList.addAll(result);
            sortByRssi(mSoftApList);
            mSoftAPAdapter.notifyDataSetChanged();
            
            mListView.onRefreshComplete();
            log.debug("scan softap over");
            
            if (mHelpMachine.isHelpModeUseSSSDevice())
            {
                mHelpMachine.transformState(!mSoftApList.isEmpty());
                onHelpUseSSSDevice();
            }
        }
    }
    
    private void sortByRssi(List<IEspDeviceNew> list)
    {
        Comparator<IEspDeviceNew> comparatro = new Comparator<IEspDeviceNew>()
        {
            
            @Override
            public int compare(IEspDeviceNew lhs, IEspDeviceNew rhs)
            {
                Integer lRssi = lhs.getRssi();
                Integer rRssi = rhs.getRssi();
                return rRssi.compareTo(lRssi);
            }
            
        };
        
        Collections.sort(list, comparatro);
    }
    
    @Override
    public void onRefresh(PullToRefreshBase<ListView> arg0)
    {
        log.debug("on scan softap refresh");
        new ScanSoftAPTask().execute();
    }
    
    private static final int OPTION_DIRECT_CONNECT = 0;
    
    private static final int OPTION_CONFIGURE = 1;
    
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
    {
        final int pullRefreshListHeaderCount = 1;
        final IEspDeviceNew device = mSoftApList.get(position - pullRefreshListHeaderCount);
        
        new AlertDialog.Builder(mActivity).setItems(R.array.esp_sss_device_configure_options,
            new DialogInterface.OnClickListener()
            {
                
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    switch (which)
                    {
                        case OPTION_DIRECT_CONNECT:
                            if (EspBaseApiUtil.isWifiEnabled()) {
                                new DirectConnectTask().execute(device);
                            }
                            else
                            {
                                Toast.makeText(mActivity, R.string.esp_sss_configure_wifi_hint, Toast.LENGTH_SHORT)
                                    .show();
                            }
                            break;
                        case OPTION_CONFIGURE:
                            // TODO configure to AP
                            break;
                    }
                }
            }).show();
    }
    
    private class DirectConnectTask extends AsyncTask<IEspDeviceNew, Void, DeviceInfo>
    {
        
        private ProgressDialog mDialog;
        
        @Override
        protected void onPreExecute()
        {
            mDialog = new ProgressDialog(mActivity);
            mDialog.setMessage(getString(R.string.esp_sss_configure_direct_connect_message));
            mDialog.setCancelable(false);
            mDialog.show();
        }
        
        @Override
        protected DeviceInfo doInBackground(IEspDeviceNew... params)
        {
            IEspDeviceNew device = params[0];
            return BEspUser.getBuilder().getInstance().doActionDeviceNewGetInfo(device);
        }
        
        @Override
        protected void onPostExecute(DeviceInfo result)
        {
            mDialog.dismiss();
            mDialog = null;
            
            if (result == null)
            {
                Toast.makeText(mActivity, R.string.esp_sss_configure_direct_connect_failed, Toast.LENGTH_LONG).show();
                
                if(mHelpMachine.isHelpModeUseSSSDevice())
                {
                    mHelpMachine.transformState(false);
                    onHelpUseSSSDevice();
                }
            }
            else
            {
                mActivity.notifyFragment(mActivity.FRAGMENT_DEVICE);
                if (!result.isTypeUnknow())
                {
                    mActivity.notifyFragment(mActivity.FRAGMENT_DEVICE);
                    Toast.makeText(mActivity, R.string.esp_sss_configure_direct_connect_success, Toast.LENGTH_LONG)
                        .show();
                    
                    if(mHelpMachine.isHelpModeUseSSSDevice())
                    {
                        mHelpMachine.transformState(true);
                        onHelpUseSSSDevice();
                    }
                }
                else
                {
                    Toast.makeText(mActivity, R.string.esp_sss_configure_not_support, Toast.LENGTH_LONG).show();
                    
                    if(mHelpMachine.isHelpModeUseSSSDevice())
                    {
                        mHelpMachine.transformState(false);
                        mHelpMachine.transformState(false);
                        onHelpUseSSSDevice();
                    }
                }
            }
        }
    }

    @Override
    public void onHelpUseSSSDevice()
    {
        mActivity.clearHelpContainer();
        
        HelpStepSSSUseDevice step = HelpStepSSSUseDevice.valueOf(mHelpMachine.getCurrentStateOrdinal());
        switch(step)
        {
            case START_USE_HELP:
                break;
            case FAIL_FOUND_DEVICE:
                break;
            case START_DIRECT_CONNECT:
                break;
            case FIND_SOFTAP:
                mSoftApList.clear();
                mSoftAPAdapter.notifyDataSetChanged();
                mActivity.highlightHelpView(mListView);
                mActivity.gestureAnimHint(R.string.esp_sss_help_use_device_find_softap_msg,
                    R.anim.esp_pull_to_refresh_hint);
                break;
            case FOUND_SOFTAP_FAILED:
                mActivity.highlightHelpView(mListView);
                mHelpMachine.retry();
                mActivity.gestureAnimHint(R.string.esp_sss_help_use_device_found_softap_failed_msg,
                    R.anim.esp_pull_to_refresh_hint);
                break;
            case SELECT_SOFTAP:
                mActivity.highlightHelpView(mListView);
                mActivity.setHelpHintMessage(R.string.esp_sss_help_use_device_select_softap_msg);
                break;
            case CONNECT_SOFTAP_FAILED:
                mHelpMachine.retry();
                mActivity.highlightHelpView(mListView);
                mActivity.setHelpHintMessage(R.string.esp_sss_help_use_device_connect_softap_failed_msg);
                break;
            case SOFTAP_NOT_SUPPORT:
                mHelpMachine.retry();
                mActivity.highlightHelpView(mListView);
                mActivity.setHelpHintMessage(R.string.esp_sss_help_use_device_not_support_msg);
                break;
            case DEVICE_SELECT:
                mActivity.onHelpUseSSSDevice();
                break;
            case DEVICE_CONTROL:
                break;
            case DEVICE_CONTROL_FAILED:
                break;
            case SUC:
                break;
        }
        
    }
}
