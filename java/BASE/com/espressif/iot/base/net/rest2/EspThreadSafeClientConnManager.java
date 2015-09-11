package com.espressif.iot.base.net.rest2;

import java.util.concurrent.TimeUnit;

import org.apache.http.conn.ClientConnectionRequest;
import org.apache.http.conn.ConnectionPoolTimeoutException;
import org.apache.http.conn.ManagedClientConnection;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.conn.tsccm.BasicPoolEntry;
import org.apache.http.impl.conn.tsccm.PoolEntryRequest;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.HttpParams;

public class EspThreadSafeClientConnManager extends ThreadSafeClientConnManager
{
    
    public EspThreadSafeClientConnManager(HttpParams params, SchemeRegistry schreg)
    {
        super(params, schreg);
    }
    
    public ClientConnectionRequest requestConnection(final HttpRoute route, final Object state)
    {
        
        final PoolEntryRequest poolRequest = connectionPool.requestPoolEntry(route, state);
        
        return new ClientConnectionRequest()
        {
            
            public void abortRequest()
            {
                poolRequest.abortRequest();
            }
            
            public ManagedClientConnection getConnection(long timeout, TimeUnit tunit)
                throws InterruptedException, ConnectionPoolTimeoutException
            {
                if (route == null)
                {
                    throw new IllegalArgumentException("Route may not be null.");
                }
                
                // if (log.isDebugEnabled()) {
                // log.debug("ThreadSafeClientConnManager.getConnection: "
                // + route + ", timeout = " + timeout);
                // }
                
                BasicPoolEntry entry = poolRequest.getPoolEntry(timeout, tunit);
                // return new BasicPooledConnAdapter(ThreadSafeClientConnManager.this, entry);
                return new EspMeshBasicPooledConnAdapter(EspThreadSafeClientConnManager.this, entry);
            }
            
        };
        
    }
}
