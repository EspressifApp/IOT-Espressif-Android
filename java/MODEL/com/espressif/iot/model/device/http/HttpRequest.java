package com.espressif.iot.model.device.http;

import java.util.List;

public class HttpRequest extends HttpMessage {
    private String mMethod;
    private String mPath;

    public void setMethod(String method) {
        mMethod = method;
    }

    public String getMethod() {
        return mMethod;
    }

    public void setPath(String path) {
        mPath = path;
    }

    public String getPath() {
        return mPath;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        final String blank = " ";
        sb.append(getMethod())
            .append(blank)
            .append(getPath())
            .append(blank)
            .append(getHttpVersion())
            .append(HEADER_SEPARATOR);
        List<HttpHeader> headers = getHeaders();
        for (HttpHeader header : headers) {
            sb.append(header.getName())
                .append(HEADER_CONTENT_SEPARATOR)
                .append(header.getValue())
                .append(HEADER_SEPARATOR);
        }
        sb.append(HEADER_SEPARATOR);

        if (getContent() != null) {
            sb.append(getContent());
        }

        return sb.toString();
    }
}
