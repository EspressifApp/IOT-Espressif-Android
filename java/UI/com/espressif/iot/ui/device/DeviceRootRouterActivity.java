package com.espressif.iot.ui.device;

import com.espressif.iot.R;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class DeviceRootRouterActivity extends DeviceActivityAbs implements OnClickListener
{
    private Button mPlugBtn;
    private Button mLightBtn;
    private Button mRemoteBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        // hide mesh child control
        mPager.setCurrentItem(1);
        mPager.setInterceptTouchEvent(false);
    }

    @Override
    protected View initControlView()
    {
        View view  = View.inflate(this, R.layout.device_activity_root_router, null);

        mPlugBtn = (Button)view.findViewById(R.id.mesh_all_plug_btn);
        mPlugBtn.setOnClickListener(this);
        mLightBtn = (Button)view.findViewById(R.id.mesh_all_light_btn);
        mLightBtn.setOnClickListener(this);
        mRemoteBtn = (Button)view.findViewById(R.id.mesh_all_remote_btn);
        mRemoteBtn.setOnClickListener(this);

        return view;
    }

    @Override
    protected void executePrepare()
    {
    }

    @Override
    protected void executeFinish(int command, boolean result)
    {
    }

    @Override
    public void onClick(View v)
    {
        if (v == mPlugBtn)
        {
        }
        else if (v == mLightBtn)
        {
        }
        else if (v == mRemoteBtn)
        {
        }
    }
}
