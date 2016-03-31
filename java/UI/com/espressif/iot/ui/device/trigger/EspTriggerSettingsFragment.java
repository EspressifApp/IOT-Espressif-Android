package com.espressif.iot.ui.device.trigger;

import java.util.List;

import org.apache.log4j.Logger;

import com.espressif.iot.R;
import com.espressif.iot.device.IEspDevice;
import com.espressif.iot.type.device.trigger.EspDeviceTrigger;
import com.espressif.iot.type.device.trigger.EspDeviceTrigger.TriggerRule;
import com.espressif.iot.user.IEspUser;
import com.espressif.iot.user.builder.BEspUser;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.Toast;

public class EspTriggerSettingsFragment extends Fragment
    implements OnItemClickListener, OnClickListener, OnItemSelectedListener {
    private final Logger log = Logger.getLogger(getClass());

    private IEspUser mUser;
    private DeviceTriggerActivity mActivity;
    private EspDeviceTrigger mTrigger;
    private IEspDevice mDevice;

    private Spinner mCompareTypeSpinner;
    private String[] mCompareTypeArray;

    private EditText mCompareValueET;

    private ListView mRuleListView;
    private List<TriggerRule> mRuleList;
    private BaseAdapter mRuleAdapter;

    private Button mAddNotificationBtn;

    private Button mSaveChangeBtn;

    private static final int MENU_ID_DELETE_RULE = 1;

    private AlertDialog mAddRuleDialog;
    private Spinner mViaSpinner;
    private String[] VIA_ARRAY = new String[] {EspDeviceTrigger.VIA_APP, EspDeviceTrigger.VIA_EMAIL};

    private boolean mChangeSaved = true;

    public void setTrigger(EspDeviceTrigger trigger) {
        mTrigger = trigger;
    }

    public void setDevice(IEspDevice device) {
        mDevice = device;
    }

    public boolean hasSavedChanges() {
        return mChangeSaved;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        mActivity = (DeviceTriggerActivity)activity;
        mCompareTypeArray = DeviceTriggerActivity.COMPARE_TYPE_ARRAY;

        mUser = BEspUser.getBuilder().getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.device_trigger_settings_fragment, container, false);

        mCompareTypeSpinner = (Spinner)view.findViewById(R.id.trigger_compare_type);
        ArrayAdapter<String> compareTypeAdapter =
            new ArrayAdapter<String>(mActivity, android.R.layout.simple_spinner_item, mCompareTypeArray);
        compareTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mCompareTypeSpinner.setAdapter(compareTypeAdapter);
        mCompareTypeSpinner.setSelection(mTrigger.getCompareType());
        mCompareTypeSpinner.setOnItemSelectedListener(this);

        mCompareValueET = (EditText)view.findViewById(R.id.trigger_compare_value);
        mCompareValueET.setText("" + mTrigger.getCompareValue());
        mCompareValueET.addTextChangedListener(mCompareValueTextWatcher);

        mRuleListView = (ListView)view.findViewById(R.id.trigger_rule_list);
        mRuleList = mTrigger.getTriggerRules();
        mRuleAdapter = new ArrayAdapter<TriggerRule>(mActivity, android.R.layout.simple_list_item_1, mRuleList);
        mRuleListView.setAdapter(mRuleAdapter);
        mRuleListView.setOnItemClickListener(this);

        mAddNotificationBtn = (Button)view.findViewById(R.id.trigger_add_notification);
        mAddNotificationBtn.setOnClickListener(this);

        mSaveChangeBtn = (Button)view.findViewById(R.id.trigger_save_btn);
        mSaveChangeBtn.setOnClickListener(this);

        mViaSpinner = new Spinner(mActivity);
        ArrayAdapter<String> viaAdapter =
            new ArrayAdapter<String>(mActivity, android.R.layout.simple_list_item_1, VIA_ARRAY);
        mViaSpinner.setAdapter(viaAdapter);
        mAddRuleDialog = new AlertDialog.Builder(mActivity).setView(mViaSpinner)
            .setPositiveButton(android.R.string.ok, mAddRuleClickListenser)
            .create();

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        mActivity.notifyDataSetChanged();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (parent == mRuleListView) {
            final TriggerRule rule = mRuleList.get(position);
            PopupMenu popupMenu = new PopupMenu(mActivity, view);
            Menu menu = popupMenu.getMenu();
            menu.add(Menu.NONE, MENU_ID_DELETE_RULE, 0, R.string.esp_device_trigger_settings_delete_notification);
            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    switch (item.getItemId()) {
                        case MENU_ID_DELETE_RULE:
                            mRuleList.remove(rule);
                            mRuleAdapter.notifyDataSetChanged();
                            onTriggerModifyed(false);
                            return true;
                    }
                    return false;
                }
            });
            popupMenu.show();
        }
    }

    @Override
    public void onClick(View v) {
        if (v == mAddNotificationBtn) {
            mViaSpinner.setSelection(0);
            mAddRuleDialog.show();
        } else if (v == mSaveChangeBtn) {
            new UpdateTriggerTask(mActivity, generateTaskTrigger()).execute();
        }
    }

    /**
     * Create a temp trigger for update task
     * 
     * @return
     */
    private EspDeviceTrigger generateTaskTrigger() {
        EspDeviceTrigger taskTrigger = new EspDeviceTrigger();
        taskTrigger.setId(mTrigger.getId());
        taskTrigger.setName(mTrigger.getName());
        taskTrigger.setDimension(mTrigger.getDimension());
        taskTrigger.setStreamType(mTrigger.getStreamType());
        taskTrigger.setInterval(mTrigger.getInterval());
        taskTrigger.setIntervalFunc(mTrigger.getIntervalFunc());
        taskTrigger.setCompareType(mCompareTypeSpinner.getSelectedItemPosition());
        String compareValeStr = mCompareValueET.getText().toString();
        int compareValue = TextUtils.isEmpty(compareValeStr) ? 0 : Integer.parseInt(compareValeStr);
        taskTrigger.setCompareValue(compareValue);

        for (TriggerRule rule : mRuleList) {
            TriggerRule taskRule = new TriggerRule();
            taskRule.setScope(rule.getScope());
            List<String> vias = rule.getViaList();
            for (String via : vias) {
                taskRule.addVia(via);
            }

            taskTrigger.addTriggerRule(taskRule);
        }

        return taskTrigger;
    }

    /**
     * When modify the trigger or save the changes
     * 
     * @param saved
     */
    private void onTriggerModifyed(boolean saved) {
        mChangeSaved = saved;

        mSaveChangeBtn.setEnabled(!saved);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        checkCompareChanged();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    private TextWatcher mCompareValueTextWatcher = new TextWatcher() {

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            checkCompareChanged();
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    };

    private void checkCompareChanged() {
        String compareValueStr = mCompareValueET.getText().toString();
        if (TextUtils.isEmpty(compareValueStr)) {
            mSaveChangeBtn.setEnabled(false);
        } else {
            int selectedType = mCompareTypeSpinner.getSelectedItemPosition();
            int compareValue = Integer.parseInt(compareValueStr);
            boolean comapreChanged =
                (selectedType != mTrigger.getCompareType() || compareValue != mTrigger.getCompareValue());
            onTriggerModifyed(!comapreChanged);
        }
    }

    private DialogInterface.OnClickListener mAddRuleClickListenser = new DialogInterface.OnClickListener() {

        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    String via = mViaSpinner.getSelectedItem().toString();
                    String scope = EspDeviceTrigger.SCOPE_ME;
                    TriggerRule rule = new TriggerRule();
                    rule.setScope(scope);
                    rule.addVia(via);
                    mRuleList.add(rule);
                    mRuleAdapter.notifyDataSetChanged();
                    onTriggerModifyed(false);

                    break;
            }
        }
    };

    private class UpdateTriggerTask extends ProgressDialogTask<Void, Void, Boolean> {
        private EspDeviceTrigger mTaskTrigger;

        public UpdateTriggerTask(Activity activity, EspDeviceTrigger taskTrigger) {
            super(activity);

            mTaskTrigger = taskTrigger;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            log.debug("execute UpdateTriggerTask");
            return mUser.doActionDeviceTriggerUpdate(mDevice, mTaskTrigger);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);

            log.debug("UpdateTriggerTask result = " + result);
            if (result) {
                mTrigger.setCompareType(mTaskTrigger.getCompareType());
                mTrigger.setCompareValue(mTaskTrigger.getCompareValue());
                mTrigger.updateTriggerRuleList(mTaskTrigger.getTriggerRules());
                onTriggerModifyed(true);

                Toast.makeText(mContext,
                    mContext.getString(R.string.esp_device_trigger_settings_save_suc),
                    Toast.LENGTH_SHORT).show();
            } else {
                Toast
                    .makeText(mContext,
                        mContext.getString(R.string.esp_device_trigger_settings_save_failed),
                        Toast.LENGTH_SHORT)
                    .show();
            }
        }
    }
}
