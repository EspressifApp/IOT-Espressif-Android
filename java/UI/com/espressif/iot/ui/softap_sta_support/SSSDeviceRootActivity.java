package com.espressif.iot.ui.softap_sta_support;

import java.util.ArrayList;
import java.util.List;

import com.espressif.iot.R;
import com.espressif.iot.adt.tree.IEspDeviceTreeElement;
import com.espressif.iot.device.IEspDevice;
import com.espressif.iot.device.IEspDeviceSSS;
import com.espressif.iot.type.device.EspDeviceType;
import com.espressif.iot.ui.device.dialog.DeviceDialogBuilder;
import com.espressif.iot.ui.main.EspActivityAbs;
import com.espressif.iot.ui.view.EspPagerAdapter;
import com.espressif.iot.ui.view.EspViewPager;
import com.espressif.iot.ui.view.TreeView;
import com.espressif.iot.ui.view.TreeView.LastLevelItemClickListener;
import com.espressif.iot.user.builder.EspSSSUser;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class SSSDeviceRootActivity extends EspActivityAbs implements OnClickListener, LastLevelItemClickListener
{
    private EspSSSUser mUser;
    private IEspDeviceSSS mDevice;
    
    private EspViewPager mPager;
    private EspPagerAdapter mAdapter;
    private List<View> mViewList;
    
    private TreeView mTreeView;
    
    private Button mLightBtn;
    private Button mPlugBtn;
    private Button mRemoteBtn;
    
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.device_ui_container);
        
        mUser = EspSSSUser.getInstance();
        String bssid = getIntent().getStringExtra("bssid");
        mDevice = mUser.getDeviceByBssid(bssid);
        if (mDevice == null)
        {
            throw new NullPointerException("Not found device by bssid: " + bssid);
        }
        
        mPager = (EspViewPager)findViewById(R.id.device_ui_pager);
        mPager.setInterceptTouchEvent(mDevice.getIsMeshDevice());
        mViewList = new ArrayList<View>();
        mAdapter = new EspPagerAdapter(mViewList);
        View controlView = getLayoutInflater().inflate(R.layout.device_activity_root_router, null);
        View meshView = getLayoutInflater().inflate(R.layout.device_mesh_children_list, null);
        mViewList.add(controlView);
        mViewList.add(meshView);
        mPager.setAdapter(mAdapter);
        
        mLightBtn = (Button)controlView.findViewById(R.id.mesh_all_light_btn);
        mPlugBtn = (Button)controlView.findViewById(R.id.mesh_all_plug_btn);
        mRemoteBtn = (Button)controlView.findViewById(R.id.mesh_all_remote_btn);
        mLightBtn.setOnClickListener(this);
        mPlugBtn.setOnClickListener(this);
        mRemoteBtn.setOnClickListener(this);
        
        mTreeView = (TreeView)meshView.findViewById(R.id.mesh_children_list);
        mTreeView.setLastLevelItemClickCallBack(this);
        List<IEspDevice> deviceList = new ArrayList<IEspDevice>();
        deviceList.addAll(mUser.getDeviceList());
        List<IEspDeviceTreeElement> elementlist = mDevice.getDeviceTreeElementList(deviceList);
        mTreeView.initData(this, elementlist);
        if (mDevice.getIsMeshDevice())
        {
            setTitleRightIcon(R.drawable.esp_icon_swap);
        }
    }
    
    @Override
    protected void onTitleRightIconClick()
    {
        final int item_control = 0;
        final int item_children = 1;
        int currentItem = mPager.getCurrentItem();
        int targetItem = currentItem == item_control ? item_children : item_control;
        mPager.setCurrentItem(targetItem, true);
    }
    
    @Override
    public void onClick(View v)
    {
        if (v == mLightBtn)
        {
            new DeviceDialogBuilder(this, mDevice).showRootDialog(EspDeviceType.LIGHT);
        }
        else if (v == mPlugBtn)
        {
            new DeviceDialogBuilder(this, mDevice).showRootDialog(EspDeviceType.PLUG);
        }
        else if (v == mRemoteBtn)
        {
            new DeviceDialogBuilder(this, mDevice).showRootDialog(EspDeviceType.REMOTE);
        }
    }

    @Override
    public void onLastLevelItemClick(IEspDeviceTreeElement element, int position)
    {
        IEspDeviceSSS device = (IEspDeviceSSS)element.getCurrentDevice();
        switch(device.getDeviceType())
        {
            case LIGHT:
            case PLUG:
            case REMOTE:
            case PLUGS:
                new DeviceDialogBuilder(this, device).show();
                break;
                
            case FLAMMABLE:
            case HUMITURE:
            case VOLTAGE:
            case NEW:
            case ROOT:
                break;
        }
    }
}
