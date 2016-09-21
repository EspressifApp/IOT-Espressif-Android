package com.espressif.iot.ui.device;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.espressif.iot.R;
import com.espressif.iot.ui.achartengine.EspChartFactory;
import com.espressif.iot.util.TimeUtil;

public abstract class DeviceChartActivityAbs extends DeviceActivityAbs implements OnRefreshListener
{
    protected static final int MENU_ID_SELECT_DATE = 0x2000;
    
    protected long mSelectTime;
    
    /**
     * Whether the chart line View has drawn once
     */
    private boolean mChartViewDrawn;

    protected TextView mDateTV;
    protected SwipeRefreshLayout mRefreshLayout;
    protected LinearLayout mChartViewContainer;
    
    protected EspChartFactory mChartFactory;
    
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        boolean compatibility = isDeviceCompatibility();
        mChartViewDrawn = false;
        if (compatibility)
        {
            mSelectTime = System.currentTimeMillis();
            refresh();
        }
    }
    
    @Override
    protected View initControlView()
    {
        View view = View.inflate(this, R.layout.device_activity_chartview, null);

        mDateTV = (TextView)view.findViewById(R.id.date_text);

        mRefreshLayout = (SwipeRefreshLayout)view.findViewById(R.id.refresh_layout);
        mRefreshLayout.setColorSchemeResources(R.color.esp_actionbar_color);
        mRefreshLayout.setOnRefreshListener(this);

        mChartViewContainer = (LinearLayout)view.findViewById(R.id.chartview_container);
        mChartFactory = new EspChartFactory(this);

        mPager.setInterceptTouchEvent(false);

        return view;
    }
    
    @Override
    protected void onCreateTitleMenuItem(Menu menu) {
        menu.add(Menu.NONE, MENU_ID_SELECT_DATE, 0, R.string.esp_device_chartview_menu_select_date);
    }
    
    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_ID_SELECT_DATE:
                showSelectDateDialog();
                return true;
            default:
                break;
        }

        return super.onMenuItemClick(item);
    }
    
    private void showSelectDateDialog()
    {
        final DatePicker datePicker = (DatePicker)View.inflate(this, R.layout.chartview_date_select_dialog, null);
        datePicker.setMaxDate(System.currentTimeMillis());
        new AlertDialog.Builder(this).setView(datePicker)
            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener()
            {
                
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    mSelectTime =
                        TimeUtil.getLong(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth());
                    refresh(mSelectTime);
                }
            })
            .setNegativeButton(android.R.string.cancel, null)
            .show();
    }
    
    /**
     * Show the current time data
     */
    protected void refresh()
    {
        refresh(System.currentTimeMillis());
    }
    
    /**
     * Show the target time data
     * 
     * @param time
     */
    abstract protected void refresh(long time);
    
    @Override
    public void onRefresh()
    {
        refresh(mSelectTime);
    }
    
    /**
     * Set the ChartView has drawn at least once
     * 
     * @param drawn
     */
    protected void setChartViewDrawn(boolean drawn)
    {
        mChartViewDrawn = drawn;
    }
    
    /**
     * Get whether the ChartView has drawn at least once
     * 
     * @return
     */
    protected boolean isChartViewDrawn()
    {
        return mChartViewDrawn;
    }
}
