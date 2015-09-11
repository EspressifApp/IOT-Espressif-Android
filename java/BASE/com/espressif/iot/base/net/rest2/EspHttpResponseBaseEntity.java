package com.espressif.iot.base.net.rest2;

import org.json.JSONException;
import org.json.JSONObject;

public class EspHttpResponseBaseEntity implements IEspHttpResponse
{
    private static final int NO_STATUS_VALUE = -1;
    
    private static final int NO_NONCE_VALUE = -1;
    
    private static final String STATUS = "status";
    
    private static final String NONCE = "nonce";
    
    private int mStatus;
    
    private long mNonce;
    
    private boolean mIsValid;
    
    private JSONObject mJson;
    
    public EspHttpResponseBaseEntity(String response)
    {
        __initResp(response);
    }
    
    private void __initResp(String response)
    {
        if (response == null)
        {
            // invalid format
            this.mIsValid = false;
            this.mStatus = NO_STATUS_VALUE;
            this.mNonce = NO_NONCE_VALUE;
            this.mJson = null;
            return;
        }
        try
        {
            JSONObject respJson = new JSONObject(response);
            this.mIsValid = true;
            if (respJson.isNull(STATUS))
            {
                this.mStatus = NO_STATUS_VALUE;
            }
            else
            {
                this.mStatus = respJson.getInt(STATUS);
            }
            if (respJson.isNull(NONCE))
            {
                this.mNonce = NO_NONCE_VALUE;
            }
            else
            {
                this.mNonce = respJson.getLong(NONCE);
            }
            this.mJson = respJson;
        }
        catch (JSONException e)
        {
            // invalid format
            e.printStackTrace();
            this.mIsValid = false;
            this.mStatus = NO_STATUS_VALUE;
            this.mNonce = NO_NONCE_VALUE;
            this.mJson = null;
        }
    }
    
    public int getStatus()
    {
        return this.mStatus;
    }
    
    public boolean isStatusExist()
    {
        return this.mStatus != NO_STATUS_VALUE;
    }
    
    public long getNonce()
    {
        return this.mNonce;
    }
    
    public boolean isNonceExist()
    {
        return this.mNonce != NO_NONCE_VALUE;
    }
    
    public boolean isValid()
    {
        return this.mIsValid;
    }
    
    public JSONObject getJson()
    {
        return this.mJson;
    }
}
