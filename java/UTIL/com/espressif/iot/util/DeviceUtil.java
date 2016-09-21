package com.espressif.iot.util;

import org.apache.log4j.Logger;

import com.espressif.iot.R;
import com.espressif.iot.device.IEspDevice;
import com.espressif.iot.type.device.EspDeviceType;
import com.espressif.iot.type.device.IEspDeviceState;
import com.espressif.iot.ui.device.DeviceFlammableActivity;
import com.espressif.iot.ui.device.DeviceHumitureActivity;
import com.espressif.iot.ui.device.DevicePlugActivity;
import com.espressif.iot.ui.device.DevicePlugsActivity;
import com.espressif.iot.ui.device.DeviceRemoteActivity;
import com.espressif.iot.ui.device.DeviceRootRouterActivity;
import com.espressif.iot.ui.device.DeviceVoltageActivity;
import com.espressif.iot.ui.device.light.DeviceLightActivity;
import com.espressif.iot.ui.device.soundbox.DeviceSoundboxActivity;

import android.content.Context;
import android.content.Intent;

public class DeviceUtil {
    private DeviceUtil() {
    }

    private static final Logger log = Logger.getLogger(DeviceUtil.class);

    /**
     * Get device type's function use class
     * 
     * @param device
     * @return
     */
    public static Class<?> getDeviceClass(IEspDevice device) {
        IEspDeviceState state = device.getDeviceState();
        Class<?> _class = null;
        switch (device.getDeviceType()) {
            case PLUG:
                if (state.isStateInternet() || state.isStateLocal()) {
                    _class = DevicePlugActivity.class;
                }
                break;
            case LIGHT:
                if (state.isStateInternet() || state.isStateLocal()) {
                    _class = DeviceLightActivity.class;
                }
                break;
            case FLAMMABLE:
                if (state.isStateInternet() || state.isStateOffline()) {
                    _class = DeviceFlammableActivity.class;
                }
                break;
            case HUMITURE:
                if (state.isStateInternet() || state.isStateOffline()) {
                    _class = DeviceHumitureActivity.class;
                }
                break;
            case VOLTAGE:
                if (state.isStateInternet() || state.isStateOffline()) {
                    _class = DeviceVoltageActivity.class;
                }
                break;
            case REMOTE:
                if (state.isStateInternet() || state.isStateLocal()) {
                    _class = DeviceRemoteActivity.class;
                }
                break;
            case PLUGS:
                if (state.isStateInternet() || state.isStateLocal()) {
                    _class = DevicePlugsActivity.class;
                }
                break;
            case SOUNDBOX:
                if (state.isStateInternet() || state.isStateLocal()) {
                    _class = DeviceSoundboxActivity.class;
                }
                break;
            case ROOT:
                if (state.isStateInternet() || state.isStateLocal()) {
                    _class = DeviceRootRouterActivity.class;
                }
                break;
            case NEW:
                log.warn("Click on NEW device, it shouldn't happen");
                break;
        }

        return _class;
    }

    public static Class<?> getLocalDeviceClass(EspDeviceType type) {
        Class<?> cls = null;
        switch (type) {
            case PLUG:
                cls = DevicePlugActivity.class;
                break;
            case LIGHT:
                cls = DeviceLightActivity.class;
                break;
            default:
                break;
        }

        return cls;
    }

    /**
     * Get the intent which can start device Activity
     * 
     * @param context
     * @param device
     * @return
     */
    public static Intent getDeviceIntent(Context context, IEspDevice device) {
        Class<?> cls = getDeviceClass(device);
        if (cls != null) {
            Intent intent = new Intent(context, cls);
            intent.putExtra(EspStrings.Key.DEVICE_KEY_KEY, device.getKey());
            return intent;
        } else {
            return null;
        }
    }

    public static int getDeviceIconRes(EspDeviceType type) {
        int res = 0;
        switch (type) {
            case FLAMMABLE:
                res = R.drawable.device_flammable_offline;
                break;
            case HUMITURE:
                res = R.drawable.device_filter_icon_humiture;
                break;
            case VOLTAGE:
                res = R.drawable.device_voltage_offline;
                break;
            case LIGHT:
                res = R.drawable.device_filter_icon_light;
                break;
            case PLUG:
                res = R.drawable.device_filter_icon_plug;
                break;
            case PLUGS:
                res = R.drawable.device_filter_icon_plugs;
                break;
            case SOUNDBOX:
                res = R.drawable.device_filter_icon_soundbox;
                break;
            case NEW:
            case ROOT:
                res = R.drawable.device_filter_icon_all;
                break;
            case REMOTE:
                break;
        }

        return res;
    }

    public static int getDeviceIconRes(IEspDevice device) {
        int res = 0;
        IEspDeviceState state = device.getDeviceState();
        boolean isOffline = state.isStateOffline();
        switch (device.getDeviceType()) {
            case FLAMMABLE:
                res = isOffline ? R.drawable.device_flammable_offline : R.drawable.device_flammable_online;
                break;
            case HUMITURE:
                res = isOffline ? R.drawable.device_humiture_offline : R.drawable.device_humiture_online;
                break;
            case VOLTAGE:
                res = isOffline ? R.drawable.device_voltage_offline : R.drawable.device_voltage_online;
                break;
            case LIGHT:
                res = isOffline ? R.drawable.device_light_offline : R.drawable.device_light_online;
                break;
            case PLUG:
                res = isOffline ? R.drawable.device_plug_offline : R.drawable.device_plug_online;
                break;
            case PLUGS:
                res = isOffline ? R.drawable.device_plugs_offline : R.drawable.device_plugs_online;
                break;
            case SOUNDBOX:
                res = isOffline ? R.drawable.device_soundbox_offline : R.drawable.device_soundbox_online;
                break;
            case ROOT:
            case NEW:
                res = R.drawable.device_home;
                break;
            case REMOTE:
                break;
        }

        return res;
    }

    public static int getDeviceTypeNameRes(EspDeviceType type) {
        int result = 0;
        switch (type) {
            case ROOT:
                break;
            case NEW:
                break;
            case REMOTE:
                break;
            case FLAMMABLE:
                result = R.string.esp_main_type_flammable;
                break;
            case HUMITURE:
                result = R.string.esp_main_type_humiture;
                break;
            case LIGHT:
                result = R.string.esp_main_type_light;
                break;
            case PLUG:
                result = R.string.esp_main_type_plug;
                break;
            case PLUGS:
                result = R.string.esp_main_type_plugs;
                break;
            case SOUNDBOX:
                result = R.string.esp_main_type_soundbox;
                break;
            case VOLTAGE:
                result = R.string.esp_main_type_voltage;
                break;
        }

        return result;
    }
}
