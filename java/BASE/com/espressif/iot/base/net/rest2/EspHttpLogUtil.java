package com.espressif.iot.base.net.rest2;

import org.json.JSONObject;

import com.espressif.iot.type.net.HeaderPair;

class EspHttpLogUtil
{
    private static final boolean IsUnixOS = true;
    
    /**
     * log Get/Post by curl could be executed in PC
     * 
     * @param isGet whether it is get or post
     * @param jsonObject jsonObject
     * @param url url String
     * @param headers headers if exist
     * @return the curl command could be executed in PC
     */
    static String convertToCurl(boolean isGet, JSONObject jsonObject, String url, HeaderPair... headers)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("curl ");
        if (isGet)
        {
            sb.append("-X GET ");
        }
        else
        {
            sb.append("-X POST ");
        }
        // curl -H
        if (headers != null)
        {
            for (int index = 0; index < headers.length; ++index)
            {
                if (IsUnixOS)
                {
                    sb.append("-H '");
                }
                else
                {
                    sb.append("-H \"");
                }
                sb.append(headers[index].getName());
                sb.append(": ");
                sb.append(headers[index].getValue());
                if (IsUnixOS)
                {
                    sb.append("' ");
                }
                else
                {
                    sb.append("\" ");
                }
            }
        }
        // curl -d
        if (jsonObject != null)
        {
            if (IsUnixOS)
            {
                sb.append("-d '");
            }
            else
            {
                sb.append("-d \"");
            }
            sb.append(jsonObject.toString());
            if (IsUnixOS)
            {
                sb.append("' ");
            }
            else
            {
                sb.append("\" ");
            }
        }
        sb.append(url.replace("https", "http"));
        return sb.toString();
    }
    
}
