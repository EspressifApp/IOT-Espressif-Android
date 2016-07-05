package com.espressif.iot.ui.device.soundbox;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.espressif.iot.type.device.other.EspAudio;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public enum QingtingFM {
    INSTANCE;

    private static final String URL_BASE = "http://api.open.qingting.fm";
    private static final String PATH_AUTHORIZATION = "/access?&grant_type=client_credentials";
    private static final String PATH_MEDIA_CENTER = "/v6/media/mediacenterlist?access_token=";
    private static final String PATH_DEMAND_CATEGORY = "/v6/media/categories?access_token=";
    private static final String PATH_PLAYING = "/v6/media/recommends/nowplaying/day/";

    private static final String PATH_CHANNEL(long categoryId, int page) {
        return "/v6/media/categories/" + categoryId + "/channels/order/0/curpage/" + page
            + "/pagesize/30?access_token=";
    }

    private static final String KEY_ACCESS_TOKEN = "access_token";
    private static final String KEY_ACCESS_TOKEN_EXPIRES = "expires_in";
    private static final String KEY_DATA = "data";
    private static final String KEY_RADIOSTATIONS_DOWNLOAD = "radiostations_download";
    private static final String KEY_MEDIACENTERS = "mediacenters";
    private static final String KEY_ACCESS = "access";
    private static final String KEY_DOMAIN = "domain";
    private static final String KEY_RADIOSTATIONS_HLS = "radiostations_hls";
    private static final String KEY_RADIOSTATIONS_HTTP = "radiostations_http";
    private static final String KEY_STOREDAUDIO_M4A = "storedaudio_m4a";
    private static final String KEY_ID = "id";
    private static final String KEY_TITLE = "title";
    private static final String KEY_THUMBS = "thumbs";
    private static final String KEY_THUMB_SMALL = "small_thumb";
    private static final String KEY_THUMB_MEDIUM = "medium_thumb";
    private static final String KEY_THUMB_LARGE = "large_thumb";

    private static final String PARAM_ID = "${res_id}";
    private static final String PARAM_BITRATE = "${BITRATE}";
    private static final String PARAM_DEVICEID = "${DEVICEID}";
    private static final String PARAM_START = "${START}";
    private static final String PARAM_END = "${END}";

    private static final String PREF_FILE_QINGTING = "qingting";
    private static final String PREF_KEY_ACCESS_TOKEN = "access_token";
    private static final String PREF_KEY_EXPIRES = "access_expires";

    private Context mContext;
    private SharedPreferences mShared;

    private DefaultHttpClient mHttpClient;

    private String mAccessToken;
    private long mAccessTokenExpires;

    private QingtingFM() {
        mHttpClient = new DefaultHttpClient();
    }

    public void init(Context context) {
        mContext = context;
        mShared = context.getSharedPreferences(PREF_FILE_QINGTING, Context.MODE_PRIVATE);
        mAccessToken = mShared.getString(PREF_KEY_ACCESS_TOKEN, null);
        mAccessTokenExpires = mShared.getLong(PREF_KEY_EXPIRES, 0);
    }

    public String getAccessToken() {
        return mAccessToken;
    }

    public long getAccessTokenExpires() {
        return mAccessTokenExpires;
    }

    private class ResponseInfo {
        int statusCode;
        String content;
    }

    public boolean accessable() {
        return mAccessToken != null && mAccessTokenExpires > System.currentTimeMillis();
    }

    private ResponseInfo executeHttpRequest(HttpUriRequest request)
        throws IOException {
        HttpResponse response = mHttpClient.execute(request);
        if (response == null) {
            System.out.println("response is null");
            return null;
        }

        ResponseInfo result = new ResponseInfo();

        int statusCode = response.getStatusLine().getStatusCode();
        System.out.println("response code = " + statusCode);
        result.statusCode = statusCode;

        HttpEntity respEntity = response.getEntity();
        if (respEntity == null) {
            System.out.println("response entity is null");
            result.content = null;
        } else {
            String respContent = EntityUtils.toString(respEntity);
            System.out.println("response content = " + respContent);
            result.content = respContent;
        }

        return result;
    }

    public boolean authorization(String client_id, String client_secret) {
        String url = URL_BASE + PATH_AUTHORIZATION;
        HttpPost request = new HttpPost(url);
        ArrayList<BasicNameValuePair> nameValuePairs = new ArrayList<BasicNameValuePair>(2);
        nameValuePairs.add(new BasicNameValuePair("client_id", client_id));
        nameValuePairs.add(new BasicNameValuePair("client_secret", client_secret));
        try {
            request.setEntity(new UrlEncodedFormEntity(nameValuePairs));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return false;
        }

        try {
            ResponseInfo resp = executeHttpRequest(request);
            if (resp == null || resp.statusCode != HttpStatus.SC_OK || resp.content == null) {
                return false;
            }
            JSONObject result = new JSONObject(resp.content);
            mAccessToken = result.getString(KEY_ACCESS_TOKEN);
            mAccessTokenExpires = result.getLong(KEY_ACCESS_TOKEN_EXPIRES);

            Editor editor = mShared.edit();
            editor.putString(PREF_KEY_ACCESS_TOKEN, mAccessToken);
            editor.putLong(PREF_KEY_EXPIRES, mAccessTokenExpires);
            editor.commit();
            return true;
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean loadMediaCenter() {
        if (mAccessToken == null) {
            return false;
        }

        String url = URL_BASE + PATH_MEDIA_CENTER + mAccessToken;
        HttpGet request = new HttpGet(url);
        try {
            ResponseInfo resp = executeHttpRequest(request);
            if (resp == null || resp.statusCode != HttpStatus.SC_OK || resp.content == null) {
                return false;
            }

            JSONObject result = new JSONObject(resp.content);
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return false;
    }

    private void getDemandCategories() {
        if (mAccessToken == null) {
            return;
        }

        String url = URL_BASE + PATH_DEMAND_CATEGORY + mAccessToken;
        HttpGet request = new HttpGet(url);
        try {
            ResponseInfo resp = executeHttpRequest(request);
            if (resp == null || resp.statusCode != HttpStatus.SC_OK || resp.content == null) {
                return;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<EspAudio> getLiveChannels() {
        if (mAccessToken == null) {
            return null;
        }

        String url = URL_BASE + PATH_CHANNEL(5, 0) + mAccessToken;
        HttpGet request = new HttpGet(url);
        try {
            ResponseInfo resp = executeHttpRequest(request);
            if (resp == null || resp.statusCode != HttpStatus.SC_OK || resp.content == null) {
                return null;
            }

            List<EspAudio> result = new ArrayList<EspAudio>();
            JSONObject contentJSON = new JSONObject(resp.content);
            JSONArray dataArray = contentJSON.getJSONArray(KEY_DATA);
            for (int i = 0; i < dataArray.length(); i++) {
                EspAudio audio = new EspAudio(EspAudio.Platform.Qingting, EspAudio.Type.Radio);
                JSONObject channelJSON = dataArray.getJSONObject(i);
                long id = channelJSON.getLong(KEY_ID);
                audio.setId(id);
                String title = channelJSON.getString(KEY_TITLE);
                audio.setTitle(title);
                JSONObject thumbJSON = channelJSON.optJSONObject(KEY_THUMBS);
                if (thumbJSON != null) {
                    String thumbSmall = thumbJSON.optString(KEY_THUMB_SMALL);
                    String thumbMedium = thumbJSON.optString(KEY_THUMB_MEDIUM);
                    String thumbLarge = thumbJSON.optString(KEY_THUMB_LARGE);
                    audio.setCoverUrlSmall(thumbSmall);
                    audio.setCoverUrlMiddle(thumbMedium);
                    audio.setCoverUrlLarge(thumbLarge);
                }

                result.add(audio);
            }

            return result;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    private void playing() {
        if (mAccessToken == null) {
            return;
        }
        int dayOfWeek = Calendar.getInstance(Locale.getDefault()).get(Calendar.DAY_OF_WEEK);
        String url = URL_BASE + PATH_PLAYING + dayOfWeek + "?" + mAccessToken;
        HttpGet request = new HttpGet(url);
        try {
            ResponseInfo resp = executeHttpRequest(request);
            if (resp == null || resp.statusCode != HttpStatus.SC_OK || resp.content == null) {
                return;
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
