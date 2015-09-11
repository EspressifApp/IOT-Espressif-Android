package com.espressif.iot.base.net.rest2;

import java.io.UnsupportedEncodingException;

import org.apache.http.entity.StringEntity;

class EspStringEntity extends StringEntity
{
    
    EspStringEntity(final String s, String charset)
        throws UnsupportedEncodingException
    {
        super(s, charset);
    }
    
    EspStringEntity(final String s)
        throws UnsupportedEncodingException
    {
        this(s, "ISO-8859-1");
    }
    
    public byte[] getContentBytes()
    {
        return this.content;
    }
}
