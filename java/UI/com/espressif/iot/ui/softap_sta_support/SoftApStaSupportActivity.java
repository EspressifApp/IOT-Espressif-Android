package com.espressif.iot.ui.softap_sta_support;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.espressif.iot.R;
import com.espressif.iot.base.api.EspBaseApiUtil;
import com.espressif.iot.command.device.esptouch.EspCommandDeviceEsptouch;
import com.espressif.iot.db.IOTApDBManager;
import com.espressif.iot.db.greenrobot.daos.ApDB;
import com.espressif.iot.ui.main.EspActivityAbs;
import com.espressif.iot.ui.view.EspViewPager;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v13.app.FragmentPagerAdapter;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ImageView.ScaleType;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Toast;

public class SoftApStaSupportActivity extends EspActivityAbs implements OnCheckedChangeListener
{
    private SectionsPagerAdapter mSectionsPagerAdapter;
    
    protected EspViewPager mViewPager;
    private RadioGroup mPagerTabs;
    
    public final int FRAGMENT_DEVICE = 0;
    public final int FRAGMENT_CONFIGURE = 1;
    
    protected List<Fragment> mFragmentList;
    protected SSSFragmentDevices mFragmentDevice;
    protected SSSFragmentConfigure mFragmentConfig;
    
    private SSSHandler mHandler;
    
