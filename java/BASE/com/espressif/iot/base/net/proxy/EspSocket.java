package com.espressif.iot.base.net.proxy;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;

public class EspSocket implements Cloneable
{
    private volatile InputStream mInputStream = null;
    
    private volatile OutputStream mOutputStream = null;
    
    private final Socket mSocket;
    
    public static EspSocket createEspSocket()
    {
        return createEspSocket(new Socket());
    }
    
    public static EspSocket createEspSocket(Socket socket)
    {
        return new EspSocket(socket);
    }
    
    private EspSocket(Socket socket)
    {
        mSocket = socket;
    }
    
    public synchronized int getSoTimeout() throws SocketException {
        return mSocket.getSoTimeout();
    }
    
    public synchronized void setSoTimeout(int timeout)
        throws SocketException
    {
        mSocket.setSoTimeout(timeout);
    }
    
    public void connect(SocketAddress remoteAddr)
        throws IOException
    {
        mSocket.connect(remoteAddr);
    }
    
    public void connect(SocketAddress remoteAddr, int timeout)
        throws IOException
    {
        mSocket.connect(remoteAddr, timeout);
    }
    
    public boolean isConnected()
    {
        return mSocket.isConnected();
    }
    
    public boolean isClosed()
    {
        return mSocket.isClosed();
    }
    
    public InetAddress getLocalAddress()
    {
        return mSocket.getLocalAddress();
    }
    
    public int getLocalPort()
    {
        return mSocket.getLocalPort();
    }
    
    public synchronized InputStream getInputStream()
        throws IOException
    {
        if (mInputStream == null)
        {
            InputStream inputStream = mSocket.getInputStream();
            mInputStream = new DataInputStream(new BufferedInputStream(inputStream));
        }
        return mInputStream;
    }
    
    public synchronized OutputStream getOutputStream()
        throws IOException
    {
        if (mOutputStream == null)
        {
            OutputStream outputStream = mSocket.getOutputStream();
            mOutputStream = new DataOutputStream(new BufferedOutputStream(outputStream));
        }
        return mOutputStream;
    }
    
    public synchronized void close()
        throws IOException
    {
        try
        {
            if (mInputStream != null)
            {
                mInputStream.close();
            }
            if (mOutputStream != null)
            {
                mOutputStream.close();
            }
        }
        finally
        {
            mSocket.close();
        }
    }
}
