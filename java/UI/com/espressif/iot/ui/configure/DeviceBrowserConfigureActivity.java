package com.espressif.iot.ui.configure;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.espressif.iot.R;
import com.espressif.iot.base.api.EspBaseApiUtil;
import com.espressif.iot.device.IEspDeviceNew;
import com.espressif.iot.ui.main.EspActivityAbs;
import com.espressif.iot.ui.widget.adapter.SoftAPAdapter;
import com.espressif.iot.user.IEspUser;
import com.espressif.iot.user.builder.BEspUser;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.Toast;

public class DeviceBrowserConfigureActivity extends EspActivityAbs
    implements OnItemClickListener, OnRefreshListener<ListView> {
    private IEspUser mUser;

    private PullToRefreshListView mSoftApListView;
    private List<IEspDeviceNew> mSoftApList;
    private BaseAdapter mSoftApAdapter;

    private static final int MENU_ID_CONNECT = 1;

    private DeviceBrowserWebFragment mFragment;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.device_browser_configure);

        mUser = BEspUser.getBuilder().getInstance();

        mSoftApList = new ArrayList<IEspDeviceNew>();
        mSoftApAdapter = new SoftAPAdapter(this, mSoftApList);
        mSoftApListView = (PullToRefreshListView)findViewById(R.id.softap_list);
        mSoftApListView.setAdapter(mSoftApAdapter);
        mSoftApListView.setOnItemClickListener(this);
        mSoftApListView.setOnRefreshListener(this);

        new RefreshSoftAPTask().execute();
    }

    @Override
    public void onRefresh(PullToRefreshBase<ListView> refreshView) {
        new RefreshSoftAPTask().execute();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        IEspDeviceNew device = (IEspDeviceNew)view.getTag();
        PopupMenu popupMenu = new PopupMenu(this, view);
        Menu menu = popupMenu.getMenu();
        menu.add(Menu.NONE, MENU_ID_CONNECT, 0, R.string.esp_browser_confiugre_menu_connect);
        popupMenu.setOnMenuItemClickListener(new SoftAPMenuClickListener(device));
        popupMenu.show();
    }

    @Override
    protected void onTitleRightIconClick(View rightIcon) {
        if (mFragment != null) {
            mFragment.onTitleRightIconClick();
        }
    }
    
    private class RefreshSoftAPTask extends AsyncTask<Void, Void, List<IEspDeviceNew>> {
        @Override
        protected List<IEspDeviceNew> doInBackground(Void... params) {
            List<IEspDeviceNew> list = mUser.scanSoftapDeviceList();
            sortDeviceByRssi(list);
            return list;
        }

        @Override
        protected void onPostExecute(List<IEspDeviceNew> result) {
            mSoftApList.clear();
            mSoftApList.addAll(result);
            mSoftApAdapter.notifyDataSetChanged();
            mSoftApListView.onRefreshComplete();
        }

        private void sortDeviceByRssi(List<IEspDeviceNew> list) {
            Comparator<IEspDeviceNew> comparatro = new Comparator<IEspDeviceNew>() {

                @Override
                public int compare(IEspDeviceNew lhs, IEspDeviceNew rhs) {
                    Integer lRssi = lhs.getRssi();
                    Integer rRssi = rhs.getRssi();
                    return rRssi.compareTo(lRssi);
                }

            };

            Collections.sort(list, comparatro);
        }
    }

    private class SoftAPMenuClickListener implements OnMenuItemClickListener {
        private IEspDeviceNew mDevice;

        public SoftAPMenuClickListener(IEspDeviceNew softap) {
            mDevice = softap;
        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            switch (item.getItemId()) {
                case MENU_ID_CONNECT:
                    new ConnectTask(mDevice).execute();
                    return true;
            }
            return false;
        }

    }

    /**
     * Connect the device and show it's configure web page
     */
    private class ConnectTask extends AsyncTask<Void, Void, Boolean> {
        private Activity mActivity;
        private IEspDeviceNew mDevice;
        private ProgressDialog mDialog;

        public ConnectTask(IEspDeviceNew device) {
            mActivity = DeviceBrowserConfigureActivity.this;
            mDevice = device;
        }

        @Override
        protected void onPreExecute() {
            mDialog = new ProgressDialog(mActivity);
            mDialog.setMessage(mActivity.getString(R.string.esp_browser_confiugre_connecting));
            mDialog.setCancelable(false);
            mDialog.show();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                mDevice.setApSsid(EspBaseApiUtil.getWifiConnectedSsid());
                return EspBaseApiUtil.connect(mDevice.getSsid(),
                    mDevice.getWifiCipherType(),
                    mDevice.getDefaultPassword());
            }
            catch (InterruptedException e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (mDialog != null) {
                mDialog.dismiss();
                mDialog = null;
            }

            if (result) {
                commitWebPageFragment(mDevice);
            } else {
                Toast.makeText(mActivity, R.string.esp_browser_configure_connect_failed, Toast.LENGTH_LONG).show();
            }
        }
    }

    // Show the device configure web page
    private void commitWebPageFragment(IEspDeviceNew device) {
        mFragment = new DeviceBrowserWebFragment();
        mFragment.setDevice(device);
        getFragmentManager().beginTransaction().replace(R.id.fragment_container, mFragment).commit();
    }
}
