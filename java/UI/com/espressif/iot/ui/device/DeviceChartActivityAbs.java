package com.espressif.iot.ui.device;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.espressif.iot.R;
import com.espressif.iot.ui.achartengine.EspChartFactory;
import com.espressif.iot.util.TimeUtil;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshScrollView;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;

public abstract class DeviceChartActivityAbs extends DeviceActivityAbs implements OnRefreshListener<ScrollView>
{
    protected static final int MENU_ID_SELECT_DATE = 0x2000;
    
    protected long mSelectTime;
    
    /**
     * Whether the chart line View has drawn once
     */
    private boolean mChartViewDrawn;

    protected TextView mDateTV;
    protected PullToRefreshScrollView mPullRereshScorllView;
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
        
        mPullRereshScorllView = (PullToRefreshScrollView)view.findViewById(R.id.pull_to_refresh_scrollview);
        mPullRereshScorllView.setOnRefreshListener(this);
        
        mChartViewContainer = (LinearLayout)view.findViewById(R.id.chartview_container);
        mChartFactory = new EspChartFactory(this);
        
        mPager.setInterceptTouchEvent(false);
        
        return view;
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        menu.add(Menu.NONE, MENU_ID_SELECT_DATE, 0, R.string.esp_device_chartview_menu_select_date);
        
        return super.onCreateOptionsMenu(menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case MENU_ID_SELECT_DATE:
                showSelectDateDialog();
                return true;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
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
    public void onRefresh(PullToRefreshBase<ScrollView> arg0)
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
