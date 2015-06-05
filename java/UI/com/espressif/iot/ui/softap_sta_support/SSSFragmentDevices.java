package com.espressif.iot.ui.softap_sta_support;

import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.espressif.iot.R;
import com.espressif.iot.action.device.builder.BEspAction;
import com.espressif.iot.action.softap_sta_support.ISSSActionDeviceUpgradeLocal;
import com.espressif.iot.action.softap_sta_support.SSSActionDeviceUpgradeLocalResult;
import com.espressif.iot.device.IEspDeviceSSS;
import com.espressif.iot.type.device.EspDeviceType;
import com.espressif.iot.type.net.IOTAddress;
import com.espressif.iot.ui.device.dialog.DeviceDialogBuilder;
import com.espressif.iot.user.builder.EspSSSUser;
import com.espressif.iot.util.BSSIDUtil;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class SSSFragmentDevices extends Fragment implements OnRefreshListener<ListView>, OnItemClickListener,
    OnItemLongClickListener
{
    private final Logger log = Logger.getLogger(SSSFragmentDevices.class);
    
    private SoftApStaSupportActivity mActivity;
    
    protected EspSSSUser mUser;
    
    protected List<IEspDeviceSSS> mStaList;
    
    protected PullToRefreshListView mListView;
    
    protected StaAdapter mStaAdapter;
    
    private View mScanningView;
    
    private boolean mScanning = false;
    
    final int PULL_REFRESH_LIST_HEADER_COUNT = 1;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        mActivity = (SoftApStaSupportActivity)getActivity();
        mStaList = new Vector<IEspDeviceSSS>();
        
        View view = inflater.inflate(R.layout.sss_pull_to_refresh_listview, null);
        mListView = (PullToRefreshListView)view.findViewById(R.id.pull_to_refresh_listview);
        mStaAdapter = new StaAdapter();
        mListView.setAdapter(mStaAdapter);
        mListView.setOnRefreshListener(this);
        mListView.setOnItemClickListener(this);
        mListView.getRefreshableView().setOnItemLongClickListener(this);
        
        mScanningView = view.findViewById(R.id.sss_scan_sta_progress_view);
        
        return view;
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        
        mUser = EspSSSUser.getInstance();
    }
    
    protected class StaAdapter extends BaseAdapter
    {
        
        @Override
        public int getCount()
        {
            return mStaList.size();
        }
        
        @Override
        public Object getItem(int position)
        {
            return mStaList.get(position);
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
            TextView typeTV = (TextView)convertView.findViewById(android.R.id.text2);
            
            IEspDeviceSSS device = mStaList.get(position);
            String name;
            if (device.getName() == null) {
                name = BSSIDUtil.genDeviceNameByBSSID(device.getBssid());
            }
            else
            {
                name = device.getName();
            }
            
            nameTV.setText(name);
            typeTV.setText(device.getDeviceType().toString());
            
            return convertView;
        }
        
    }
    
    public void scanStas()
    {
        if (!mScanning)
        {
            new ScanStasTask().execute();
        }
    }
    
    public void showScanningPorgress(boolean show)
    {
        if (mScanningView != null)
        {
            mScanningView.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }
    
    private class ScanStasTask extends AsyncTask<Void, Void, List<IEspDeviceSSS>>
    {
        @Override
        protected void onPreExecute()
        {
            log.debug("scan stas start");
            mScanning = true;
        }
        
        @Override
        protected List<IEspDeviceSSS> doInBackground(Void... params)
        {
            mUser.scanDevices();
            return mUser.getDeviceList();
        }
        
        @Override
        protected void onPostExecute(List<IEspDeviceSSS> result)
        {
            mStaList.clear();
            mStaList.addAll(result);
            mStaAdapter.notifyDataSetChanged();
            
            mListView.onRefreshComplete();
            
            mScanning = false;
            
            showScanningPorgress(false);
            log.debug("scan stas over");
            
            checkHelpOnPostScanSta();
        }
    }
    
    @Override
    public void onRefresh(PullToRefreshBase<ListView> arg0)
    {
        log.debug("on scan sta refresh");
        showScanningPorgress(false);
        scanStas();
    }
    
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
    {
        IEspDeviceSSS device = mStaList.get(position - PULL_REFRESH_LIST_HEADER_COUNT);
        EspDeviceType type = device.getDeviceType();
        if (checkHelpOnItemClick(device))
        {
            return;
        }
        switch (type)
        {
            case PLUG:
            case LIGHT:
            case REMOTE:
            case PLUGS:
                new DeviceDialogBuilder(mActivity, device).show();
                break;
            case ROOT:
                String bssid = device.getBssid();
                Intent intent =
                    new Intent(mActivity, SSSDeviceRootActivity.class)
                        .putExtra("bssid", bssid);
                mActivity.startActivity(intent);
                break;
            
            default:
                break;
        }
    }
    
    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id)
    {
        if (checkHelpOnItemLongClick())
        {
            return true;
        }
        
        final IOTAddress iotAddress = mStaList.get(position - PULL_REFRESH_LIST_HEADER_COUNT).getIOTAddress();
        EspDeviceType type = iotAddress.getDeviceTypeEnum();
        switch (type)
        {
            case PLUG:
            case LIGHT:
            case REMOTE:
                new AlertDialog.Builder(mActivity).setItems(R.array.esp_sss_device_use_options,
                    new DialogInterface.OnClickListener()
                    {
                        
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            String ip = iotAddress.getInetAddress().getHostAddress();
                            log.info(ip);
                            new UpgradeTask(ip).execute();
                            
                            checkHelpUpgrade();
                        }
                    }).show();
                return true;
                
            default:
                break;
        }
        return false;
    }
    
    private class UpgradeTask extends AsyncTask<String, Void, SSSActionDeviceUpgradeLocalResult> implements
        DialogInterface.OnClickListener
    {
        
        private ProgressDialog mDialog;
        
        private final ISSSActionDeviceUpgradeLocal mAction;
        
        private final String mIpStr;
        
        public UpgradeTask(String ip)
        {
            mIpStr = ip;
            mAction = (ISSSActionDeviceUpgradeLocal)BEspAction.getInstance().alloc(ISSSActionDeviceUpgradeLocal.class);
        }
        
        @Override
        protected void onPreExecute()
        {
            mDialog = new ProgressDialog(mActivity);
            mDialog.setCancelable(false);
            mDialog.setMessage(getString(R.string.esp_sss_device_upgrading_message));
            mDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.esp_sss_device_dialog_exit), this);
            mDialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.esp_sss_device_reset), this);
            mDialog.show();
            mDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setEnabled(false);
            mDialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
        }
        
        @Override
        protected SSSActionDeviceUpgradeLocalResult doInBackground(String... params)
        {
            return mAction.doActionSSSDeviceUpgradeLocal(mIpStr, mActivity);
        }
        
        @Override
        protected void onPostExecute(SSSActionDeviceUpgradeLocalResult result)
        {
            mDialog.setCancelable(true);
            mDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setEnabled(true);
            switch (result)
            {
                case DEVICE_NOT_SUPPORT:
                    mDialog.setMessage(getString(R.string.esp_sss_device_upgrade_result_not_support));
                    break;
                case FILE_NOT_FOUND:
                    mDialog.setMessage(getString(R.string.esp_sss_device_upgrade_result_file_not_found));
                    break;
                case POST_START_FAIL:
                case PUSH_BIN_FAIL:
                    mDialog.setMessage(getString(R.string.esp_sss_device_upgrade_result_push_bin_failed));
                    break;
                case SUC:
                    mDialog.setMessage(getString(R.string.esp_sss_device_upgrade_result_success));
                    mDialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(true);
                    break;
            }
        }
        
        @Override
        public void onClick(DialogInterface dialog, int which)
        {
            switch (which)
            {
                case DialogInterface.BUTTON_POSITIVE:
                    mRestTask.execute();
                    break;
                case DialogInterface.BUTTON_NEGATIVE:
                    break;
            }
        }
        
        private AsyncTask<Void, Void, Boolean> mRestTask = new AsyncTask<Void, Void, Boolean>()
        {
            
            @Override
            protected Boolean doInBackground(Void... params)
            {
                return mAction.doActionSSSDevicePostReset(mIpStr);
            }
            
            protected void onPostExecute(Boolean result)
            {
                if (result)
                {
                    scanStas();
                }
            }
        };
    }
    
    protected void checkHelpOnPostScanSta()
    {
    }
    
    protected boolean checkHelpOnItemClick(IEspDeviceSSS device)
    {
        return false;
    }
    
    protected boolean checkHelpOnItemLongClick()
    {
        return false;
    }
    
    protected void checkHelpUpgrade()
    {
    }
}
