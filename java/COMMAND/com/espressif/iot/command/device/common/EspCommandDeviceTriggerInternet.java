package com.espressif.iot.command.device.common;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.espressif.iot.base.api.EspBaseApiUtil;
import com.espressif.iot.device.IEspDevice;
import com.espressif.iot.type.device.trigger.EspDeviceTrigger;
import com.espressif.iot.type.device.trigger.EspDeviceTrigger.TriggerRule;
import com.espressif.iot.type.net.HeaderPair;

public class EspCommandDeviceTriggerInternet implements IEspCommandDeviceTriggerInternet
{

    @Override
    public List<EspDeviceTrigger> getTriggersInternet(IEspDevice device)
    {
        HeaderPair header = new HeaderPair(Authorization, Token + " " + device.getKey());
        
        JSONObject responseJSON = EspBaseApiUtil.Get(URL, header);
        if (responseJSON != null) {
            try
            {
                int httpStatus = responseJSON.getInt(Status);
                if (httpStatus == HttpURLConnection.HTTP_OK) {
                    List<EspDeviceTrigger> result = new ArrayList<EspDeviceTrigger>();
                    JSONArray triggerArray = responseJSON.getJSONArray(KEY_TRIGGERS);
                    for (int i = 0; i < triggerArray.length(); i++) {
                        JSONObject triggerJSON = triggerArray.getJSONObject(i);
                        EspDeviceTrigger trigger = generateTrigger(triggerJSON);
                        result.add(trigger);
                    }
                    
                    return result;
                }
            }
            catch (JSONException e)
            {
                e.printStackTrace();
            }
        }
        
        return null;
    }
    
