package com.espressif.iot.base.net.rest2;

import java.net.URI;

import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;

class EspHttpRequest extends HttpEntityEnclosingRequestBase
{
    final static String METHOD_POST = "POST";
    
    final static String METHOD_GET = "GET";
    
    final static String ESP_INSTANTLY = "ESP_INSTANTLY";
    
    // dummy runnable do nothing, it is used by post instantly
    final static Runnable ESP_DUMMY_RUNNABLE = new Runnable()
    {
        @Override
        public void run()
        {
        }
    };
    
    private final String METHOD_NAME;
    
    EspHttpRequest(String method)
    {
        super();
        METHOD_NAME = method;
    }
    
    EspHttpRequest(final URI uri, String method)
    {
        super();
        setURI(uri);
        METHOD_NAME = method;
    }
    
    EspHttpRequest(final String uri, String method)
    {
        super();
        setURI(URI.create(uri));
        METHOD_NAME = method;
    }
    
    @Override
    public String getMethod()
    {
        return METHOD_NAME;
    }
    
}