package com.espressif.iot.util;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.os.Environment;

public class FileUtil
{
    
    public static String getDownloadPath()
    {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
            return Environment.getExternalStorageDirectory().toString() + "/Download/";
        else
            return null;
    }
    
    /**
     * read bytes from specific file path
     * 
     * @param context the Context of the Application
     * @param filePath the file path including path and filename
     * @return the bytes read from specific file path
     */
    public static byte[] readBytes(String filePath)
    {
        return __readBytesArray(filePath);
    }
    
    /**
     * write bytes to specific file path
     * 
     * @param bytes the bytes to be written
     */
    
    /**
     * 
     * @param bytes the bytes to be written
     * @param filePath the file path to be written
     */
    public static void writeBytes(byte[] bytes, String filePath)
    {
        __writeBytesArray(bytes, filePath);
    }
    
    private static byte[] __readBytesArray(String filePath)
    {
        byte[] data = null;
        InputStream input = null;
        DataInputStream dis = null;
        try
        {
            input = new FileInputStream(filePath);
            dis = new DataInputStream(input);
            data = new byte[dis.available()];
            dis.read(data);
            input.close();
            dis.close();
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
            return null;
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return null;
        }
        finally
        {
            if (input != null)
            {
                try
                {
                    input.close();
                }
                catch (IOException ignore)
                {
                }
            }
            if (dis != null)
            {
                try
                {
                    dis.close();
                }
                catch (IOException ignore)
                {
                }
            }
        }
        return data;
    }
    
    private static boolean __writeBytesArray(byte[] bytes, String filePath)
    {
        OutputStream output = null;
        DataOutputStream dio = null;
        try
        {
            output = new FileOutputStream(filePath);
            dio = new DataOutputStream(output);
            dio.write(bytes);
            dio.flush();
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
            return false;
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return false;
        }
        finally
        {
            if (dio != null)
            {
                try
                {
                    dio.close();
                }
                catch (IOException ignore)
                {
                }
            }
            if (output != null)
            {
                try
                {
                    output.close();
                }
                catch (IOException ignore)
                {
                }
            }
        }
        return true;
    }
}
