package com.espressif.iot.ui.device;

import java.util.ArrayList;
import java.util.List;

import org.achartengine.util.MathHelper;

import com.espressif.iot.R;
import com.espressif.iot.action.device.builder.BEspAction;
import com.espressif.iot.action.device.voltage.IEspActionVoltageGetStatusListInternetDB;
import com.espressif.iot.help.ui.IEspHelpUIUseVoltage;
import com.espressif.iot.type.device.status.IEspStatusVoltage;
import com.espressif.iot.type.help.HelpStepUseVoltage;
import com.espressif.iot.ui.achartengine.ChartData;
import com.espressif.iot.ui.achartengine.ChartPoint;
import com.espressif.iot.ui.achartengine.EspChartFactory;
import com.espressif.iot.util.TimeUtil;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshScrollView;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class DeviceVoltageActivity extends DeviceActivityAbs implements OnRefreshListener<ScrollView>,
    IEspHelpUIUseVoltage
{
    private TextView mDateTV;
    
    private PullToRefreshScrollView mPullRereshScorllView;
    
    private LinearLayout mChartViewContainer;
    
    private EspChartFactory mChartFactory;
    
    private boolean mRefreshing = false;
    
    private long mSelectTime;
    
    private static final int MENU_ID_SELECT_DATE = 0x2000;
    
    private IEspActionVoltageGetStatusListInternetDB mAction;
    
    /**
     * Whether the chart line View has drawn once
     */
    private boolean mChartViewDrawn;
    
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        boolean compatibility = isDeviceCompatibility();
        if (mHelpMachine.isHelpModeUseVoltage())
        {
            mHelpMachine.transformState(compatibility);
            if (!compatibility)
            {
                onHelpUseVoltage();
            }
            // if compatibility is true, do onHelpUseVoltage() when first get data (onPostExecute in RefreshTask)
        }
        
        mChartViewDrawn = false;
        if (compatibility)
        {
            mSelectTime = System.currentTimeMillis();
            refresh();
        }
    }
    
    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        if (mAction != null)
        {
            mAction.cancel(true);
        }
    }
    
    @Override
    protected View initControlView()
    {
        View view = getLayoutInflater().inflate(R.layout.device_activity_chartview, null);
        
        mPullRereshScorllView = (PullToRefreshScrollView)view.findViewById(R.id.pull_to_refresh_scrollview);
        mPullRereshScorllView.setOnRefreshListener(this);
        
        mChartViewContainer = (LinearLayout)view.findViewById(R.id.chartview_container);
        mChartFactory = new EspChartFactory(this);
        
        mDateTV = (TextView)view.findViewById(R.id.date_text);
        
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
                if (mHelpMachine.isHelpModeUseVoltage())
                {
                    return true;
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    
    private void showSelectDateDialog()
    {
        final DatePicker datePicker =
            (DatePicker)getLayoutInflater().inflate(R.layout.chartview_date_select_dialog, null);
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
    
    @Override
    protected void executePrepare()
    {
    }
    
    @Override
    protected void executeFinish(int command, boolean result)
    {
    }
    
    @Override
    public void onRefresh(PullToRefreshBase<ScrollView> arg0)
    {
        refresh(mSelectTime);
    }
    
    /**
     * Show the current time data
     */
    private void refresh()
    {
        refresh(System.currentTimeMillis());
    }
    
    /**
     * Show the target time data
     * 
     * @param time
     */
    private void refresh(long time)
    {
        if (!mRefreshing)
        {
            new RefreshTask(time).execute();
        }
    }
    
    private class RefreshTask extends AsyncTask<Void, Void, List<IEspStatusVoltage>>
    {
        private final long INTERVAL = 6 * 60 * 1000;
        
        private final String DATE_FORMAT = "yyyy-MM-dd";
        private final String TIME_FORMAT = "HH:mm";
        
        private final long mEndTime;
        private final long mDayStartTime;
        
        private final String mDateText;
        
        private ProgressDialog mDialog;
        
        public RefreshTask(long time)
        {
            mDayStartTime = TimeUtil.getDayStartLong(time);
            mEndTime = mDayStartTime + TimeUtil.ONE_DAY_LONG_VALUE;
            mDateText = TimeUtil.getDateStr(mDayStartTime, DATE_FORMAT);
        }
        
        @Override
        protected void onPreExecute()
        {
            mDialog = new ProgressDialog(DeviceVoltageActivity.this);
            mDialog.setMessage(getString(R.string.esp_device_task_dialog_message));
            mDialog.setCancelable(false);
            mDialog.show();
            
            mRefreshing = true;
            mDateTV.setText(mDateText);
        }
        
        @Override
        protected List<IEspStatusVoltage> doInBackground(Void... params)
        {
            mAction =
                (IEspActionVoltageGetStatusListInternetDB)BEspAction.getInstance()
                    .alloc(IEspActionVoltageGetStatusListInternetDB.class);
            long deviceId = mIEspDevice.getId();
            String deviceKey = mIEspDevice.getKey();
            return mAction.doActionVoltageGetStatusListInternetDB(deviceId,
                deviceKey,
                mDayStartTime,
                mEndTime,
                INTERVAL);
        }
        
        @Override
        protected void onPostExecute(List<IEspStatusVoltage> result)
        {
            if (result != null)
            {
                List<ChartPoint> cpListL = new ArrayList<ChartPoint>();
                String flamTitle = getString(R.string.esp_device_voltage);
                for (int i = 0; i < result.size(); i++)
                {
                    ChartPoint cpL;
                    IEspStatusVoltage status = result.get(i);
                    
                    if (status != null)
                    {
                        String timeAtStr = TimeUtil.getDateStr(status.getAt(), TIME_FORMAT);
                        cpL = new ChartPoint(i, status.getX());
                        cpL.setXLabel(timeAtStr);
                    }
                    else
                    {
                        long timeAt = mDayStartTime + i * INTERVAL;
                        String timeAtStr = TimeUtil.getDateStr(timeAt, TIME_FORMAT);
                        cpL = new ChartPoint(i, MathHelper.NULL_VALUE);
                        cpL.setXLabel(timeAtStr);
                    }
                    
                    cpListL.add(cpL);
                }
                ChartData data = new ChartData(flamTitle, flamTitle, cpListL);
                
                View view = mChartFactory.getSingleLineChartView(data);
                mChartViewContainer.removeAllViews();
                mChartViewContainer.addView(view);
            }
            
            else
            {
                Toast.makeText(DeviceVoltageActivity.this,
                    R.string.esp_device_data_network_not_found_message,
                    Toast.LENGTH_LONG).show();
            }
            
            mPullRereshScorllView.onRefreshComplete();
            mRefreshing = false;
            
            if (mDialog != null)
            {
                mDialog.dismiss();
                mDialog = null;
            }
            
            if (mHelpMachine.isHelpModeUseVoltage())
            {
                onHelpUseVoltage();
                if (result == null)
                {
                    mHelpMachine.transformState(false);
                    onHelpUseVoltage();
                }
                else if (mChartViewDrawn)
                {
                    mHelpMachine.transformState(true);
                    onHelpUseVoltage();
                }
            }
            
            mChartViewDrawn = true;
        }
    }

    @Override
    public void onHelpUseVoltage()
    {
        clearHelpContainer();
        
        HelpStepUseVoltage step = HelpStepUseVoltage.valueOf(mHelpMachine.getCurrentStateOrdinal());
        switch(step)
        {
            case START_USE_HELP:
                break;
            case FAIL_FOUND_VOLTAGE:
                break;
            case VOLTAGE_SELECT:
                break;
                
            case VOLTAGE_NOT_COMPATIBILITY:
                mHelpMachine.exit();
                setResult(RESULT_EXIT_HELP_MODE);
                break;
            case PULL_DOWN_TO_REFRESH:
                highlightHelpView(mChartViewContainer);
                setHelpHintMessage(R.string.esp_help_use_voltage_pull_down_to_refresh_msg);
                break;
            case GET_DATA_FAILED:
                highlightHelpView(mChartViewContainer);
                setHelpHintMessage(R.string.esp_help_use_voltage_get_data_failed_msg);
                mHelpMachine.retry();
                break;
            case SELECT_DATE:
                highlightHelpView(getRightTitleIcon());
                setHelpHintMessage(R.string.esp_help_use_voltage_select_date_msg);
                break;
            case SELECT_DATE_FAILED:
                highlightHelpView(mChartViewContainer);
                setHelpHintMessage(R.string.esp_help_use_voltage_select_date_failed_msg);
                mHelpMachine.retry();
                break;
            case SUC:
                setHelpFrameDark();
                setHelpHintMessage(R.string.esp_help_use_voltage_success_msg);
                setHelpButtonVisible(HELP_BUTTON_EXIT, true);
                break;
        }
    }
}
