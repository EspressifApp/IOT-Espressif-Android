package com.espressif.iot.model.device.http;

public class HttpHeader {
    private String mName;
    private String mValue;

    public HttpHeader(String name, String value) {
        mName = name;
        mValue = value;
    }

    public String getName() {
        return mName;
    }

    public String getValue() {
        return mValue;
    }

    public void setValue(String value) {
        mValue = value;
    }
}
