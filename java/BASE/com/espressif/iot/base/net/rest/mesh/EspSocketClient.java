package com.espressif.iot.base.net.rest.mesh;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import org.apache.log4j.Logger;

public class EspSocketClient
{
    
    private static final Logger log = Logger.getLogger(EspSocketClient.class);
    
    private final Socket mSocket;
    private BufferedReader mReader;
    
    private void __initReader() throws IOException
    {
        mReader = new BufferedReader(new InputStreamReader((mSocket.getInputStream())));
    }
    
    private String __getPrintInfo()
    {
        return "[ localPort:" + this.getLocalPort() + ", targetAddress:" + this.getTargetAddress() + "targetPort:"
            + this.getPort() + "]";
    }
    
    /**
     * close the mSocket when gc will recycle TcpSocketClient
     */
    @Override
    protected void finalize()
        throws Throwable
    {
        close();
        super.finalize();
    }
    
    /**
     * Close the socket
     * 
     * @throws IOException if an I/O error occurs when closing this socket.
     */
    public void close()
        throws IOException
    {
        log.debug(__getPrintInfo() + " is closed");
        mSocket.close();
        if (mReader != null)
        {
            mReader.close();
        }
    }
    
    /**
     * Constructor of TcpSocketClient with an unconnected socket
     */
    public EspSocketClient()
    {
        mSocket = new Socket();
    }
    
    
    /**
     * Returns whether this socket is closed.
     *
     * @return {@code true} if the socket is closed, {@code false} otherwise.
     */
    public boolean isClosed()
    {
        return mSocket.isClosed();
    }
    
    /**
     * Returns the IP address of the target host this socket is connected to, or null if this
     * socket is not yet connected.
     */
    public InetAddress getTargetAddress()
    {
        return mSocket.getInetAddress();
    }
    
    /**
     * Constructor of TcpSocketClient with a connected socket
     * 
     * @param host the host name, or <code>null</code> for the loopback address.
     * @param port the port number.
     * 
     * @exception UnknownHostException if the IP address of the host could not be determined.
     * 
     * @exception IOException if an I/O error occurs when creating the socket.
     * @exception SecurityException if a security manager exists and its <code>checkConnect</code> method doesn't allow
     *                the operation.
     */
    public EspSocketClient(String host, int port)
        throws UnknownHostException, IOException
    {
        mSocket = new Socket(host, port);
        __initReader();
    }
    
    /**
     * Constructor of TcpSocketClient with a connected socket
     * 
     * @param address the IP address.
     * 
     * @param port the port number.
     * 
     * @exception IOException if an I/O error occurs when creating the socket
     */
    public EspSocketClient(InetAddress address, int port)
        throws IOException
    {
        mSocket = new Socket(address, port);
        __initReader();
    }
    
    /**
     * Get the local address to which the socket is bound.
     *
     * @return the local address to which the socket is bound or 
     *         <code>InetAddress.anyLocalAddress()</code>
     *         if the socket is not bound yet.
     */
    public InetAddress getLocalAddress()
    {
        return mSocket.getLocalAddress();
    }
    
    /**
     * Get the local address String to which the socket is bound.
     * @return the local address String to which the socket is bound or 
     *         <code>InetAddress.anyLocalAddress()</code>
     *         if the socket is not bound yet.
     */
    public String getLocalAddressStr(){
        InetAddress inetAddr = mSocket.getLocalAddress();
        String localAddrStr = null;
        if(inetAddr!=null)
        {
            String localAddrRawStr = inetAddr.toString();
            localAddrStr = localAddrRawStr.substring(1,localAddrRawStr.length());
        }
        return localAddrStr;
    }
    
    /**
     * Returns the port number of the target host this socket is connected to, or 0 if this socket
     * is not yet connected.
     */
    public int getPort()
    {
        return mSocket.getPort();
    }
    
    /**
     * Get the local port to which this socket is bound.
     *
     * @return  the local port number to which this socket is bound or -1
     *          if the socket is not bound yet.
     */
    public int getLocalPort()
    {
        return mSocket.getLocalPort();
    }
    
    /**
     * Write the request in the socket
     * @param request the request content String
     * @throws IOException if an I/O error occurs when writing the socket
     */
    public void writeRequest(String request)
        throws IOException
    {
        log.debug(__getPrintInfo() + " writeRequest:\n " + request);
        EspSocketUtil.writeStr2Stream(request, mSocket.getOutputStream());
    }
    
