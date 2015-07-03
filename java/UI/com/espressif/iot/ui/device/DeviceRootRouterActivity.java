package com.espressif.iot.ui.device;

import java.util.ArrayList;
import java.util.List;

import com.espressif.iot.R;
import com.espressif.iot.action.device.common.EspActionDeviceReconnectLocal;
import com.espressif.iot.action.device.common.IEspActionDeviceReconnectLocal;
import com.espressif.iot.adt.tree.IEspDeviceTreeElement;
import com.espressif.iot.base.api.EspBaseApiUtil;
import com.espressif.iot.device.IEspDevice;
import com.espressif.iot.type.device.EspDeviceType;
import com.espressif.iot.type.net.WifiCipherType;
import com.espressif.iot.ui.device.dialog.DeviceLightDialog;
import com.espressif.iot.ui.device.dialog.DevicePlugDialog;
import com.espressif.iot.ui.device.dialog.DeviceRemoteDialog;
import com.espressif.iot.ui.view.TreeView;
import com.espressif.iot.ui.view.TreeView.LastLevelItemLongClickListener;
import com.espressif.iot.user.builder.BEspUser;
import com.espressif.iot.util.BSSIDUtil;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.net.wifi.ScanResult;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

public class DeviceRootRouterActivity extends DeviceActivityAbs implements OnClickListener, LastLevelItemLongClickListener
{
    private Button mPlugBtn;
    private Button mLightBtn;
    private Button mRemoteBtn;
    
    private TreeView mTreeView;
    
    private ChangeChildRouterDialog mChangeRouterDialog;
    
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        mChangeRouterDialog = new ChangeChildRouterDialog(this);
        
        // hide mesh child control
        mPager.setCurrentItem(1);
        mPager.setInterceptTouchEvent(false);
    }

    @Override
    protected View initControlView()
    {
        View view  = getLayoutInflater().inflate(R.layout.device_activity_root_router, null);
        
        mPlugBtn = (Button)view.findViewById(R.id.mesh_all_plug_btn);
        mPlugBtn.setOnClickListener(this);
        mLightBtn = (Button)view.findViewById(R.id.mesh_all_light_btn);
        mLightBtn.setOnClickListener(this);
        mRemoteBtn = (Button)view.findViewById(R.id.mesh_all_remote_btn);
        mRemoteBtn.setOnClickListener(this);
        
        mTreeView = getTreeView();
        mTreeView.setLastLevelItemLongClickCallBack(this);
        
        return view;
    }

    @Override
    protected void executePrepare()
    {
    }

    @Override
    protected void executeFinish(int command, boolean result)
    {
    }

    @Override
    public void onClick(View v)
    {
        if (v == mPlugBtn)
        {
            new DevicePlugDialog(this, mIEspDevice).show();
        }
        else if (v == mLightBtn)
        {
            new DeviceLightDialog(this, mIEspDevice).show();
        }
        else if (v == mRemoteBtn)
        {
            new DeviceRemoteDialog(this, mIEspDevice).show();
        }
    }

    @Override
    public boolean onLastLevelItemLongClick(IEspDeviceTreeElement element, int position)
    {
        IEspDevice device = element.getCurrentDevice();
        if (device.getDeviceType() != EspDeviceType.ROOT)
        {
            mChangeRouterDialog.setDevice(device);
            mChangeRouterDialog.show();
            return true;
        }
        else
        {
            return false;
        }
    }
    
    private class ChangeChildRouterDialog implements DialogInterface.OnClickListener {
        private Activity mActivity;
        
        private IEspDevice mDevice;
        
        private AlertDialog mDialog;
        private ProgressDialog mProgressDialog;
        
        private Spinner mSpinner;
        private List<Router> mRouterList;
        private ArrayAdapter<Router> mRouterAdapter;
        
        private EditText mPasswordEdT;
        
        public ChangeChildRouterDialog(Activity activity) {
            mActivity = activity;
            
            init();
        }
        
        private void init() {
            View view = mActivity.getLayoutInflater().inflate(R.layout.device_change_router_dialog, null);
            mSpinner = (Spinner)view.findViewById(R.id.router_spinner);
            mRouterList = new ArrayList<Router>();
            mRouterAdapter = new ArrayAdapter<Router>(mActivity, android.R.layout.simple_list_item_1, mRouterList);
            mSpinner.setAdapter(mRouterAdapter);
            mPasswordEdT = (EditText)view.findViewById(R.id.router_password);
            
            mDialog =
                new AlertDialog.Builder(mActivity).setView(view).setPositiveButton(android.R.string.ok, this).create();
            
            mProgressDialog = new ProgressDialog(mActivity);
            mProgressDialog.setMessage(mActivity.getString(R.string.esp_device_change_router_progress));
            mProgressDialog.setCancelable(false);
        }
        
        public void setDevice(IEspDevice device) {
            mDevice = device;
        }
        
        public void show() {
            mDialog.setTitle(mActivity.getString(R.string.esp_device_change_router, mDevice.getName()));
            updateRouterList();
            mPasswordEdT.setText("");
            mDialog.show();
        }

        private void updateRouterList() {
            mRouterList.clear();
            List<ScanResult> list = EspBaseApiUtil.scan();
            for (ScanResult sr : list) {
                if (BSSIDUtil.isEqualIgnore2chars(mDevice.getBssid(), sr.BSSID)) {
                    continue;
                }
                
                Router router = new Router();
                router.ssid = sr.SSID;
                router.wifiType = WifiCipherType.getWifiCipherType(sr);
                mRouterList.add(router);
            }
            
            mRouterAdapter.notifyDataSetChanged();
        }
        
        @Override
        public void onClick(DialogInterface dialog, int which)
        {
            String password = mPasswordEdT.getText().toString();
            int selection = mSpinner.getSelectedItemPosition();
            Router router = mRouterList.get(selection);
            
            new ChangeRouterTask(router, password).execute();
        }
        
        public class Router
        {
            String ssid;
            WifiCipherType wifiType;
            
            @Override
            public String toString()
            {
                return ssid;
            }
        }
        
        private class ChangeRouterTask extends AsyncTask<Void, Void, Boolean>
        {
            final Router mRouter;
            final String mPassword;
            
            public ChangeRouterTask(Router router, String password)
            {
                mRouter = router;
                mPassword = password;
            }
            
            @Override
            protected void onPreExecute()
            {
                mProgressDialog.show();
            }
            
            @Override
            protected Boolean doInBackground(Void... params)
            {
                IEspActionDeviceReconnectLocal action = new EspActionDeviceReconnectLocal();
                String router = mDevice.getRouter();
                String deviceBssid = mDevice.getBssid();
                String apSsid = mRouter.ssid;
                WifiCipherType type = mRouter.wifiType;
                String apPassword = mPassword;
                List<IEspDevice> currentDeviceList = BEspUser.getBuilder().getInstance().getDeviceList();
                boolean result =
                    action.doActionDeviceReconnectLocal(currentDeviceList,
                        router,
                        deviceBssid,
                        apSsid,
                        type,
                        apPassword);
                return result;
            }
            
            @Override
            protected void onPostExecute(Boolean result)
            {
                mProgressDialog.dismiss();
            }
        }
    }
}
