package com.espressif.iot.base.net.rest2;

import java.io.IOException;

import javax.net.ssl.SSLHandshakeException;

import org.apache.http.HeaderElement;
import org.apache.http.HeaderElementIterator;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.NoHttpResponseException;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.ClientConnectionManagerFactory;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestExecutor;

class EspHttpClient extends DefaultHttpClient
{
    
    private final static int MAX_RETRY_TIMES = 0;
    
    private static final int HTTP_DEFAULT_PORT = 80;
    
    private static final int HTTPS_DEFAULT_PORT = 443;
    
    private final static int CONNECTION_TIMEOUT = 2 * 1000;
    
    private final static int SO_TIMEOUT = 20 * 1000;
    
    private static class EspHttpClientHolder
    {
        static EspHttpClient instance = new EspHttpClient();
    }
    
    static EspHttpClient getEspHttpClient()
    {
        return EspHttpClientHolder.instance;
    }
    
    private EspHttpClient(final ClientConnectionManager conman, final HttpParams params)
        throws UnsupportedOperationException
    {
        throw new UnsupportedOperationException();
    }
    
    private EspHttpClient(final HttpParams params)
        throws UnsupportedOperationException
    {
        throw new UnsupportedOperationException();
    }
    
    private EspHttpClient()
    {
        super(null, null);
        init();
        // Set Retry Handler
        MyHttpRequestRetryHandler retryHandler = new MyHttpRequestRetryHandler();
        this.setHttpRequestRetryHandler(retryHandler);
    }
    
    private void init()
    {
        BasicHttpParams params = new BasicHttpParams();
        params.setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, CONNECTION_TIMEOUT);
        params.setParameter(CoreConnectionPNames.SO_TIMEOUT, SO_TIMEOUT);
        params.setParameter("timemout", 6);
        this.setParams(params);
        // set keep alive strategy
        this.setKeepAliveStrategy(new ConnectionKeepAliveStrategy()
        {
            public long getKeepAliveDuration(HttpResponse response, HttpContext context)
            { // Honor 'keep-alive' header
                HeaderElementIterator it =
                    new BasicHeaderElementIterator(response.headerIterator(HTTP.CONN_KEEP_ALIVE));
                while (it.hasNext())
                {
                    HeaderElement he = it.nextElement();
                    String param = he.getName();
                    String value = he.getValue();
                    if (value != null && param.equalsIgnoreCase("timeout"))
                    {
                        try
                        {
                            return Long.parseLong(value) * 1000;
                        }
                        catch (NumberFormatException ignore)
                        {
                        }
                    }
                }
                Integer timeout = (Integer)context.getAttribute("timeout");
                if (timeout != null)
                {
                    return timeout.longValue();
                }
                HttpHost target = (HttpHost)context.getAttribute(ExecutionContext.HTTP_TARGET_HOST);
                if ("iot.espressif.cn".equalsIgnoreCase(target.getHostName()))
                {
                    // Keep alive for 30 seconds when connecting to server
                    return 30 * 1000;
                }
                else
                {
                    // otherwise keep alive for 8 seconds when connection to device
                    return 10 * 1000;
                }
            }
        });
    }
    
    @Override
    protected HttpRequestExecutor createRequestExecutor()
    {
        return new EspHttpRequestExecutor();
    }
    
    @Override
    protected ClientConnectionManager createClientConnectionManager()
    {
        SchemeRegistry registry = new SchemeRegistry();
        registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), HTTP_DEFAULT_PORT));
        registry.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), HTTPS_DEFAULT_PORT));
        
        ClientConnectionManager connManager = null;
        HttpParams params = getParams();
        
        ClientConnectionManagerFactory factory = null;
        
        // Try first getting the factory directly as an object.
        factory = (ClientConnectionManagerFactory)params.getParameter(ClientPNames.CONNECTION_MANAGER_FACTORY);
        if (factory == null)
        { // then try getting its class name.
            String className = (String)params.getParameter(ClientPNames.CONNECTION_MANAGER_FACTORY_CLASS_NAME);
            if (className != null)
            {
                try
                {
                    Class<?> clazz = Class.forName(className);
                    factory = (ClientConnectionManagerFactory)clazz.newInstance();
                }
                catch (ClassNotFoundException ex)
                {
                    throw new IllegalStateException("Invalid class name: " + className);
                }
                catch (IllegalAccessException ex)
                {
                    throw new IllegalAccessError(ex.getMessage());
                }
                catch (InstantiationException ex)
                {
                    throw new InstantiationError(ex.getMessage());
                }
            }
        }
        
        if (factory != null)
        {
            connManager = factory.newInstance(params, registry);
        }
        else
        {
            connManager = new ThreadSafeClientConnManager(getParams(), registry);
        }
        
        return connManager;
    }
    
    private static class MyHttpRequestRetryHandler implements HttpRequestRetryHandler
    {
        
        @Override
        public boolean retryRequest(IOException exception, int executionCount, HttpContext context)
        {
            System.out.println("$$$###MyHttpRequestRetryHandler retryRequest");
            if (executionCount >= MAX_RETRY_TIMES)
            {
                // Do not retry if over max retry count
                return false;
            }
            
            if (exception instanceof NoHttpResponseException)
            {
                // Retry if the server dropped connection on us
                return true;
            }
            
            if (exception instanceof SSLHandshakeException)
            {
                // Do not retry on SSL handshake exception
                return false;
            }
            
            HttpRequest request = (HttpRequest)context.getAttribute(ExecutionContext.HTTP_REQUEST);
            boolean idempotent = !(request instanceof HttpEntityEnclosingRequest);
            if (idempotent)
            {
                // Retry if the request is considered idempotentreturn true;
                return true;
            }
            
            return false;
        }
        
    }
    
}
