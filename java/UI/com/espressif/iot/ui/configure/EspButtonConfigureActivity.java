package com.espressif.iot.ui.configure;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.espressif.iot.R;
import com.espressif.iot.command.device.espbutton.IEspButtonConfigureListener;
import com.espressif.iot.device.IEspDevice;
import com.espressif.iot.ui.main.EspActivityAbs;
import com.espressif.iot.user.IEspUser;
import com.espressif.iot.user.builder.BEspUser;
import com.espressif.iot.util.EspStrings;

public class EspButtonConfigureActivity extends EspActivityAbs implements OnClickListener
{
    private IEspUser mUser;
    
    private String mMac;
    private String mTempKey;
    
    private List<IEspDevice> mDevices;
    private ListView mDeviceListView;
    private SimpleDeviceAdapter mDeviceAdapter;
    
    private TextView mMacTV;
    private TextView mTempKeyTV;
    private CheckBox mPermitAllCB;
    private TextView mTimeoutTV;
    private Button mConfigureBtn;
    
    private int mTimeoutCount; // unit is second
    
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.espbutton_configure_activity);
        
        mUser = BEspUser.getBuilder().getInstance();
        
        Intent intent = getIntent();
        mTempKey = intent.getStringExtra(EspStrings.Key.KEY_ESPBUTTON_TEMP_KEY);
        mMac = intent.getStringExtra(EspStrings.Key.KEY_ESPBUTTON_MAC);
        String[] deviceKeys = intent.getStringArrayExtra(EspStrings.Key.KEY_ESPBUTTON_DEVICE_KEYS);
        
        if (TextUtils.isEmpty(mTempKey) || TextUtils.isEmpty(mMac))
        {
            // Just test
            mMac = "18FE34A48CA3";
            mTempKey = "8899aabbccddeeff0011223344556677";
        }
        
        mDevices = new ArrayList<IEspDevice>();
        mDeviceListView = (ListView)findViewById(R.id.device_list);
        if (deviceKeys != null)
        {
            for (String deviceKey : deviceKeys)
            {
                IEspDevice device = mUser.getUserDevice(deviceKey);
                if (device != null)
                {
                    mDevices.add(device);
                }
            }
        }
        
        mMacTV = (TextView)findViewById(R.id.espbutton_mac);
        mMacTV.setText(getString(R.string.esp_espbutton_mac, mMac));
        mTempKeyTV = (TextView)findViewById(R.id.espbutton_temp_key);
        mTempKeyTV.setText(getString(R.string.esp_espbutton_temp_key, mTempKey));
        
        mPermitAllCB = (CheckBox)findViewById(R.id.espbutton_permit_all);
        
        mTimeoutTV = (TextView)findViewById(R.id.espbutton_timeout_countdown);
        
        mConfigureBtn = (Button)findViewById(R.id.espbutton_start_configure);
        mConfigureBtn.setOnClickListener(this);
        
        mDeviceAdapter = new SimpleDeviceAdapter(this, mDevices);
        mDeviceListView.setAdapter(mDeviceAdapter);
        if (mDevices.isEmpty())
        {
            mDevices.addAll(mUser.getAllDeviceList());
        }
    }
    
    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        
        stopCountdown();
    }
    
    private class SimpleDeviceAdapter extends BaseAdapter
    {
        private Activity mActivity;
        private List<IEspDevice> mList;
        
        public SimpleDeviceAdapter(Activity activity, List<IEspDevice> deviceList)
        {
            mActivity = activity;
            mList = new ArrayList<IEspDevice>();
            mList.addAll(deviceList);
        }
        
        @Override
        public int getCount()
        {
            return mList.size();
        }

        @Override
        public Object getItem(int position)
        {
            return mList.get(position);
        }

        @Override
        public long getItemId(int position)
        {
            return mList.get(position).getId();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            if (convertView == null)
            {
                convertView = View.inflate(mActivity, android.R.layout.simple_list_item_2, null);
            }
            
            IEspDevice device = mList.get(position);
            TextView nameTV = (TextView)convertView.findViewById(android.R.id.text1);
            TextView typeTV = (TextView)convertView.findViewById(android.R.id.text2);
            nameTV.setText(device.getName());
            typeTV.setText(device.getDeviceType().toString());
            
            return convertView;
        }
        
    }
    
    @Override
    public void onClick(View v)
    {
        if (v == mConfigureBtn)
        {
            new AddEspButtonTask(this, mPermitAllCB.isChecked()).execute(mTempKey, mMac);
        }
    }
    
    private Runnable mTimeoutCounter = new Runnable()
    {
        
        @Override
        public void run()
        {
            mTimeoutTV.setText(getString(R.string.esp_espbutton_configure_counter, mTimeoutCount--));
            if (mTimeoutCount > 0)
            {
                mTimeoutTV.setVisibility(View.VISIBLE);
                mTimeoutTV.postDelayed(this, 1000);
            }
            else
            {
                mTimeoutTV.setVisibility(View.GONE);
            }
        }
    };
    
    /**
     * Start count down, show info bottom
     */
    private void startCountdown()
    {
        mTimeoutTV.removeCallbacks(mTimeoutCounter);
        mTimeoutCount = 60;
        mTimeoutTV.post(mTimeoutCounter);
    }
    
    /**
     * Stop count down, hide count down info
     */
    private void stopCountdown()
    {
        mTimeoutTV.removeCallbacks(mTimeoutCounter);
        mTimeoutTV.setVisibility(View.GONE);
    }
    
    private class AddEspButtonTask extends AsyncTask<String, Object, Boolean>
    {
        /**
         * The message pair broadcast complete, start pair progress
         */
        private static final String PAIR_BUTTON_CONFIGURE = "button_configure";
        /**
         * The message receive pair request
         */
        private static final String PAIR_REQUEST = "request";
        /**
         * The message receive pair result
         */
        private static final String PAIR_RESULT = "result";
        
        private Activity mActivity;
        
        final private boolean mPermitAll;
        
        private ProgressDialog mProgressDialog;
        private AlertDialog mPairRequestDialog;
        private AlertDialog mPairResultDialog;
        
        public AddEspButtonTask(Activity activity, boolean permitAll)
        {
            mActivity = activity;
            mPermitAll = permitAll;
            
            initDialog();
        }
        
        private void initDialog() {
            mProgressDialog = new ProgressDialog(mActivity);
            mProgressDialog.setMessage(getString(R.string.esp_espbutton_configure_progress));
            mProgressDialog.setCancelable(false);
            mProgressDialog.setButton(DialogInterface.BUTTON_NEGATIVE,
                getString(R.string.esp_espbutton_configure_exit),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mListener.interrupt();
                    }
                });

            mPairRequestDialog = new AlertDialog.Builder(mActivity).setCancelable(false).create();
            mPairResultDialog = new AlertDialog.Builder(mActivity).setCancelable(false).create();
        }
        
        @Override
        protected void onPreExecute()
        {
            mProgressDialog.show();
        }
        
        @Override
        protected Boolean doInBackground(String... params)
        {
            String tempKey = params[0];
            String macAddress = params[1];
            
            boolean isBroadcast = mDeviceAdapter.getCount() == 0;
            
            return mUser.doActionEspButtonAdd(tempKey, macAddress, mPermitAll, mDevices, isBroadcast, mListener);
        }
        
        @Override
        protected void onPostExecute(Boolean result) {
            dissmissAllDialog();

            if (!mListener.isInterrupted()) {
                int resultRes = result ? R.string.esp_espbutton_configure_result_suc
                    : R.string.esp_espbutton_configure_result_failed;
                String resultStr = getString(R.string.esp_espbutton_configure_result, "", getString(resultRes));
                Toast.makeText(mActivity, resultStr, Toast.LENGTH_SHORT).show();
            }
            mActivity.finish();
        }
        
        private void dissmissAllDialog()
        {
            if (mProgressDialog != null)
            {
                mProgressDialog.dismiss();
            }
            if (mPairRequestDialog != null)
            {
                mPairRequestDialog.dismiss();
            }
            if (mPairResultDialog != null)
            {
                mPairResultDialog.dismiss();
            }
        }
        
        @SuppressWarnings("unchecked")
        @Override
        protected void onProgressUpdate(Object... values)
        {
            dissmissAllDialog();
            
            String action = values[0].toString();
            if (action.equals(PAIR_BUTTON_CONFIGURE))
            {
                boolean broadcastSuc = (Boolean)values[1];
                if (broadcastSuc)
                {
                    mProgressDialog.setMessage(getString(R.string.esp_espbutton_configure_button_scan));
                    mProgressDialog.show();
                    startCountdown();
                }
            }
            else if (action.equals(PAIR_REQUEST))
            {
                String deviceMac = values[1].toString();
                String buttonMac = values[2].toString();
                Queue<String> queue = (Queue<String>)values[3];
                onPairRequest(deviceMac, buttonMac, queue);
            }
            else if (action.equals(PAIR_RESULT))
            {
                String deviceMac = values[1].toString();
                boolean suc = (Boolean)values[2];
                Queue<String> queue = (Queue<String>)values[3];
                onPairResult(deviceMac, suc, queue);
            }
        }
        
        /**
         * Call when receive pair request, show pair request dialog
         * 
         * @param deviceMac
         * @param buttonMac
         * @param queue
         */
        private void onPairRequest(String deviceMac, String buttonMac, final Queue<String> queue)
        {
            PairRequestDialogListener buttonListener = new PairRequestDialogListener(queue);
            mPairRequestDialog.setTitle(buttonMac);
            mPairRequestDialog.setMessage(getString(R.string.esp_espbutton_configure_request, deviceMac));
            mPairRequestDialog.setButton(DialogInterface.BUTTON_POSITIVE,
                getString(R.string.esp_espbutton_configure_permit),
                buttonListener);
            mPairRequestDialog.setButton(DialogInterface.BUTTON_NEUTRAL,
                getString(R.string.esp_espbutton_configure_forbid),
                buttonListener);
            mPairRequestDialog.setButton(DialogInterface.BUTTON_NEGATIVE,
                getString(R.string.esp_espbutton_configure_exit),
                buttonListener);
            mPairRequestDialog.show();
        }
        
        /**
         * Call when receive pair result, show pair result dialog
         * 
         * @param deviceMac
         * @param success
         * @param queue
         */
        private void onPairResult(String deviceMac, boolean success, final Queue<String> queue)
        {
            PairResultDialogListener buttonListener = new PairResultDialogListener(queue);
            int resultRes =
                success ? R.string.esp_espbutton_configure_result_suc : R.string.esp_espbutton_configure_result_failed;
            mPairResultDialog
                .setTitle(getString(R.string.esp_espbutton_configure_result, deviceMac, getString(resultRes)));
            mPairResultDialog.setMessage(getString(R.string.esp_espbutton_configure_continue));
            mPairResultDialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(android.R.string.ok), buttonListener);
            mPairResultDialog.setButton(DialogInterface.BUTTON_NEGATIVE,
                getString(R.string.esp_espbutton_configure_exit),
                buttonListener);
            mPairResultDialog.show();
        }
        
        private class ConfigureInteractive implements IEspButtonConfigureListener
        {
            private volatile boolean mInterrupted = false;
            
            @Override
            public void interrupt(){
                mInterrupted = true;
            }

            @Override
            public boolean isInterrupted() {
                return mInterrupted;
            }
            
            @Override
            public void onBroadcastComplete(IEspDevice rootDevice, boolean result)
            {
                publishProgress(PAIR_BUTTON_CONFIGURE, result);
            }
            
            @Override
            public void receivePairRequest(String deviceMac, String buttonMac, Queue<String> queue)
            {
                publishProgress(PAIR_REQUEST, deviceMac, buttonMac, queue);
            }

            @Override
            public void receivePairResult(String deviceMac, boolean success, Queue<String> queue)
            {
                publishProgress(PAIR_RESULT, deviceMac, success, queue);
            }
        }
        
        private ConfigureInteractive mListener = new ConfigureInteractive();
        
        private class PairRequestDialogListener implements DialogInterface.OnClickListener
        {
            private Queue<String> mQueue;
            
            public PairRequestDialogListener(Queue<String> queue)
            {
                mQueue = queue;
            }
            
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                switch (which)
                {
                    case DialogInterface.BUTTON_POSITIVE:
                        mQueue.add(IEspButtonConfigureListener.PAIR_PERMIT);
                        mProgressDialog.setMessage(getString(R.string.esp_espbutton_configure_pairing));
                        mProgressDialog.show();
                        break;
                    case DialogInterface.BUTTON_NEUTRAL:
                        mQueue.add(IEspButtonConfigureListener.PAIR_FORBID);
                        onPairResult("", false, mQueue);
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        mQueue.add(IEspButtonConfigureListener.PAIR_OVER);
                        mActivity.finish();
                        break;
                }
            }
            
        }
        
        private class PairResultDialogListener implements DialogInterface.OnClickListener
        {
            private Queue<String> mQueue;
            
            public PairResultDialogListener(Queue<String> queue)
            {
                mQueue = queue;
            }
            
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                switch (which)
                {
                    case DialogInterface.BUTTON_POSITIVE:
                        mQueue.add(IEspButtonConfigureListener.PAIR_CONTINUE);
                        mProgressDialog.setMessage(getString(R.string.esp_espbutton_configure_waiting));
                        mProgressDialog.show();
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        mQueue.add(IEspButtonConfigureListener.PAIR_OVER);
                        mActivity.finish();
                        break;
                }
            }
            
        }
    }
}
