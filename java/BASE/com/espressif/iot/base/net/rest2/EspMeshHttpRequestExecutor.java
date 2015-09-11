package com.espressif.iot.base.net.rest2;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.ProtocolException;

import org.apache.http.Header;
import org.apache.http.HttpClientConnection;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseFactory;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;
import org.apache.http.conn.ManagedClientConnection;
import org.apache.http.impl.DefaultHttpResponseFactory;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicStatusLine;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestExecutor;
import org.apache.log4j.Logger;

import com.espressif.iot.util.MeshUtil;

class EspMeshHttpRequestExecutor extends HttpRequestExecutor
{
    
    private static final Logger log = Logger.getLogger(EspMeshHttpRequestExecutor.class);
    
    // {"sip":"C0A80102","sport":"F1F2","mdev":"18FE34971268", others...}\r\n
    
    private static final String TAG_SIP = "\"sip\":\"";
    
    private static final String TAG_SPORT = "\"sport\":\"";
    
    private static final HttpResponseFactory ResponseFactory = new DefaultHttpResponseFactory();
    
    // private static final String TAG_MDEV = "\"mdev\":\"";
    
    private int findPos(byte[] contents, String target)
    {
        String contentsStr = null;
        try
        {
            contentsStr = new String(contents, "ISO-8859-1");
        }
        catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
        }
        return contentsStr.indexOf(target);
    }
    
    private byte[] updateBytes(byte[] origin, int offset, int count, byte[] newBytes)
    {
        for (int indexOrigin = offset, indexNew = 0; indexNew < count; ++indexOrigin, ++indexNew)
        {
            origin[indexOrigin] = newBytes[indexNew];
        }
        return origin;
    }
    
    private EspStringEntity updateEntity(EspStringEntity entity, InetAddress localAddr, int localPort)
    {
        String sip = MeshUtil.getIpAddressForMesh(localAddr.getHostAddress());
        String sport = MeshUtil.getPortForMesh(localPort);
        
        byte[] sipBytes = sip.getBytes();
        byte[] sportBytes = sport.getBytes();
        
        // get content
        byte[] contentBytes = entity.getContentBytes();
        log.debug("updateEntity() origin entity:" + new String(contentBytes));
        // update sip
        int indexSip = findPos(contentBytes, TAG_SIP) + TAG_SIP.length();
        if (indexSip < 0)
        {
            throw new IllegalArgumentException("indexSip < 0");
        }
        contentBytes = updateBytes(contentBytes, indexSip, sipBytes.length, sipBytes);
        
        // update sport
        int indexSport = findPos(contentBytes, TAG_SPORT) + TAG_SPORT.length();
        if (indexSport < 0)
        {
            throw new IllegalArgumentException("indexSport < 0");
        }
        contentBytes = updateBytes(contentBytes, indexSport, sportBytes.length, sportBytes);
        
        // only use default charset currently
        EspStringEntity newEntity = null;
        try
        {
            newEntity = new EspStringEntity(new String(contentBytes));
        }
        catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
        }
        log.debug("updateEntity() newEntity:" + new String(newEntity.getContentBytes()));
        return newEntity;
    }
    
    private void updateRequest(final HttpEntityEnclosingRequest request, final ManagedClientConnection conn,
        final HttpContext context)
    {
        InetAddress localAddr = conn.getLocalAddress();
        int localPort = conn.getLocalPort();
        log.debug("updateRequest() localAddr:" + localAddr + ",localPort:" + localPort);
        EspStringEntity entity = (EspStringEntity)request.getEntity();
        updateEntity(entity, localAddr, localPort);
    }
    
    /**
     * Send a request over a connection. This method also handles the expect-continue handshake if necessary. If it does
     * not have to handle an expect-continue handshake, it will not use the connection for reading or anything else that
     * depends on data coming in over the connection.
     * 
     * @param request the request to send, already {@link #preProcess preprocessed}
     * @param conn the connection over which to send the request, already established
     * @param context the context for sending the request
     * 
     * @return a terminal response received as part of an expect-continue handshake, or <code>null</code> if the
     *         expect-continue handshake is not used
     * 
     * @throws HttpException in case of a protocol or processing problem
     * @throws IOException in case of an I/O problem
     */
    protected HttpResponse doSendRequest(final HttpRequest request, final HttpClientConnection conn,
        final HttpContext context)
        throws IOException, HttpException
    {
        log.debug("doSendRequest()");
        
        if (request == null)
        {
            throw new IllegalArgumentException("HTTP request may not be null");
        }
        if (conn == null)
        {
            throw new IllegalArgumentException("HTTP connection may not be null");
        }
        if (context == null)
        {
            throw new IllegalArgumentException("HTTP context may not be null");
        }
        
        HttpResponse response = null;
        context.setAttribute(ExecutionContext.HTTP_REQ_SENT, Boolean.FALSE);
        
        // update sip and sport
        if (!request.getRequestLine().getMethod().equals(EspHttpRequest.METHOD_COMMAND))
        {
            updateRequest((HttpEntityEnclosingRequest)request, (ManagedClientConnection)conn, context);
        }
        
        // don't send header when using "COMMAND"
        if (!request.getRequestLine().getMethod().equals(EspHttpRequest.METHOD_COMMAND))
        {
            conn.sendRequestHeader(request);
        }
        
        if (request instanceof HttpEntityEnclosingRequest)
        {
            // Check for expect-continue handshake. We have to flush the
            // headers and wait for an 100-continue response to handle it.
            // If we get a different response, we must not send the entity.
            boolean sendentity = true;
            final ProtocolVersion ver = request.getRequestLine().getProtocolVersion();
            if (((HttpEntityEnclosingRequest)request).expectContinue() && !ver.lessEquals(HttpVersion.HTTP_1_0))
            {
                
                conn.flush();
                // As suggested by RFC 2616 section 8.2.3, we don't wait for a
                // 100-continue response forever. On timeout, send the entity.
                int tms = request.getParams().getIntParameter(CoreProtocolPNames.WAIT_FOR_CONTINUE, 2000);
                
                if (conn.isResponseAvailable(tms))
                {
                    response = conn.receiveResponseHeader();
                    if (canResponseHaveBody(request, response))
                    {
                        conn.receiveResponseEntity(response);
                    }
                    int status = response.getStatusLine().getStatusCode();
                    if (status < 200)
                    {
                        if (status != HttpStatus.SC_CONTINUE)
                        {
                            throw new ProtocolException("Unexpected response: " + response.getStatusLine());
                        }
                        // discard 100-continue
                        response = null;
                    }
                    else
                    {
                        sendentity = false;
                    }
                }
            }
            if (sendentity)
            {
                conn.sendRequestEntity((HttpEntityEnclosingRequest)request);
            }
        }
        conn.flush();
        context.setAttribute(ExecutionContext.HTTP_REQ_SENT, Boolean.TRUE);
        return response;
    }
    
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
        log.debug("EspMeshHttpRequestExecutor::doReceiveResponse()");
        if (request == null)
        {
            throw new IllegalArgumentException("HTTP request may not be null");
        }
        if (conn == null)
        {
            throw new IllegalArgumentException("HTTP connection may not be null");
        }
        if (context == null)
        {
            throw new IllegalArgumentException("HTTP context may not be null");
        }
        
        HttpResponse response = null;
        int statuscode = 0;
        
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
            if (!request.getRequestLine().getMethod().equals(EspHttpRequest.METHOD_COMMAND))
            {
                while (response == null || statuscode < HttpStatus.SC_OK)
                {
                    
                    response = conn.receiveResponseHeader();
                    if (canResponseHaveBody(request, response))
                    {
                        conn.receiveResponseEntity(response);
                    }
                    statuscode = response.getStatusLine().getStatusCode();
                    
                } // while intermediate response
            }
            else
            {
                ProtocolVersion version = new ProtocolVersion("HTTP", 1, 1);
                StatusLine statusline = new BasicStatusLine(version, 200, "OK");
                response = ResponseFactory.newHttpResponse(statusline, context);
                // copy request headers
                // Header[] requestHeaders = request.getAllHeaders();
                // for (Header requestHeader : requestHeaders) {
                // System.out.println("requestHeader:" + requestHeader);
                // response.addHeader(requestHeader);
                // }
                
                Header[] contentLengthHeader = request.getHeaders(HTTP.CONTENT_LEN);
                if (contentLengthHeader == null || contentLengthHeader.length != 1)
                {
                    throw new IllegalArgumentException("contentLengthHeader == null || contentLengthHeader.length != 1");
                }
                // at the moment, mesh command request and response len is the same
                response.addHeader(contentLengthHeader[0]);
                
                conn.receiveResponseEntity(response);
            }
        }
        
        // for device won't reply "Connection: Keep-Alive" by default, add the header by manual
        if (response != null && response.getFirstHeader(HTTP.CONN_DIRECTIVE) == null)
        {
            response.addHeader(HTTP.CONN_DIRECTIVE, HTTP.CONN_KEEP_ALIVE);
        }
        
        return response;
        
    }
}
