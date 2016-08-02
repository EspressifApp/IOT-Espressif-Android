package com.espressif.iot.model.device;

import com.espressif.iot.device.IEspDevice;
import com.espressif.iot.device.IEspDeviceLight;
import com.espressif.iot.type.device.status.EspStatusLight;
import com.espressif.iot.type.device.status.IEspStatusLight;

import android.graphics.Color;
import android.text.TextUtils;

public class EspDeviceLight extends EspDevice implements IEspDeviceLight
{
    private IEspStatusLight mStatusLight;
    
    public EspDeviceLight()
    {
        mStatusLight = new EspStatusLight();
    }
    
    @Override
    public Object clone()
        throws CloneNotSupportedException
    {
        EspDeviceLight device = (EspDeviceLight)super.clone();
        // deep copy
        IEspStatusLight status = device.getStatusLight();
        device.mStatusLight = (IEspStatusLight)((EspStatusLight)status).clone();
        return device;
    }
    
    @Override
    public IEspStatusLight getStatusLight()
    {
        return mStatusLight;
    }
    
    @Override
    public void setStatusLight(IEspStatusLight statusLight)
    {
        mStatusLight = statusLight;
    }

    private void parseInfo(String info) {
        if (!TextUtils.isEmpty(info)) {
            int infoInt;
            try {
                infoInt = Integer.parseInt(info, 16);
            } catch (NumberFormatException e) {
                e.printStackTrace();
                return;
            }

            int status = Color.alpha(infoInt);
            int red = Color.red(infoInt);
            int green = Color.green(infoInt);
            int blue = Color.blue(infoInt);

            mStatusLight.setStatus(status);
            switch (status) {
                case IEspStatusLight.STATUS_OFF:
                case IEspStatusLight.STATUS_COLOR:
                    mStatusLight.setRed(red);
                    mStatusLight.setGreen(green);
                    mStatusLight.setBlue(blue);
                    break;
                case IEspStatusLight.STATUS_ON:
                    break;
                case IEspStatusLight.STATUS_BRIGHT:
                    mStatusLight.setWhite(red);
                    break;
            }
        }
    }

    @Override
    public void setInfo(String info) {
        super.setInfo(info);

        parseInfo(info);
    }

    @Override
    public void copyDeviceInfo(IEspDevice device) {
        super.copyDeviceInfo(device);

        parseInfo(device.getInfo());
    }
}
