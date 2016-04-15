package com.espressif.iot.ui.device.trigger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.log4j.Logger;

import com.espressif.iot.R;
import com.espressif.iot.device.IEspDevice;
import com.espressif.iot.type.device.trigger.EspDeviceTrigger;
import com.espressif.iot.type.device.trigger.EspDeviceTrigger.TriggerRule;
import com.espressif.iot.ui.main.EspActivityAbs;
import com.espressif.iot.user.IEspUser;
import com.espressif.iot.user.builder.BEspUser;
import com.espressif.iot.util.EspStrings;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class DeviceTriggerActivity extends EspActivityAbs implements OnItemClickListener, OnItemLongClickListener {
    private final Logger log = Logger.getLogger(getClass());

    public static final String[] COMPARE_TYPE_ARRAY = new String[] {"=", "!=", ">", ">=", "<", "<="};

    private IEspUser mUser;
    private IEspDevice mDevice;

    private ListView mTriggerListView;
    private List<EspDeviceTrigger> mTriggerList;
    private BaseAdapter mTriggerAdapter;

    private FragmentManager mFragmentManager;
    private static final String TAG_FRAGMENT = "Settings";

    private static final int MENU_ID_TRIGGER_DELETE = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.device_trigger_activity);

        setTitle(R.string.esp_device_trigger);
        setTitleRightIcon(R.drawable.esp_menu_icon_add);

        Intent intent = getIntent();
        String deviceKey = intent.getStringExtra(EspStrings.Key.DEVICE_KEY_KEY);
        mUser = BEspUser.getBuilder().getInstance();
        mDevice = mUser.getUserDevice(deviceKey);

        mTriggerListView = (ListView)findViewById(R.id.device_trigger_list);
        mTriggerList = new ArrayList<EspDeviceTrigger>();
        mTriggerAdapter = new TriggerAdapter(this);
        mTriggerListView.setAdapter(mTriggerAdapter);
        mTriggerListView.setOnItemClickListener(this);
        mTriggerListView.setOnItemLongClickListener(this);

        mFragmentManager = getFragmentManager();

        new GetTriggerTask(this).execute();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        EspDeviceTrigger trigger = mTriggerList.get(position);
        EspTriggerSettingsFragment fragment = new EspTriggerSettingsFragment();
        fragment.setTrigger(trigger);
        fragment.setDevice(mDevice);

        mFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment, TAG_FRAGMENT)
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            .addToBackStack(null)
            .commit();
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        EspDeviceTrigger trigger = mTriggerList.get(position);

        PopupMenu popup = new PopupMenu(this, view);
        Menu menu = popup.getMenu();
        menu.add(0, MENU_ID_TRIGGER_DELETE, 0, R.string.esp_device_trigger_delete);
        popup.setOnMenuItemClickListener(new MenuItemClickListener(trigger));
        popup.show();

        return true;
    }

    @Override
    protected void onTitleRightIconClick(View rightIcon) {
        View view = View.inflate(this, R.layout.device_trigger_add_dialog, null);
        new AlertDialog.Builder(this).setTitle(R.string.esp_device_trigger_add)
            .setView(view)
            .setPositiveButton(android.R.string.ok, new AddTriggerListener(view))
            .show();
    }

    private class MenuItemClickListener implements PopupMenu.OnMenuItemClickListener {
        private EspDeviceTrigger mTrigger;

        public MenuItemClickListener(EspDeviceTrigger trigger) {
            mTrigger = trigger;
        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            switch (item.getItemId()) {
                case MENU_ID_TRIGGER_DELETE:
                    new DeleteTriggerTask(DeviceTriggerActivity.this, mTrigger).execute();
                    return true;
            }
            return false;
        }

    }

    private class AddTriggerListener implements DialogInterface.OnClickListener {
        private View mView;

        public AddTriggerListener(View contentView) {
            mView = contentView;
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            Activity activity = DeviceTriggerActivity.this;
            String name = ((EditText)mView.findViewById(R.id.trigger_name_edit)).getText().toString();
            if (TextUtils.isEmpty(name)) {
                Toast.makeText(activity, R.string.esp_device_trigger_name_hint, Toast.LENGTH_SHORT).show();
            }
            int dimension = ((Spinner)mView.findViewById(R.id.trigger_dimension_spinner)).getSelectedItemPosition();

            new CreateTriggerTask(activity, name, dimension).execute();
        }
    }

    private class TriggerAdapter extends BaseAdapter {
        private Activity mActivity;

        public TriggerAdapter(Activity activity) {
            mActivity = activity;
        }

        @Override
        public int getCount() {
            return mTriggerList.size();
        }

        @Override
        public Object getItem(int position) {
            return mTriggerList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return mTriggerList.get(position).getId();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = View.inflate(mActivity, android.R.layout.simple_list_item_2, null);
            }

            EspDeviceTrigger trigger = mTriggerList.get(position);

            TextView tv1 = (TextView)convertView.findViewById(android.R.id.text1);
            tv1.setText(trigger.getName());
            TextView tv2 = (TextView)convertView.findViewById(android.R.id.text2);
            List<TriggerRule> rules = trigger.getTriggerRules();
            if (!rules.isEmpty()) {
                TriggerRule rule = rules.get(0);

                StringBuilder ruleStr = new StringBuilder();
                ruleStr.append(COMPARE_TYPE_ARRAY[trigger.getCompareType()]).append(" ");
                ruleStr.append(trigger.getCompareValue()).append(", ");
                ruleStr.append(rule.toString());
                if (rules.size() > 1) {
                    ruleStr.append("...");
                }

                tv2.setText(ruleStr);
            } else {
                tv2.setText("");
            }

            return convertView;
        }
    }

    /**
     * Sort by dimension
     */
    private Comparator<EspDeviceTrigger> mTriggerComparator = new Comparator<EspDeviceTrigger>() {
        @Override
        public int compare(EspDeviceTrigger lhs, EspDeviceTrigger rhs) {
            Integer l = lhs.getDimension();
            Integer r = rhs.getDimension();
            return l.compareTo(r);
        }
    };

    private class GetTriggerTask extends ProgressDialogTask<Void, Void, List<EspDeviceTrigger>> {

        public GetTriggerTask(Activity activity) {
            super(activity);
        }

        @Override
        protected List<EspDeviceTrigger> doInBackground(Void... params) {
            log.debug("execute GetTriggerTask");
            List<EspDeviceTrigger> result = mUser.doActionDeviceTriggerGet(mDevice);
            Collections.sort(result, mTriggerComparator);

            return result;
        }

        @Override
        protected void onPostExecute(List<EspDeviceTrigger> result) {
            super.onPostExecute(result);

            if (result == null) {
                log.info("GetTriggerTask result null");
                Toast.makeText(mContext, R.string.esp_device_trigger_get_failed, Toast.LENGTH_LONG).show();
            } else {
                log.info("GetTriggerTask result size = " + result.size());
                mTriggerList.addAll(result);
                mTriggerAdapter.notifyDataSetChanged();
            }
        }
    }

    private class CreateTriggerTask extends ProgressDialogTask<Void, Void,  EspDeviceTrigger> {
        private String mName;
        private int mDimension;

        public CreateTriggerTask(Activity activity, String name, int dimension) {
            super(activity);

            mName = name;
            mDimension = dimension;
        }

        @Override
        protected EspDeviceTrigger doInBackground(Void... params) {
            log.debug("execute CreateTriggerTask");
            EspDeviceTrigger trigger = new EspDeviceTrigger();
            trigger.setDimension(mDimension);
            trigger.setName(mName);
            trigger.setStreamType(EspDeviceTrigger.STREAM_ALARM);
            trigger.setInterval(0);
            trigger.setIntervalFunc(EspDeviceTrigger.INTERVAL_FUNC_AVG);
            trigger.setCompareType(EspDeviceTrigger.COMPARE_TYPE_EQ);
            trigger.setCompareValue(0);

            long id = mUser.doActionDeviceTriggerCreate(mDevice, trigger);
            log.info("create dimension " + mDimension + " result id = " + id);
            if (id > 0) {
                trigger.setId(id);
                return trigger;
            } else {
                return null;
            }
        }

        @Override
        protected void onPostExecute(EspDeviceTrigger result) {
            super.onPostExecute(result);

            if (result != null) {
                mTriggerList.add(result);
                mTriggerAdapter.notifyDataSetChanged();
            }
        }
    }

    private class DeleteTriggerTask extends ProgressDialogTask<Void, Void, Boolean> {
        private EspDeviceTrigger mTrigger;

        public DeleteTriggerTask(Activity activity, EspDeviceTrigger trigger) {
            super(activity);

            mTrigger = trigger;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            return mUser.doActionDeviceTriggerDelete(mDevice, mTrigger);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);

            if (result) {
                mTriggerList.remove(mTrigger);
                mTriggerAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void onBackPressed() {
        /*
         * if find fragment and the fragment hasn't saved it's changes, hint user
         */
        Fragment fragment = mFragmentManager.findFragmentByTag(TAG_FRAGMENT);
        if (fragment != null) {
            if (!((EspTriggerSettingsFragment)fragment).hasSavedChanges()) {
                new AlertDialog.Builder(this).setMessage(R.string.esp_device_trigger_settings_back_msg)
                    .setPositiveButton(R.string.esp_device_trigger_settings_back_exit,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                superBack();
                            }
                        })
                    .show();

                return;
            }
        }

        super.onBackPressed();
    }

    private void superBack() {
        super.onBackPressed();
    }

    public void notifyDataSetChanged() {
        mTriggerAdapter.notifyDataSetChanged();
    }
}
