package com.espressif.iot.type.device.trigger;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EspDeviceTrigger
{
    public static final int INTERVAL_FUNC_SUM = 1;
    public static final int INTERVAL_FUNC_MAX = 2;
    public static final int INTERVAL_FUNC_MIN = 3;
    public static final int INTERVAL_FUNC_AVG = 4;
    public static final int INTERVAL_FUNC_MEDIAN = 5;
    
    public static final int COMPARE_TYPE_EQ = 1;
    public static final int COMPARE_TYPE_NOT_EQ = 2;
    public static final int COMPARE_TYPE_GT = 3;
    public static final int COMPARE_TYPE_GE = 4;
    public static final int COMPARE_TYPE_LT = 5;
    public static final int COMPARE_TYPE_LE = 6;
    
    public static final String STREAM_ALARM = "alarm_status";
    
    public static final String VIA_APP = "app";
    public static final String VIA_EMAIL = "mail";
    
    public static final String SCOPE_ME = "me";
    public static final String SCOPE_USER = "user";
    public static final String SCOPE_OWNER = "owner";
    
    private static final String TMPL_TOKEN_X = "mail-88d6ca90d9c646185516de7ba9e9ad3f3e5d7793";
    private static final String TMPL_TOKEN_Y = "mail-37752bd4947d86af9a9bf90e4820c889cbc8a0ea";
    private static final String TMPL_TOKEN_Z = "mail-7fe1a7125d910d81a0cd9d84b4b0539208061861";
    private static final String TMPL_TOKEN_K = "mail-9814759fed41cfc7c0f8191e22c0c1f3416992f0";
    private static final String TMPL_TOKEN_L = "mail-db98f2eab39313085c578f06bbb084d2e984fd87";
    
    public static final class TriggerRule {
        private Set<String> mViaSet;
        private String mScope;
        
        public TriggerRule() {
            mViaSet = new HashSet<String>();
        }
        
        public void addVia(String via) {
            synchronized (mViaSet) {
                mViaSet.add(via);
            }
        }
        
        public void removeVia(String via) {
            synchronized (mViaSet) {
                mViaSet.remove(via);
            }
        }
        
        public List<String> getViaList() {
            synchronized (mViaSet) {
                List<String> vias = new ArrayList<String>();
                vias.addAll(mViaSet);
                return vias;
            }
        }
        
        public void setScope(String scope) {
            mScope = scope;
        }
        
        public String getScope() {
            return mScope;
        }
        
        @Override
        public String toString() {
            StringBuilder result = new StringBuilder();
            result.append("scope: ").append(mScope).append(", ");
            
            result.append("via: ");
            for (String via : mViaSet) {
                result.append(via).append("|");
            }
            result.delete(result.length() - 1, result.length());
            
            return result.toString();
        }
    }
    
    private long mId;
    private String mName;
    private int mDimension;
    private String mStreamType;
    private int mInterval;
    private int mIntervalFunc;
    private int mCompareType;
    private int mCompareValue;
    private List<TriggerRule> mRuleList;
    private String mTmplToken;
    
    public EspDeviceTrigger() {
        mRuleList = new ArrayList<TriggerRule>();
    }
    
    public void setId(long id) {
        mId = id;
    }
    
    public long getId() {
        return mId;
    }
    
    public void setName(String name) {
        mName = name;
    }
    
    public String getName() {
        return mName;
    }
    
    public void setDimension(int dimension) {
        mDimension = dimension;
        
        switch (dimension) {
            case 0:
                mTmplToken = TMPL_TOKEN_X;
                break;
            case 1:
                mTmplToken = TMPL_TOKEN_Y;
                break;
            case 2:
                mTmplToken = TMPL_TOKEN_Z;
                break;
            case 3:
                mTmplToken = TMPL_TOKEN_K;
                break;
            case 4:
                mTmplToken = TMPL_TOKEN_L;
                break;
            default:
                mTmplToken = null;
                break;
        }
    }
    
    public int getDimension() {
        return mDimension;
    }
    
    public void setStreamType(String type) {
        mStreamType = type;
    }
    
    public String getStreamType() {
        return mStreamType;
    }
    
    public void setInterval(int interval) {
        mInterval = interval;
    }
    
    public int getInterval() {
        return mInterval;
    }
    
    public void setIntervalFunc(int func) {
        mIntervalFunc = func;
    }
    
    public int getIntervalFunc() {
        return mIntervalFunc;
    }
    
    public void setCompareType(int type) {
        mCompareType = type;
    }
    
    public int getCompareType() {
        return mCompareType;
    }
    
    public void setCompareValue(int value) {
        mCompareValue = value;
    }
    
    public int getCompareValue() {
        return mCompareValue;
    }
    
    public void updateTriggerRuleList(List<TriggerRule> rules) {
        synchronized (mRuleList) {
            mRuleList.clear();
            mRuleList.addAll(rules);
        }
    }
    
    public void addTriggerRule(TriggerRule rule) {
        synchronized (mRuleList) {
            mRuleList.add(rule);
        }
    }
    
    public List<TriggerRule> getTriggerRules() {
        synchronized (mRuleList) {
            List<TriggerRule> result = new ArrayList<TriggerRule>();
            result.addAll(mRuleList);
            
            return result;
        }
    }
    
    public String getTmplToken() {
        return mTmplToken;
    }
}
