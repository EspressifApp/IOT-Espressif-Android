package com.espressif.iot.command.device.light;

import java.net.HttpURLConnection;
import java.net.InetAddress;

import org.json.JSONException;
import org.json.JSONObject;

import com.espressif.iot.base.api.EspBaseApiUtil;
import com.espressif.iot.device.IEspDevice;

public class EspCommandLightTwinkleLocal implements IEspCommandLightTwinkleLocal {

    @Override
    public String getLocalUrl(InetAddress inetAddress) {
        return "http://" + inetAddress.getHostAddress();
    }

    @Override
    public boolean doCommandPostTwinkleOn(IEspDevice device, String appIP, int appPort, String appId) {
        String url = getLocalUrl(device.getInetAddress()) + URL_PARAM_ON;
        return postTwinkle(url, device, appIP, appPort, appId);
    }

    @Override
    public boolean doCommandPostTwinkleOff(IEspDevice device, String appIP, int appPort, String appId) {
        String url = getLocalUrl(device.getInetAddress()) + URL_PARAM_OFF;
        return postTwinkle(url, device, appIP, appPort, appId);
    }

    private boolean postTwinkle(String url, IEspDevice device, String appIP, int appPort, String appId) {
        try {
            JSONObject postJSON = new JSONObject();
            postJSON.put(KEY_IP, appIP);
            postJSON.put(KEY_PORT, appPort);
            postJSON.put(KEY_ID, appId);

            JSONObject respJSON;
            if (device.getIsMeshDevice()) {
                respJSON = EspBaseApiUtil.PostForJson(url, device.getBssid(), postJSON);
            } else {
                respJSON = EspBaseApiUtil.Post(url, postJSON);
            }

            if (respJSON != null) {
                int respStatus = respJSON.getInt(Status);
                return respStatus == HttpURLConnection.HTTP_OK;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return false;
    }
}
