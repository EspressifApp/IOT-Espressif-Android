package com.espressif.iot.base.net.proxy;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;

public class EspSocketUtil
{
    private static final boolean DEBUG = true;
    private static final boolean USE_LOG4J = true;
    private static final Class<?> CLASS = EspSocketUtil.class;
    
    private static final byte LF = '\r';
    
    private static final byte CR = '\n';
    
    /**
     * read one byte from the socket
     * 
     * @param sckInputStream the intputStream of socket
     * @return one byte value
     * @throws IOException the IOException
     */
    public static byte readByte(InputStream sckInputStream)
        throws IOException
    {
        int _byte = sckInputStream.read();
        if (_byte == -1)
        {
            MeshLog.i(DEBUG, USE_LOG4J, CLASS, "readByte() -1, throw IOException");
            throw new IOException();
        }
        return (byte)_byte;
    }
    
    /**
     * read some bytes from the socket into the buffer
     * 
     * @param sckInputStream the intputStream of socket
     * @param buffer the buffer
     * @param byteOffset the byte of offset
     * @param byteCount the count to be read
     * @throws IOException the IOException
     */
    public static void readBytes(InputStream sckInputStream, byte[] buffer, int byteOffset, int byteCount)
        throws IOException
    {
        int _byteCount = sckInputStream.read(buffer, byteOffset, byteCount);
        if (_byteCount != byteCount)
        {
            MeshLog.i(DEBUG, USE_LOG4J, CLASS, "readBytes() _byteCount != byteCount, throw IOException");
            throw new IOException();
        }
    }
    
    /**
     * write some bytes to the socket
     * 
     * @param sckOutputStream the outputStream of socket
     * @param buffer the buffer
     * @throws IOException the IOException
     */
    public static void writeBytes(OutputStream sckOutputStream, byte[] buffer)
        throws IOException
    {
        try
        {
            sckOutputStream.write(buffer);
        }
        catch (IOException e)
        {
            MeshLog.e(DEBUG, USE_LOG4J, CLASS, "writeBytes() IOException, throw IOException");
            throw e;
        }
    }
    
    /**
     * flush written bytes to the socket
     * 
     * @param sckOutputStream the outputStream of socket
     * @throws IOException the IOException
     */
    public static void flush(OutputStream sckOutputStream)
        throws IOException
    {
        try
        {
            sckOutputStream.flush();
        }
        catch (IOException e)
        {
            MeshLog.e(DEBUG, USE_LOG4J, CLASS, "flush() IOException, throw IOException");
            throw e;
        }
    }
    
    /**
     * read http header from socket into buffer
     * 
     * @param sckInputStream the intputStream of socket
     * @param buffer the buffer to store Http Header
     * @param byteOffset the byte of offset
     * @return the count of header
     * @throws IOException the IOException
     */
    public static int readHttpHeader(InputStream sckInputStream, byte[] buffer, int byteOffset)
        throws IOException
    {
        int count = 0;
        byte last = 0;
        byte current = 0;
        boolean isEscapeOnce = false;
        boolean isEscapeTwiceTry = false;
        buffer[byteOffset + count++] = last = readByte(sckInputStream);
        while (true)
        {
            // read one byte into buffer
            current = readByte(sckInputStream);
            buffer[byteOffset + count++] = current;
            if (!isEscapeTwiceTry && last == LF && current == CR)
            {
                isEscapeOnce = true;
            }
            else if (isEscapeOnce)
            {
                isEscapeOnce = false;
                isEscapeTwiceTry = true;
            }
            else if (isEscapeTwiceTry)
            {
                if (last == LF && current == CR)
                {
                    break;
                }
                isEscapeTwiceTry = false;
            }
            last = current;
        }
        return count;
    }
    
    /**
     * find Http Header Value by its Header Key
     * 
     * @param buffer the buffer stored HttpHeader
     * @param byteOffset the byte of HttpHeader
     * @param byteCount the count of HttpHeader
     * @param headerKey the Key of Header
     * @return the Value of Header
     */
    public static String findHttpHeader(byte[] buffer, int byteOffset, int byteCount, String headerKey)
    {
        String headerValue = null;
        ByteArrayInputStream byteInputStream = new ByteArrayInputStream(buffer, byteOffset, byteCount);
        BufferedReader reader = new BufferedReader(new InputStreamReader(byteInputStream));
        String line = null;
        // read first line
        try
        {
            line = reader.readLine();
        }
        catch (IOException e)
        {
            MeshLog.e(DEBUG, USE_LOG4J, CLASS, "findHttpHeader() IOException firstly");
        }
        // find the hear value and read next line
        while (line != null)
        {
            String[] kv = line.split(":( )+");
            if (kv.length == 2 && kv[0].equals(headerKey))
            {
                headerValue = kv[1];
                break;
            }
            else
            {
                try
                {
                    line = reader.readLine();
                }
                catch (IOException e)
                {
                    MeshLog.e(DEBUG, USE_LOG4J, CLASS, "findHttpHeader() IOException");
                    break;
                }
            }
        }
        // close resources
        try
        {
            byteInputStream.close();
            reader.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return headerValue;
    }
    
    /**
     * change byte[] to Hex String
     * @param bytes the bytes[] to be changed
     * @return Hex String
     */
    public static String toHexString(byte[] bytes)
    {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; ++i)
        {
            int value = 0xff & bytes[i];
            String hexStr = Integer.toHexString(value);
            sb.append("0x");
            if (hexStr.length() < 2)
            {
                sb.append("0");
            }
            sb.append(hexStr);
            if (i % 8 == 0)
            {
                sb.append("\n");
            }
        }
        return sb.toString();
    }
    
    /**
     * get stack trace message
     * 
     * @param e the Exception
     * @return stack trace message of the Exception
     */
    public static String getStrackTrace(Exception e)
    {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw, true));
        String str = sw.toString();
        return str;
    }
}