    private IOTApDBManager mIOTApDBManager;
    
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.softap_sta_layout);
        
        mFragmentList = new ArrayList<Fragment>();
        initPagerFragments();
        mFragmentList.add(mFragmentDevice);
        mFragmentList.add(mFragmentConfig);
        
        mSectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager());
        
        mViewPager = (EspViewPager)findViewById(R.id.pager);
        mViewPager.setInterceptTouchEvent(true);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener()
        {
            @Override
            public void onPageSelected(int position)
            {
                switch(position)
                {
                    case FRAGMENT_DEVICE:
                        mPagerTabs.check(R.id.pager_tab_devices);
                        break;
                    case FRAGMENT_CONFIGURE:
                        mPagerTabs.check(R.id.pager_tab_configure);
                        
                        checkHelpPagerConfigureSelected();
                        break;
                }
            }
        });
        
        mPagerTabs = (RadioGroup)findViewById(R.id.pager_tabs);
        mPagerTabs.setOnCheckedChangeListener(this);
        mPagerTabs.check(R.id.pager_tab_devices);
        
        ImageView esptouch = new ImageView(this);
        ViewGroup.LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        esptouch.setLayoutParams(lp);
        esptouch.setScaleType(ScaleType.CENTER_INSIDE);
        esptouch.setBackgroundResource(R.drawable.esp_activity_icon_background);
        esptouch.setImageResource(R.drawable.esp_icon_add);
        esptouch.setContentDescription("EspTouch");
        esptouch.setOnClickListener(mEsptouchClickListener);
        esptouch.setOnLongClickListener(mEspTouchLongClickistener);
        setTitleContentView(esptouch);
        
        mHandler = new SSSHandler(this);
        mIOTApDBManager = IOTApDBManager.getInstance();
    }
    
    protected void initPagerFragments()
    {
        mFragmentDevice = new SSSFragmentDevices();
        mFragmentConfig = new SSSFragmentConfigure();
    }
    
    private View.OnClickListener mEsptouchClickListener = new View.OnClickListener()
    {
        
        @Override
        public void onClick(View v)
        {
            if (!EspBaseApiUtil.isWifiConnected())
            {
                Toast.makeText(getBaseContext(), R.string.esp_sss_esptouch_wifi_hint, Toast.LENGTH_LONG).show();
                return;
            }
            
            showEspTouchDialog();
        }
    };
    
    private View.OnLongClickListener mEspTouchLongClickistener = new View.OnLongClickListener()
    {
        
        @Override
        public boolean onLongClick(View v)
        {
            Toast.makeText(getBaseContext(), v.getContentDescription(), Toast.LENGTH_SHORT).show();
            return true;
        }
    };
    
    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId)
    {
        switch(checkedId)
        {
            case R.id.pager_tab_devices:
                mViewPager.setCurrentItem(FRAGMENT_DEVICE);
                break;
            case R.id.pager_tab_configure:
                mViewPager.setCurrentItem(FRAGMENT_CONFIGURE);
                break;
        }
    }
    
    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter
    {
        
        public SectionsPagerAdapter(FragmentManager fm)
        {
            super(fm);
        }
        
        @Override
        public Fragment getItem(int position)
        {
            if (position < mFragmentList.size())
            {
                return mFragmentList.get(position);
            }
            return null;
        }
        
        @Override
        public int getCount()
        {
            return mFragmentList.size();
        }
        
        @Override
        public CharSequence getPageTitle(int position)
        {
            Locale l = Locale.getDefault();
            switch (position)
            {
                case FRAGMENT_DEVICE:
                    return getString(R.string.esp_sss_actiontab_device).toUpperCase(l);
                case FRAGMENT_CONFIGURE:
                    return getString(R.string.esp_sss_actiontab_configure).toUpperCase(l);
            }
            return null;
        }
    }
    
    public void notifyFragment(int targetId)
    {
        switch(targetId) {
            case FRAGMENT_DEVICE:
                mFragmentDevice.showScanningPorgress(true);
                mFragmentDevice.scanStas();
                mViewPager.setCurrentItem(FRAGMENT_DEVICE);
                
                checkHelpNotifyDevice();
                break;
            case FRAGMENT_CONFIGURE:
                break;
        }
    }
    
    private void showEspTouchDialog()
    {
        View view = getLayoutInflater().inflate(R.layout.esp_esptouch, null);
        
        final String ssid = EspBaseApiUtil.getWifiConnectedSsid();
        TextView ssidTV = (TextView)view.findViewById(R.id.ssid);
        ssidTV.append(ssid);
        
        WifiManager wm = (WifiManager)getSystemService(Context.WIFI_SERVICE);
        final String bssid = wm.getConnectionInfo().getBSSID();
        
        final EditText passwordEdt = (EditText)view.findViewById(R.id.password);
        List<ApDB> apDBList = mIOTApDBManager.getAllApDBList();
        for (ApDB ap : apDBList)
        {
            if (ap.getBssid().equals(bssid))
            {
                passwordEdt.setText(ap.getPassword());
                break;
            }
        }
        
        CheckBox showPwdCB = (CheckBox)view.findViewById(R.id.show_password);
        showPwdCB.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener()
        {
            
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                if (isChecked)
                {
                    passwordEdt.setInputType(InputType_PASSWORD_VISIBLE);
                }
                else
                {
                    passwordEdt.setInputType(InputType_PAssWORD_NORMAL);
                }
            }
        });
        
        final CheckBox isHiddenCB = (CheckBox)view.findViewById(R.id.is_hidden);
        
        new AlertDialog.Builder(this).setView(view)
            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener()
            {
                
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    String password = passwordEdt.getText().toString();
                    mIOTApDBManager.insertOrReplace(bssid, ssid, password);
                    doEspTouch(ssid, bssid, password, isHiddenCB.isChecked());
                }
            })
            .show();
    }
    
    private void doEspTouch(final String ssid, final String bssid, final String password, final boolean isHidden)
    {
        final EspCommandDeviceEsptouch command = new EspCommandDeviceEsptouch();
        
        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage(getString(R.string.esp_sss_esptouch_progressing));
        dialog.setCancelable(false);
        dialog.setButton(DialogInterface.BUTTON_NEGATIVE,
            getString(android.R.string.cancel),
            new DialogInterface.OnClickListener()
            {
                
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    command.cancel();
                }
            });
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener()
        {
            
            @Override
            public void onDismiss(DialogInterface dialog)
            {
                notifyFragment(FRAGMENT_DEVICE);
            }
        });
        dialog.show();
        
        final Runnable dissmissRunnable = new Runnable()
        {
            
            @Override
            public void run()
            {
                dialog.dismiss();
            }
        };
        
        new Thread()
        {
            public void run() {
                command.doCommandDeviceEsptouch(0, ssid, bssid, password, isHidden);
                mHandler.post(dissmissRunnable);
            };
        }.start();
    }
    
    private static class SSSHandler extends Handler
    {
        private WeakReference<SoftApStaSupportActivity> mActivity;
        
        public SSSHandler(SoftApStaSupportActivity activity)
        {
            mActivity = new WeakReference<SoftApStaSupportActivity>(activity);
        }
        
        @Override
        public void handleMessage(Message msg)
        {
            SoftApStaSupportActivity activity = mActivity.get();
            if (activity == null)
            {
                return;
            }
        }
    }
    
    protected void checkHelpPagerConfigureSelected()
    {
    }
    
    protected void checkHelpNotifyDevice()
    {
    }
}
