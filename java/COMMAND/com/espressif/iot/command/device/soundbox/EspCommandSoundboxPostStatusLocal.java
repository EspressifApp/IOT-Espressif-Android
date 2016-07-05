package com.espressif.iot.command.device.soundbox;

import java.net.HttpURLConnection;
import java.net.InetAddress;

import org.json.JSONException;
import org.json.JSONObject;

import com.espressif.iot.base.api.EspBaseApiUtil;
import com.espressif.iot.type.device.other.EspAudio;
import com.espressif.iot.type.device.status.IEspStatusSoundbox;

public class EspCommandSoundboxPostStatusLocal implements IEspCommandSoundboxPostStatusLocal {

    @Override
    public String getLocalUrl(InetAddress inetAddress) {
        return "http://" + inetAddress.getHostAddress() + "/" + "config?command=soundbox";
    }

    @Override
    public boolean doCommandPlugsPostStatusLocal(InetAddress inetAddress, IEspStatusSoundbox status, String deviceBssid,
        boolean isMesh) {
        JSONObject respJSON;
        String url = getLocalUrl(inetAddress);
        JSONObject postJSON = getPostJSON(status);
        if (deviceBssid == null || !isMesh) {
            respJSON = EspBaseApiUtil.Post(url, postJSON);
        } else {
            respJSON = EspBaseApiUtil.PostForJson(url, deviceBssid, postJSON);
        }
        if (respJSON != null) {
            try {
                int statusCode = respJSON.getInt(Status);
                return statusCode == HttpURLConnection.HTTP_OK;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return false;
    }

    private JSONObject getPostJSON(IEspStatusSoundbox status) {
        int action = status.getAction();
        JSONObject json = new JSONObject();
        try {
            json.put(KEY_TYPE, action);
            switch (action) {
                case IEspStatusSoundbox.ACTION_AUDIO:
                    EspAudio audio = status.getAudio();
                    json.put(KEY_LIVE, audio.getType().ordinal());
                    json.put(KEY_URL, audio.getDownloadUrl());
                    json.put(KEY_NAME, audio.getTitle());
                    break;
                case IEspStatusSoundbox.ACTION_PLAY:
                    int playStatus = status.getPlayStatus();
                    json.put(KEY_PLAY_STATUS, playStatus);
                    break;
                case IEspStatusSoundbox.ACTION_VOLUME:
                    int volume = status.getVolume();
                    json.put(KEY_VOLUME, volume);
                    break;
                default:
                    return null;
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

        return json;
    }
}
