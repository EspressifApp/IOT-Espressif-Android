package com.espressif.iot.ui.configure;

import java.util.ArrayList;
import java.util.List;

import com.espressif.iot.R;
import com.espressif.iot.action.device.mesh.EspActionMeshDeviceConfigureLocal;
import com.espressif.iot.action.device.mesh.IEspActionMeshDeviceConfigureLocal.MeshDeviceConfigureLocalResult;
import com.espressif.iot.base.api.EspBaseApiUtil;
import com.espressif.iot.command.device.mesh.IEspCommandMeshConfigureLocal.MeshMode;
import com.espressif.iot.device.IEspDeviceNew;
import com.espressif.iot.ui.view.WifiAdapter;
import com.espressif.iot.util.BSSIDUtil;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.os.Handler;
import android.os.Message;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class DeviceMeshConfigureDialog implements OnCheckedChangeListener, OnItemSelectedListener, OnClickListener,
    OnCancelListener, OnDismissListener
{
    private static final String[] ALL_MESH_VALUES = {MeshMode.MESH_OFF.toString(), MeshMode.MESH_LOCAL.toString(),
        MeshMode.MESH_ONLINE.toString()};
    
    private static final int POSITION_MESH_OFF = 0;
    private static final int POSITION_MESH_LOCAL = 1;
    private static final int POSITION_MESH_ONLINE = 2;
    
    private Context mContext;
    
    private IEspDeviceNew mDevice;
    
    private View mWifiContent;
    private Spinner mWifiSpinner;
    private WifiAdapter mWifiAdapter;
    private EditText mPwdEdT;
    private CheckBox mPwdShowCB;
    
    private Spinner mMeshSpinner;
    
    private AlertDialog mSettingsDialog;
    private ProgressDialog mProgressDialog;
    
    private Handler mHandler;
    
    private List<String> mMeshValues;
    
    public DeviceMeshConfigureDialog(Context context, IEspDeviceNew device)
    {
        this(context, device, false);
    }
    
    public DeviceMeshConfigureDialog(Context context, IEspDeviceNew device, boolean isActivated)
    {
        mContext = context;
        mDevice = device;
        
        mProgressDialog = new ProgressDialog(mContext);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setMessage(mContext.getString(R.string.esp_configure_configuring));
        mProgressDialog.setOnDismissListener(this);
        
        mHandler = new DialogHandler(mProgressDialog);
        
        mMeshValues = new ArrayList<String>();
        mMeshValues.add(ALL_MESH_VALUES[0]);
        mMeshValues.add(ALL_MESH_VALUES[1]);
        if (isActivated)
        {
            mMeshValues.add(ALL_MESH_VALUES[2]);
        }
    }
    
    public Context getContext()
    {
        return mContext;
    }
    
    public void show()
    {
        stopAutoRefresh();
        
        LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.device_configure_select_dialog, null);
        
        mWifiContent = view.findViewById(R.id.device_configrue_content);
        mWifiSpinner = (Spinner)view.findViewById(R.id.wifi_spinner);
        mWifiAdapter = new WifiAdapter(mContext, EspBaseApiUtil.scan());
        mWifiAdapter.addFilter(BSSIDUtil.restoreSoftApBSSID(mDevice.getBssid()));
        mWifiSpinner.setAdapter(mWifiAdapter);
        TextView hintTV = (TextView)view.findViewById(R.id.select_hint);
        hintTV.setText(R.string.esp_configure_select_parent_node);
        
        mPwdEdT = (EditText)view.findViewById(R.id.wifi_password);
        mPwdShowCB = (CheckBox)view.findViewById(R.id.wifi_show_password_check);
        mPwdShowCB.setOnCheckedChangeListener(this);
        
        mMeshSpinner = (Spinner)view.findViewById(R.id.mesh_configure);
        mMeshSpinner.setAdapter(new ArrayAdapter<String>(mContext, android.R.layout.simple_list_item_1,
            android.R.id.text1, mMeshValues));
        mMeshSpinner.setOnItemSelectedListener(this);
        mMeshSpinner.setVisibility(View.VISIBLE);
        
        mSettingsDialog =
            new AlertDialog.Builder(mContext).setTitle(mDevice.getName())
                .setView(view)
                .setPositiveButton(android.R.string.ok, this)
                .setOnCancelListener(this)
                .show();
    }
    
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
    {
        if (buttonView == mPwdShowCB)
        {
            int inputType =
                isChecked ? (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD)
                    : (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            mPwdEdT.setInputType(inputType);
        }
    }
    
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
    {
        if (parent == mMeshSpinner)
        {
            onMeshItemSelected(position);
        }
    }
    
    private void onMeshItemSelected(int position)
    {
        switch (position)
        {
            case POSITION_MESH_ONLINE:
                mWifiContent.setVisibility(View.VISIBLE);
                break;
            case POSITION_MESH_LOCAL:
            case POSITION_MESH_OFF:
                mWifiContent.setVisibility(View.GONE);
                break;
        }
    }
    
    @Override
    public void onNothingSelected(AdapterView<?> parent)
    {
    }

    @Override
    public void onClick(DialogInterface dialog, int which)
    {
        if (dialog == mSettingsDialog)
        {
            mProgressDialog.show();
            new Thread() {
                
                public void run() {
                    EspActionMeshDeviceConfigureLocal action = new EspActionMeshDeviceConfigureLocal();
                    MeshDeviceConfigureLocalResult result = action.doActionMeshDeviceConfigureLocal(mDevice,
                        getMeshMode(mMeshSpinner.getSelectedItemPosition()),
                        mWifiSpinner.getSelectedItem().toString(),
                        mPwdEdT.getText().toString(),
                        null);
                    int msg = -1;
                    switch(result)
                    {
                        case FAIL:
                            msg = MeshDeviceConfigureLocalResult.FAIL.ordinal();
                            break;
                        case SUC:
                            msg = MeshDeviceConfigureLocalResult.SUC.ordinal();
                            break;
                    }
                    mHandler.sendEmptyMessage(msg);
                }
            }.start();
        }
    }
    
    private MeshMode getMeshMode(int position)
    {
        switch(position)
        {
            case POSITION_MESH_ONLINE:
                return MeshMode.MESH_ONLINE;
            case POSITION_MESH_LOCAL:
                return MeshMode.MESH_LOCAL;
            case POSITION_MESH_OFF:
                return MeshMode.MESH_OFF;
            default:
                return null;
        }
    }
    
    private static class DialogHandler extends Handler
    {
        private AlertDialog mDialog;
        
        public DialogHandler(AlertDialog dialog)
        {
            mDialog = dialog;
        }
        
        @Override
        public void handleMessage(Message msg)
        {
            if (mDialog != null)
            {
                if(msg.what==MeshDeviceConfigureLocalResult.SUC.ordinal()){
                    Toast.makeText(mDialog.getContext(), R.string.esp_configure_result_success, Toast.LENGTH_LONG).show();
                }
                else if(msg.what==MeshDeviceConfigureLocalResult.FAIL.ordinal()){
                    Toast.makeText(mDialog.getContext(), R.string.esp_configure_result_failed, Toast.LENGTH_LONG).show();
                }
                mDialog.dismiss();
            }
        }
    }
    
    private void stopAutoRefresh()
    {
        if (mContext instanceof DeviceConfigureActivity)
        {
            DeviceConfigureActivity activity = (DeviceConfigureActivity) mContext;
            activity.setIsShowConfigureDialog(true);
            activity.removeRefreshMessage();
        }
    }
    
    private void resetAutoRefresh()
    {
        if (mContext instanceof DeviceConfigureActivity)
        {
            DeviceConfigureActivity activity = (DeviceConfigureActivity) mContext;
            activity.setIsShowConfigureDialog(false);
            activity.resetRefreshMessage();
        }
    }

    @Override
    public void onCancel(DialogInterface dialog)
    {
        resetAutoRefresh();
    }

    @Override
    public void onDismiss(DialogInterface dialog)
    {
        resetAutoRefresh();
        
        checkHelpDissmissDialog();
    }
    
    protected void checkHelpDissmissDialog()
    {
    }
}
