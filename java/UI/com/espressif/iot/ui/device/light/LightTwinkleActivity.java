package com.espressif.iot.ui.device.light;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.espressif.iot.R;
import com.espressif.iot.command.device.light.EspCommandLightTwinkleLocal;
import com.espressif.iot.device.IEspDevice;
import com.espressif.iot.type.device.EspDeviceType;
import com.espressif.iot.ui.device.light.LightTwinkleServer.OnLightTwinkleListener;
import com.espressif.iot.ui.widget.adapter.DeviceAdapter;
import com.espressif.iot.user.IEspUser;
import com.espressif.iot.user.builder.BEspUser;
import com.espressif.iot.util.CommonUtils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

public class LightTwinkleActivity extends Activity implements OnLightTwinkleListener {
    private static final Logger log = Logger.getLogger(LightTwinkleActivity.class);

    private WifiManager mWifiManager;
    private IEspUser mUser;
    private List<IEspDevice> mDeviceList;

    private ListView mListView;
    private List<IEspDevice> mLightList;
    private LightAdapter mLightAdapter;

    private IEspDevice mTwinkleLight;

    private LightTwinkleServer mTwinkleServer;

    private TextView mServerTV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.light_twinkle_activity);

        mUser = BEspUser.getBuilder().getInstance();
        mWifiManager = (WifiManager)getSystemService(WIFI_SERVICE);

        mListView = (ListView)findViewById(R.id.list);
        mLightList = new ArrayList<IEspDevice>();
        mDeviceList = mUser.getAllDeviceList();
        for (IEspDevice device : mDeviceList) {
            if (device.getDeviceType() == EspDeviceType.LIGHT) {
                mLightList.add(device);
            }
        }
        mLightAdapter = new LightAdapter(this, mLightList);
        mListView.setAdapter(mLightAdapter);

        mServerTV = (TextView)findViewById(R.id.server_info);

        mTwinkleServer = LightTwinkleServer.INSTANCE;
        mTwinkleServer.registerOnLightTwinkleListener(this);
        new OpenServerTask().execute();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mTwinkleServer.unRegisterOnLightTwinkleListener(this);
    }

    @Override
    public void onBackPressed() {
        new CloseServerTask().execute();
    }

    private class LightAdapter extends DeviceAdapter {

        public LightAdapter(Activity activity, List<IEspDevice> list) {
            super(activity, list);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = super.getView(position, convertView, parent);

            if (view.getTag() == mTwinkleLight) {
                view.setBackgroundColor(Color.RED);
            } else {
                view.setBackgroundColor(Color.WHITE);
            }

            return view;
        }
    }

    @Override
    public void onTwinkle(String bssid) {
        log.debug("onTwinkle " + bssid);
        for (IEspDevice device : mLightList) {
            if (device.getBssid().equals(bssid)) {
                mTwinkleLight = device;
                mLightAdapter.notifyDataSetChanged();
                return;
            }
        }

        mTwinkleLight = null;
        mLightAdapter.notifyDataSetChanged();
    }

    private String getCurrentWifiIP() {
        String result = null;
        WifiInfo info = mWifiManager.getConnectionInfo();
        if (info != null) {
            int ip = info.getIpAddress();

            StringBuilder sb = new StringBuilder();
            for (int i = 0; i <= 3; i++) {
                sb.append((ip >> (i * 8)) & 0xff);
                if (i != 3) {
                    sb.append(".");
                }
            }
            result = sb.toString();
        }

        return result;
    }

    private abstract class ServerTask extends AsyncTask<Void, Void, Boolean> {
        protected LightTwinkleActivity mActivity;
        protected String mCurrentIP;

        public ServerTask() {
            mActivity = LightTwinkleActivity.this;
        }

        protected void notifyServerStatus(boolean open) {
            EspCommandLightTwinkleLocal cmd = new EspCommandLightTwinkleLocal();

            String ip = getCurrentWifiIP();
            mCurrentIP = ip;
            if (TextUtils.isEmpty(ip)) {
                return;
            }
            int port = mTwinkleServer.getPort();
            String id = CommonUtils.getMac();

            List<IEspDevice> cacheDevices = new ArrayList<IEspDevice>();
            for (IEspDevice device : mDeviceList) {
                boolean isStateLocal = device.getDeviceState().isStateLocal();
                boolean isRootRouter = device.getBssid().equals(device.getRootDeviceBssid());
                if (isStateLocal && isRootRouter) {
                    boolean suc = doCommand(open, cmd, device, ip, port, id);
                    if (!suc) {
                        cacheDevices.add(device);
                    }
                }
            }

            log.debug("ServerTask " + open + " notify failed " + cacheDevices.size());
            if (cacheDevices.isEmpty()) {
                for (IEspDevice device : cacheDevices) {
                    doCommand(open, cmd, device, ip, port, id);
                }
            }
        }

        private boolean doCommand(boolean open, EspCommandLightTwinkleLocal cmd, IEspDevice device, String ip, int port,
            String id) {
            if (open) {
                return cmd.doCommandPostTwinkleOn(device, ip, port, id);
            } else {
                return cmd.doCommandPostTwinkleOff(device, ip, port, id);
            }
        }
    }

    private class OpenServerTask extends ServerTask {

        @Override
        protected Boolean doInBackground(Void... params) {
            mTwinkleServer.openServer();

            notifyServerStatus(true);
            return null;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            mServerTV.setText(mCurrentIP + ":" + mTwinkleServer.getPort());
        }
    }

    private class CloseServerTask extends ServerTask {
        private ProgressDialog mDialog;

        @Override
        protected void onPreExecute() {
            mDialog = new ProgressDialog(mActivity);
            mDialog.setMessage(mActivity.getText(R.string.esp_device_light_twinkle_progress_close));
            mDialog.setCancelable(false);
            mDialog.show();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            notifyServerStatus(false);

            mTwinkleServer.closeServer();
            return null;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (mDialog != null) {
                mDialog.dismiss();
                mDialog = null;
            }

            mActivity.finish();
        }
    }
}
