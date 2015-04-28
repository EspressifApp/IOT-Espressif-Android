package com.espressif.iot.base.net.rest;

import java.net.URI;

import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;

public class EspHttpRequest extends HttpEntityEnclosingRequestBase {

    public final static String METHOD_POST = "POST";
    public final static String METHOD_GET = "GET";
    
    private final String METHOD_NAME;

    public EspHttpRequest(String method) {
        super();
        METHOD_NAME = method;
    }

    public EspHttpRequest(final URI uri, String method) {
        super();
        setURI(uri);
        METHOD_NAME = method;
    }

    /**
     * @throws IllegalArgumentException if the uri is invalid.
     */
    public EspHttpRequest(final String uri, String method) {
        super();
        setURI(URI.create(uri));
        METHOD_NAME = method;
    }

    @Override
    public String getMethod() {
        return METHOD_NAME;
    }

}