    @Override
    public long createTriggerInternet(IEspDevice device, EspDeviceTrigger trigger)
    {
        HeaderPair header = new HeaderPair(Authorization, Token + " " + device.getKey());
        try
        {
            JSONObject postJSON = new JSONObject();
            
            JSONObject triggerJSON = new JSONObject();
            triggerJSON.put(KEY_NAME, trigger.getName());
            triggerJSON.put(KEY_DIMENSION, trigger.getDimension());
            triggerJSON.put(KEY_STREAM, trigger.getStreamType());
            triggerJSON.put(KEY_INTERVAL, trigger.getInterval());
            triggerJSON.put(KEY_INTERVAL_FUNC, trigger.getIntervalFunc());
            triggerJSON.put(KEY_COMPARE_TYPE, trigger.getCompareType());
            triggerJSON.put(KEY_COMPARE_VALUE, trigger.getCompareValue());
            
            postJSON.put(KEY_TRIGGER, triggerJSON);
            
            JSONObject responseJSON = EspBaseApiUtil.Post(URL, postJSON, header);
            if (responseJSON != null) {
                int httpStatus = responseJSON.getInt(Status);
                if (httpStatus == HttpURLConnection.HTTP_OK) {
                    JSONObject createdTriggerJSON = responseJSON.getJSONObject(KEY_TRIGGER);
                    long id = createdTriggerJSON.getLong(KEY_ID);
                    return id;
                }
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        
        return -1;
    }
    
    @Override
    public boolean updateTriggerInternet(IEspDevice device, EspDeviceTrigger trigger)
    {
        HeaderPair header = new HeaderPair(Authorization, Token + " " + device.getKey());
        try
        {
            JSONObject postJSON = new JSONObject();
            
            JSONObject triggerJSON = new JSONObject();
            triggerJSON.put(KEY_ID, trigger.getId());
            triggerJSON.put(KEY_NAME, trigger.getName());
            triggerJSON.put(KEY_DIMENSION, trigger.getDimension());
            triggerJSON.put(KEY_STREAM, trigger.getStreamType());
            triggerJSON.put(KEY_INTERVAL, trigger.getInterval());
            triggerJSON.put(KEY_INTERVAL_FUNC, trigger.getIntervalFunc());
            triggerJSON.put(KEY_COMPARE_TYPE, trigger.getCompareType());
            triggerJSON.put(KEY_COMPARE_VALUE, trigger.getCompareValue());
            
            JSONArray ruleArray = new JSONArray();
            List<TriggerRule> ruleList = trigger.getTriggerRules();
            for (TriggerRule rule : ruleList) {
                String scope = rule.getScope();
                String via = rule.getViaList().get(0);
                String tmplToken = trigger.getTmplToken();
                
                JSONObject ruleJSON = new JSONObject();
                ruleJSON.put(KEY_SCOPE, scope);
                ruleJSON.put(KEY_VIA, via);
                ruleJSON.put(KEY_TMPL_TOKEN, tmplToken);
                
                ruleArray.put(ruleJSON);
            }
            triggerJSON.put(KEY_NOTIFY_RULES, ruleArray);
            
            postJSON.put(KEY_TRIGGER, triggerJSON);
            
            String url = URL + "?" + METHOD_PUT;
            JSONObject responseJSON = EspBaseApiUtil.Post(url, postJSON, header);
            if (responseJSON != null) {
                int httpStatus = responseJSON.getInt(Status);
                if (httpStatus == HttpURLConnection.HTTP_OK) {
                    return true;
                }
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        
        return false;
    }
    
    @Override
    public boolean deleteTriggerInternet(IEspDevice device, long id)
    {
        HeaderPair header = new HeaderPair(Authorization, Token + " " + device.getKey());
        try
        {
            JSONObject postJSON = new JSONObject();
            JSONObject triggerJSON = new JSONObject();
            triggerJSON.put(KEY_ID, id);
            postJSON.put(KEY_TRIGGER, triggerJSON);
            
            String url = URL + "?" + METHOD_DELETE;
            JSONObject responseJSON = EspBaseApiUtil.Post(url, postJSON, header);
            if (responseJSON != null) {
                int httpStatus= responseJSON.getInt(Status);
                if (httpStatus == HttpURLConnection.HTTP_OK) {
                    return true;
                }
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        
        return false;
    }
    
    private EspDeviceTrigger generateTrigger(JSONObject triggerJSON) throws JSONException {
        long id = triggerJSON.getLong(KEY_ID);
        String name = triggerJSON.getString(KEY_NAME);
        int dimension = triggerJSON.getInt(KEY_DIMENSION);
        String stream = triggerJSON.optString(KEY_STREAM, null);
        if (stream == null) {
            stream = EspDeviceTrigger.STREAM_ALARM;
        }
        int interval = triggerJSON.getInt(KEY_INTERVAL);
        int intervalFunc = triggerJSON.getInt(KEY_INTERVAL_FUNC);
        int compareType = triggerJSON.getInt(KEY_COMPARE_TYPE);
        int compareValue = triggerJSON.getInt(KEY_COMPARE_VALUE);
        
        EspDeviceTrigger trigger = new EspDeviceTrigger();
        trigger.setId(id);
        trigger.setName(name);
        trigger.setDimension(dimension);
        trigger.setStreamType(stream);
        trigger.setInterval(interval);
        trigger.setIntervalFunc(intervalFunc);
        trigger.setCompareType(compareType);
        trigger.setCompareValue(compareValue);
        
        JSONArray ruleArray = triggerJSON.optJSONArray(KEY_NOTIFY_RULES);
        if (ruleArray != null) {
            for (int i = 0; i < ruleArray.length(); i++) {
                JSONObject ruleJSON = ruleArray.getJSONObject(i);
                
                String scope = ruleJSON.getString(KEY_SCOPE);
                if (scope.equals("1")) {
                    scope = EspDeviceTrigger.SCOPE_ME;
                }
                String via = ruleJSON.getString(KEY_VIA);
                if (via.equals("1")) {
                    via = EspDeviceTrigger.VIA_APP;
                } else if (via.equals("2")) {
                    via = EspDeviceTrigger.VIA_EMAIL;
                }
                
                TriggerRule rule = new TriggerRule();
                rule.setScope(scope);
                rule.addVia(via);
                
                trigger.addTriggerRule(rule);
            }
        }
        
        return trigger;
    }
}
