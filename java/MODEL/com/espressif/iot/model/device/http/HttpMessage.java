package com.espressif.iot.model.device.http;

import java.util.ArrayList;
import java.util.List;

public abstract class HttpMessage {
    private String mHttpVer;
    private List<HttpHeader> mHeaders;
    private String mContent;

    protected static final String HEADER_END = "\r\n\r\n";
    protected static final String HEADER_SEPARATOR = "\r\n";
    protected static final String HEADER_CONTENT_SEPARATOR = ": ";

    public static final String CONTENT_LENGTH = "Content-Length";

    public static final String HTTP_1_0 = "HTTP/1.0";
    public static final String HTTP_1_1 = "HTTP/1.1";

    public HttpMessage() {
        mHttpVer = HTTP_1_0;
        mHeaders = new ArrayList<HttpHeader>();
    }

    public void addHeader(String name, String value) {
        addHeader(new HttpHeader(name, value));
    }

    public void addHeader(HttpHeader header) {
        for (HttpHeader h : mHeaders) {
            if (h.getName().equals(header.getName())) {
                h.setValue(header.getValue());
                return;
            }
        }

        mHeaders.add(header);
    }

    public List<HttpHeader> getHeaders() {
        List<HttpHeader> headers = new ArrayList<HttpHeader>();
        headers.addAll(mHeaders);

        return headers;
    }

    public String getHeaderValue(String name) {
        for (HttpHeader h : mHeaders) {
            if (name.equals(h.getName())) {
                return h.getValue();
            }
        }

        return null;
    }

    public int getContentLength() {
        int result = 0;

        String len = getHeaderValue(CONTENT_LENGTH);
        if (len != null) {
            try {
                result = Integer.parseInt(len);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    public void setContent(String content) {
        mContent = content;
    }

    public String getContent() {
        return mContent;
    }

    public void setHttpVersion(String version) {
        mHttpVer = version;
    }

    public String getHttpVersion() {
        return mHttpVer;
    }
}
