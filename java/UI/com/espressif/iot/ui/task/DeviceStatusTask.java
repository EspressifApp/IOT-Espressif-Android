package com.espressif.iot.ui.task;

import com.espressif.iot.device.IEspDevice;
import com.espressif.iot.device.array.IEspDeviceArray;
import com.espressif.iot.type.device.IEspDeviceStatus;
import com.espressif.iot.user.IEspUser;
import com.espressif.iot.user.builder.BEspUser;

import android.os.AsyncTask;

public class DeviceStatusTask extends AsyncTask<IEspDeviceStatus, Void, Boolean> {

    private IEspDevice mDevice;
    private IEspUser mUser;

    public interface OnTaskExecuteListener {
        void onPreExecute();

        void onPostExecute(Boolean result);

        void onCancelled();
    }

    private OnTaskExecuteListener mOnTaskExecuteListener;

    public DeviceStatusTask(IEspDevice device) {
        mDevice = device;
        mUser = BEspUser.getBuilder().getInstance();
    }

    @Override
    protected void onPreExecute() {
        if (mOnTaskExecuteListener != null) {
            mOnTaskExecuteListener.onPreExecute();
        }
    }

    @Override
    protected Boolean doInBackground(IEspDeviceStatus... params) {
        if (params.length > 0) {
            // execute Post status
            IEspDeviceStatus status = params[0];
            return mUser.doActionPostDeviceStatus(mDevice, status);
        } else {
            // execute Get status
            if (mDevice instanceof IEspDeviceArray) {
                return false;
            } else {
                return mUser.doActionGetDeviceStatus(mDevice);
            }
        }
    }

    @Override
    protected void onPostExecute(Boolean result) {
        if (mOnTaskExecuteListener != null) {
            mOnTaskExecuteListener.onPostExecute(result);
        }
    }

    @Override
    protected void onCancelled() {
        if (mOnTaskExecuteListener != null) {
            mOnTaskExecuteListener.onCancelled();
        }
    }

    public void setOnTaskExecuteListener(OnTaskExecuteListener listener) {
        mOnTaskExecuteListener = listener;
    }
}
