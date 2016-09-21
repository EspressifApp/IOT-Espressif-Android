package com.espressif.iot.ui.main;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;

import java.util.ArrayList;
import java.util.List;

import com.espressif.iot.R;
import com.espressif.iot.type.device.EspDeviceType;
import com.espressif.iot.util.DeviceUtil;

import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class EspDrawerFragmentRight extends EspDrawerFragmentBase {

    private NavigationDrawerCallbacks mCallbacks;

    private DrawerLayout mDrawerLayout;
    private View mFragmentContainerView;

    private ListView mDrawerListView;
    private DeviceFilterAdapter mAdapter;
    private int mCurrentSelectedPosition;

    private CheckBox mUsableFilterCB;
    public static final int FILTER_USEABLE = -1;

    public EspDrawerFragmentRight() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Indicate that this fragment would like to influence the set of actions in the action bar.
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.main_drawer_right, container, false);

        mUsableFilterCB = (CheckBox)view.findViewById(R.id.device_usable_filter);
        mUsableFilterCB.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                checkUsableFilter(isChecked);
            }
        });

        mDrawerListView = (ListView)view.findViewById(R.id.list);
        mDrawerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectItem(position);
            }
        });
        mAdapter = new DeviceFilterAdapter(getActivity());
        mDrawerListView.setAdapter(mAdapter);
        mDrawerListView.setItemChecked(0, true);
        mCurrentSelectedPosition = 0;

        return view;
    }

    public boolean isDrawerOpen() {
        return mDrawerLayout != null && mDrawerLayout.isDrawerOpen(mFragmentContainerView);
    }

    /**
     * Users of this fragment must call this method to set up the navigation drawer interactions.
     *
     * @param fragmentId The android:id of this fragment in its activity's layout.
     * @param drawerLayout The DrawerLayout containing this fragment's UI.
     */
    public void setUp(int fragmentId, DrawerLayout drawerLayout) {
        mFragmentContainerView = getActivity().findViewById(fragmentId);
        mDrawerLayout = drawerLayout;

        // set a custom shadow that overlays the main content when the drawer opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        // set up the drawer's list view with items and click listener
    }

    private void selectItem(int position) {
        mCurrentSelectedPosition = position;
        if (mDrawerListView != null) {
            mDrawerListView.setItemChecked(position, true);
        }
        if (mDrawerLayout != null) {
            mDrawerLayout.closeDrawer(mFragmentContainerView);
        }
        if (mCallbacks != null) {
            mCallbacks.onNavigationDrawerItemSelected(this, position);
        }
        mAdapter.notifyDataSetChanged();
    }

    private void checkUsableFilter(boolean filter) {
        if (mDrawerLayout != null) {
            mDrawerLayout.closeDrawer(mFragmentContainerView);
        }
        if (mCallbacks != null) {
            mCallbacks.onNavigationDrawerItemSelected(this, FILTER_USEABLE);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallbacks = (NavigationDrawerCallbacks)activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement NavigationDrawerCallbacks.");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    private class DeviceFilterAdapter extends BaseAdapter {
        private Context mContext;

        private List<EspDeviceType> mTypes;

        public DeviceFilterAdapter(Context context) {
            mContext = context;
            mTypes = new ArrayList<EspDeviceType>();
            EspDeviceType[] values = EspDeviceType.values();
            mTypes.add(null);
            for (EspDeviceType type : values) {
                if (type != EspDeviceType.NEW && type != EspDeviceType.ROOT && type != EspDeviceType.REMOTE
                    && type != EspDeviceType.FLAMMABLE && type != EspDeviceType.VOLTAGE) {
                    mTypes.add(type);
                }
            }
        }

        @Override
        public int getCount() {
            return mTypes.size();
        }

        @Override
        public EspDeviceType getItem(int position) {
            return mTypes.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view;
            if (convertView == null) {
                view = View.inflate(mContext, R.layout.main_drawer_right_item, null);
            } else {
                view = convertView;
            }

            if (position == mCurrentSelectedPosition) {
                view.setBackgroundColor(getResources().getColor(R.color.esp_main_drawer_right_item_checked));
            } else {
                view.setBackgroundColor(Color.WHITE);
            }

            EspDeviceType type = mTypes.get(position);
            TextView text = (TextView)view.findViewById(R.id.text);
            ImageView icon = (ImageView)view.findViewById(R.id.icon);

            if (type == null) {
                text.setText(R.string.esp_main_all_type);
                icon.setImageResource(R.drawable.device_filter_icon_all);
            } else {
                text.setText(DeviceUtil.getDeviceTypeNameRes(type));
                icon.setImageResource(DeviceUtil.getDeviceIconRes(type));
            }

            return view;
        }

    }

    public EspDeviceType getCheckedDeviceType() {
        return mAdapter.getItem(mCurrentSelectedPosition);
    }

    public boolean isFilterDeviceUsable() {
        return mUsableFilterCB.isChecked();
    }
}
