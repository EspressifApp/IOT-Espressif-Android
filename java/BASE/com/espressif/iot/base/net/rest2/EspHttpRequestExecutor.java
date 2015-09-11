package com.espressif.iot.base.net.rest2;

import java.io.IOException;

import org.apache.http.Header;
import org.apache.http.HttpClientConnection;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseFactory;
import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;
import org.apache.http.impl.DefaultHttpResponseFactory;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicStatusLine;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestExecutor;

class EspHttpRequestExecutor extends HttpRequestExecutor
{
    private static final HttpResponseFactory ResponseFactory = new DefaultHttpResponseFactory();
    
    /**
     * Wait for and receive a response. This method will automatically ignore intermediate responses with status code
     * 1xx.
     * 
     * @param request the request for which to obtain the response
     * @param conn the connection over which the request was sent
     * @param context the context for receiving the response
     * 
     * @return the final response, not yet post-processed
     * 
     * @throws HttpException in case of a protocol or processing problem
     * @throws IOException in case of an I/O problem
     */
    protected HttpResponse doReceiveResponse(final HttpRequest request, final HttpClientConnection conn,
        final HttpContext context)
        throws HttpException, IOException
    {
        HttpResponse response = null;
        // check whether the request is instantly, instantly request don't wait the response
        boolean isInstantly = request.getParams().isParameterTrue(EspHttpRequest.ESP_INSTANTLY);
        if (isInstantly)
        {
            ProtocolVersion version = new ProtocolVersion("HTTP", 1, 1);
            StatusLine statusline = new BasicStatusLine(version, 200, "OK");
            // let the connection used only once to check whether the device is available
            context.setAttribute("timeout", 1);
            response = ResponseFactory.newHttpResponse(statusline, context);
            Header contentLengthHeader = new BasicHeader(HTTP.CONTENT_LEN, "0");
            response.addHeader(contentLengthHeader);
        }
        else
        {
            response = super.doReceiveResponse(request, conn, context);
        }
        
        // for device won't reply "Connection: Keep-Alive" by default, add the header by manual
        if (response != null && response.getFirstHeader(HTTP.CONN_DIRECTIVE) == null)
        {
            response.addHeader(HTTP.CONN_DIRECTIVE, HTTP.CONN_KEEP_ALIVE);
        }
        
        return response;
    }
}
