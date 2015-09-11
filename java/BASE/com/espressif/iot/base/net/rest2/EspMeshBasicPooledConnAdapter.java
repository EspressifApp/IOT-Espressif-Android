package com.espressif.iot.base.net.rest2;

import java.io.IOException;

import org.apache.http.HttpConnectionMetrics;
import org.apache.http.conn.OperatedClientConnection;
import org.apache.http.impl.conn.AbstractPoolEntry;
import org.apache.http.impl.conn.tsccm.BasicPooledConnAdapter;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;

class EspMeshBasicPooledConnAdapter extends BasicPooledConnAdapter
{
    
    private static final int MAX_PACKAGE_LEN = 1300;
    
    EspMeshBasicPooledConnAdapter(ThreadSafeClientConnManager tsccm, AbstractPoolEntry entry)
    {
        super(tsccm, entry);
    }
    
    @Override
    public void flush()
        throws IOException
    {
        assertNotAborted();
        OperatedClientConnection conn = getWrappedConnection();
        assertValid(conn);
        
        HttpConnectionMetrics metrics = this.getMetrics();
        boolean isOverflow = metrics.getRequestCount() > MAX_PACKAGE_LEN;
        if (isOverflow)
        {
            throw new IllegalArgumentException("too many bytes to be sent(> MAX_PACKAGE_LEN: " + MAX_PACKAGE_LEN + " )");
        }
        
        // InetAddress localAddr = this.getLocalAddress();
        // int localPort = this.getLocalPort();
        // InetAddress remoteAddr = this.getRemoteAddress();
        // int remotePort = this.getRemotePort();
        //
        // System.out.println("bh localAddr:" + localAddr);
        // System.out.println("bh localPort:" + localPort);
        // System.out.println("bh remoteAddr:" + remoteAddr);
        // System.out.println("bh remotePort:" + remotePort);
        //
        // HttpConnectionMetrics metrics = this.getMetrics();
        // long receivedBytesCount = metrics.getReceivedBytesCount();
        // long sentBytesCount = metrics.getSentBytesCount();
        // long requestCount = metrics.getRequestCount();
        // long responseCount = metrics.getResponseCount();
        //
        // System.out.println("receivedBytesCount:" + receivedBytesCount);
        // System.out.println("sentBytesCount:" + sentBytesCount);
        // System.out.println("requestCount:" + requestCount);
        // System.out.println("responseCount:" + responseCount);
        
        conn.flush();
    }
    
}
