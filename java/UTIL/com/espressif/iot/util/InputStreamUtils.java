package com.espressif.iot.util;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;

import java.io.ByteArrayOutputStream;

import java.io.IOException;

import java.io.InputStream;

public class InputStreamUtils
{
    
    final static int BUFFER_SIZE = 4096;
    
    final static String DEFAULT_CHARSET = "ISO-8859-1";
    
    /**
     * Transform InputStream to String
     * 
     * @param in the InputStream
     * @return String
     * 
     */
    public static String InputStream2String(InputStream in)
    {
        
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        
        byte[] data = new byte[BUFFER_SIZE];
        
        int count = -1;
        
        try
        {
            while ((count = in.read(data, 0, BUFFER_SIZE)) != -1)
                
                outStream.write(data, 0, count);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return null;
        }
        
        data = null;
        
        try
        {
            return new String(outStream.toByteArray(), DEFAULT_CHARSET);
        }
        catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
            return null;
        }
        
    }
    
    /**
     * Transform InputStream to String by some encoding charsetName
     * 
     * @param in the InputStream
     * @param encoding the encoding charsetName
     * @return String
     * 
     */
    public static String InputStream2String(InputStream in, String encoding)
    {
        
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        
        byte[] data = new byte[BUFFER_SIZE];
        
        int count = -1;
        
        try
        {
            while ((count = in.read(data, 0, BUFFER_SIZE)) != -1)
                
                outStream.write(data, 0, count);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return null;
        }
        
        data = null;
        
        try
        {
            return new String(outStream.toByteArray(), encoding);
        }
        catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
            return null;
        }
        
    }
    
    /**
     * Transform String to InputStream
     * 
     * @param in the String
     * @return InputStream
     * 
     */
    public static InputStream String2InputStream(String in)
        throws Exception
    {
        
        ByteArrayInputStream is = new ByteArrayInputStream(in.getBytes(DEFAULT_CHARSET));
        
        return is;
        
    }
    
    /**
     * Transform InputStream to byte[]
     * 
     * @param in the InputStream
     * @return byte[]
     * @throws IOException
     */
    public static byte[] InputStream2Byte(InputStream in)
        throws IOException
    {
        
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        
        byte[] data = new byte[BUFFER_SIZE];
        
        int count = -1;
        
        while ((count = in.read(data, 0, BUFFER_SIZE)) != -1)
            
            outStream.write(data, 0, count);
        
        data = null;
        
        return outStream.toByteArray();
        
    }
    
    /**
     * Transform byte[] to InputStream
     * 
     * @param in the byte[]
     * @return InputStream
     * 
     */
    public static InputStream byte2InputStream(byte[] in)
    {
        
        ByteArrayInputStream is = new ByteArrayInputStream(in);
        
        return is;
        
    }
    
    /**
     * Transform byte[] to String
     * 
     * @param in the byte[]
     * @return String
     * 
     */
    public static String byte2String(byte[] in)
    {
        
        InputStream is = byte2InputStream(in);
        
        return InputStream2String(is);
        
    }
    
}