    /**
     * Read one line from the socket
     * @return one line Str except '\r\n' or '\n'
     * @throws IOException if an I/O error occurs when reading the socket
     */
    public String readLine()
        throws IOException
    {
        String result = EspSocketUtil.readLineFromStream(mSocket.getInputStream());
        if(result!=null)
        {
            log.debug(__getPrintInfo() + " readLine():\n " + result);
        }
        return result;
    }
    
    /**
     * Read the response from the socket
     * @return the response content String from the Server
     * @throws IOException if an I/O error occurs when reading the socket
     */
    public String readResponse()
        throws IOException
    {
        String result = EspSocketUtil.readStrFromStream(mSocket.getInputStream());
        log.debug(__getPrintInfo() + " readResponse():\n " + result);
        return result;
    }

    /**
     * Read the response from socket
     * @return the response of EspSocketResponseEntity
     */
    public EspSocketResponseBaseEntity readResponseEntity()
    {
        EspSocketResponseBaseEntity result = null;
        if(mReader!=null)
        {
            result = EspSocketReaderUtil.readHeaderBodyEntity(mReader);
        }
        return result;
    }
    
    /**
     * Enable/disable SO_TIMEOUT with the specified timeout, in milliseconds.
     * 
     * @param timeout the specified timeout, in millisecond or zero means infinite
     * @return whether the action is suc
     */
    public boolean setSoTimeout(int timeout)
    {
        try
        {
            mSocket.setSoTimeout(timeout);
            return true;
        }
        catch (SocketException e)
        {
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Connect to the host's specified port
     * @param host the host of server
     * @param port the port which to be connected
     * @return whether the connection is build up success
     */
    public boolean connect(String host, int port)
    {
        InetAddress addr = null;
        try
        {
            addr = InetAddress.getByName(host);
        }
        catch (UnknownHostException e)
        {
            e.printStackTrace();
            return false;
        }
        SocketAddress sockaddr = new InetSocketAddress(addr, port);
        boolean result = connect(sockaddr);
        log.debug(__getPrintInfo() + " connect to host: " + host + ", port: " + port + ", suc is " + result);
        return result;
    }
    
    /**
     * Connect this socket to the server with a specified timeout value.
     * A timeout of zero is interpreted as an infinite timeout. The connection
     * will then block until established or an error occurs.
     *
     * @param   endpoint the <code>SocketAddress</code>
     * @param   timeout  the timeout value to be used in milliseconds.
     * @return  whether the connection is build up success
     */
    public boolean connect(String host, int port, int timeout)
    {
        InetAddress addr = null;
        try
        {
            addr = InetAddress.getByName(host);
        }
        catch (UnknownHostException e)
        {
            e.printStackTrace();
            return false;
        }
        SocketAddress sockaddr = new InetSocketAddress(addr, port);
        boolean result = connect(sockaddr, timeout);
        log.debug(__getPrintInfo() + " connect to host: " + host + ", timeout(milliseconds): " + timeout + ", port: "
            + port + ", suc is " + result);
        return result;
    }
    
    /**
     * Connect this socket to the server.
     * @param endpoint the <code>SocketAddress</code>
     * @return whether the connection is build up success
     */
    private boolean connect(SocketAddress endpoint)
    {
        try
        {
            mSocket.connect(endpoint);
            __initReader();
            return true;
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Connect this socket to the server with a specified timeout value.
     * A timeout of zero is interpreted as an infinite timeout. The connection
     * will then block until established or an error occurs.
     *
     * @param   endpoint the <code>SocketAddress</code>
     * @param   timeout  the timeout value to be used in milliseconds.
     * @return  whether the connection is build up success
     */
    private boolean connect(SocketAddress endpoint, int timeout)
    {
        try
        {
            mSocket.connect(endpoint, timeout);
            __initReader();
            return true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return false;
    }
    
    public static void main(String args[])
    {
        EspSocketClient client = new EspSocketClient();
        client.setSoTimeout(8000);
        System.out.println("start connecting...");
        long startTime = System.currentTimeMillis();
        boolean isConnect = client.connect("127.0.0.1", 8000, 0);
        System.out.println("cost: " + (System.currentTimeMillis() - startTime));
        if (!isConnect)
        {
            System.out.println("connect fail");
            return;
        }
        
        System.out.println(client.getLocalAddress());
        System.out.println(client.getLocalPort());
        try
        {
            EspSocketRequestBaseEntity request = new EspSocketRequestBaseEntity("GET", "http://192.168.1.1/config?command=light");
            client.writeRequest(request.toString());
            EspSocketResponseBaseEntity response = client.readResponseEntity();
            System.out.println("$$$$$$$$client receive the response 1: \n" + response);
            response = client.readResponseEntity();
            System.out.println("$$$$$$$$client receive the response 2: \n" + response);
            client.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
    
