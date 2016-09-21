package com.espressif.iot.ui.device;

import java.util.ArrayList;
import java.util.List;

import org.achartengine.util.MathHelper;

import com.espressif.iot.R;
import com.espressif.iot.action.device.builder.BEspAction;
import com.espressif.iot.action.device.flammable.IEspActionFlammableGetStatusListInternetDB;
import com.espressif.iot.type.device.status.IEspStatusFlammable;
import com.espressif.iot.ui.achartengine.ChartData;
import com.espressif.iot.ui.achartengine.ChartPoint;
import com.espressif.iot.util.TimeUtil;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.view.View;
import android.widget.Toast;

public class DeviceFlammableActivity extends DeviceChartActivityAbs
{
    private boolean mRefreshing = false;
    
    private IEspActionFlammableGetStatusListInternetDB mAction;
    
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
    protected void executePrepare()
    {
    }

    @Override
    protected void executeFinish(int command, boolean result)
    {
    }
    
    @Override
    protected void refresh(long time)
    {
        if (!mRefreshing)
        {
            new RefreshTask(time).execute();
        }
    }
    
    private class RefreshTask extends AsyncTask<Void, Void, List<IEspStatusFlammable>>
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
            mDialog = new ProgressDialog(DeviceFlammableActivity.this);
            mDialog.setMessage(getString(R.string.esp_device_task_dialog_message));
            mDialog.setCancelable(false);
            mDialog.show();
            
            mRefreshing = true;
            mDateTV.setText(mDateText);
        }
        
        @Override
        protected List<IEspStatusFlammable> doInBackground(Void... params)
        {
            mAction =
                (IEspActionFlammableGetStatusListInternetDB)BEspAction.getInstance()
                    .alloc(IEspActionFlammableGetStatusListInternetDB.class);
            long deviceId = mIEspDevice.getId();
            String deviceKey = mIEspDevice.getKey();
            return mAction.doActionFlammableGetStatusListInternetDB(deviceId,
                deviceKey,
                mDayStartTime,
                mEndTime,
                INTERVAL);
        }
        
        @Override
        protected void onPostExecute(List<IEspStatusFlammable> result)
        {
            if (result != null)
            {
                List<ChartPoint> cpListL = new ArrayList<ChartPoint>();
                String flamTitle = getString(R.string.esp_device_flammable);
                for (int i = 0; i < result.size(); i++)
                {
                    ChartPoint cpL;
                    IEspStatusFlammable status = result.get(i);
                    
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
                Toast.makeText(DeviceFlammableActivity.this,
                    R.string.esp_device_data_network_not_found_message,
                    Toast.LENGTH_LONG).show();
            }
            
            mRefreshLayout.setRefreshing(false);
            mRefreshing = false;
            
            if (mDialog != null)
            {
                mDialog.dismiss();
                mDialog = null;
            }
            
            setChartViewDrawn(true);
        }
    }
}
