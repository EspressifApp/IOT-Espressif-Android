package com.espressif.iot.ui.device;

import com.espressif.iot.R;
import com.espressif.iot.help.ui.IEspHelpUIUseRemote;
import com.espressif.iot.type.device.status.EspStatusRemote;
import com.espressif.iot.type.device.status.IEspStatusRemote;
import com.espressif.iot.type.help.HelpStepUseRemote;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

public class DeviceRemoteActivity extends DeviceActivityAbs implements OnClickListener, IEspHelpUIUseRemote
{
    private EditText mAddressEdt;
    private EditText mCommandEdt;
    private EditText mRepeatEdt;
    
    private Button mConfirmBtn;
    
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        boolean compatibility = isDeviceCompatibility();
        if (mHelpMachine.isHelpModeUseRemote())
        {
            mHelpMachine.transformState(compatibility);
            onHelpUseRemote();
        }
    }
    
    @Override
    protected View initControlView()
    {
        View view = getLayoutInflater().inflate(R.layout.device_activity_remote, null);
        
        mAddressEdt = (EditText)view.findViewById(R.id.remote_address_edit);
        mCommandEdt = (EditText)view.findViewById(R.id.remote_command_edit);
        mRepeatEdt = (EditText)view.findViewById(R.id.remote_repeat_edit);
        
        mConfirmBtn = (Button)view.findViewById(R.id.remote_confirm_btn);
        mConfirmBtn.setOnClickListener(this);
        
        return view;
    }
    
    @Override
    protected void executePrepare()
    {
    }
    
    @Override
    protected void executeFinish(int command, boolean result)
    {
        int msgRes = result ? R.string.esp_device_remote_post_success : R.string.esp_device_remote_post_failed;
        Toast.makeText(this, msgRes, Toast.LENGTH_LONG).show();
        
        if (mHelpMachine.isHelpModeUseRemote() && command == COMMAND_POST)
        {
            mHelpMachine.transformState(result);
            onHelpUseRemote();
        }
    }
    
    @Override
    public void onClick(View v)
    {
        String address = mAddressEdt.getText().toString();
        if (TextUtils.isEmpty(address))
        {
            Toast.makeText(this, R.string.esp_device_remote_address_hint, Toast.LENGTH_LONG).show();
            return;
        }
        
        String command = mCommandEdt.getText().toString();
        if (TextUtils.isEmpty(command))
        {
            Toast.makeText(this, R.string.esp_device_remote_command_hint, Toast.LENGTH_LONG).show();
            return;
        }
        
        String repeat = mRepeatEdt.getText().toString();
        if (TextUtils.isEmpty(repeat))
        {
            Toast.makeText(this, R.string.esp_device_remote_repeat_hint, Toast.LENGTH_LONG).show();
            return;
        }
        
        IEspStatusRemote status = new EspStatusRemote();
        status.setAddress(Integer.parseInt(address));
        status.setCommand(Integer.parseInt(command));
        status.setRepeat(Integer.parseInt(repeat));
        
        executePost(status);
    }
    
    @Override
    public void onHelpUseRemote()
    {
        clearHelpContainer();
        
        HelpStepUseRemote step = HelpStepUseRemote.valueOf(mHelpMachine.getCurrentStateOrdinal());
        switch (step)
        {
            case START_USE_HELP:
                break;
            case FAIL_FOUND_REMOTE:
                break;
            case FIND_ONLINE:
                break;
            case NO_REMOTE_ONLINE:
                break;
            case REMOTE_SELECT:
                break;
            
            case REMOTE_NOT_COMPATIBILITY:
                mHelpMachine.exit();
                setResult(RESULT_EXIT_HELP_MODE);
                break;
            case REMOTE_CONTROL:
                highlightHelpView(findViewById(R.id.remote_layout));
                setHelpHintMessage(R.string.esp_help_use_remote_control_msg);
                break;
            case REMOTE_CONTROL_FAILED:
                highlightHelpView(findViewById(R.id.remote_layout));
                setHelpHintMessage(R.string.esp_help_use_remote_control_failed_msg);
                mHelpMachine.retry();
                break;
            case SUC:
                setHelpFrameDark();
                setHelpHintMessage(R.string.esp_help_use_remote_success_msg);
                setHelpButtonVisible(HELP_BUTTON_EXIT, true);
                break;
        }
    }
    
}
