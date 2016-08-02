package com.espressif.iot.command.group;

import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.espressif.iot.base.api.EspBaseApiUtil;
import com.espressif.iot.command.group.IEspCommandGroupCreateInternet;
import com.espressif.iot.type.net.HeaderPair;

public class EspCommandGroupCreateInternet implements IEspCommandGroupCreateInternet {

    @Override
    public long doCommandCreateGroupInternet(String userKey, String groupName) {
        return doCommandCreateGroupInternet(userKey, groupName, 0);
    }

    @Override
    public long doCommandCreateGroupInternet(String userKey, String groupName, int groupType) {
        String headerKey = Authorization;
        String headerValue = Token + " " + userKey;
        HeaderPair header = new HeaderPair(headerKey, headerValue);

        try {
            JSONObject contentJSON = new JSONObject();
            contentJSON.put(KEY_GROUP_NAME, groupName);
            JSONObject descJSON = new JSONObject();
            descJSON.put(KEY_GROUP_TYPE, groupType);
            contentJSON.put(KEY_GROUP_DESC, descJSON.toString());

            JSONArray groupArray = new JSONArray();
            groupArray.put(contentJSON);
            JSONObject postJSON = new JSONObject();
            postJSON.put(KEY_DEVICE_GROUPS, groupArray);

            JSONObject resultJSON = EspBaseApiUtil.Post(URL_CREATE, postJSON, header);
            if (resultJSON != null) {
                int status = resultJSON.getInt(Status);
                if (status == HttpStatus.SC_OK) {
                    long groupId = resultJSON.getJSONArray(KEY_DEVICE_GROUPS).getJSONObject(0).getLong(Id);
                    return groupId;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return -1;
    }
}
