package com.espressif.iot.base.net.rest.mesh;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Date;

import org.apache.log4j.Logger;

/**
 * filename:SocketUtil.java
 * author:����
 * comment:
 */

/**
 * @author martin
 * 
 */
public class EspSocketUtil {

    private static final Logger log = Logger.getLogger(EspSocketUtil.class);
    
	/**
	 * write string 2 a outputstream
	 * 
	 * @param str
	 *            to write string
	 * @param in
	 *            stream
	 * @throws IOException 
	 */
    public static void writeStr2Stream(String str, OutputStream out)
        throws IOException
    {
        try
        {
            log.debug(EspSocketUtil.getNowTime() + ": prepared to write [\n" + str + "\n].");
            // add writer
            DataOutputStream writer = new DataOutputStream(out);
            
            // write
            writer.writeBytes(str);
            
            writer.flush();
        }
        catch (IOException ex)
        {
            log.error("SocketUtil: writeStr2Stream IOException: " + EspSocketUtil.getNowTime() + ex);
            throw ex;
        }
    }

	/**
	 * read string from a inputstream
	 * 
	 * @param in
	 * @return
	 * @throws IOException 
	 */
    public static String readStrFromStream(InputStream in)
        throws IOException
    {
        
        log.debug(getNowTime() + " : start to read string from stream");
        
        StringBuffer result = new StringBuffer("");
        
        // build buffered reader
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        
        // read 1024 bytes per time
        char[] chars = new char[2048];
        int len;
        
        try
        {
            while ((len = reader.read(chars)) != -1)
            {
                // if the length of array is 1M
                if (2048 == len)
                {
                    // then append all chars of the array
                    result.append(chars);
                }
                // if the length of array is less then 1M
                else
                {
                    // then append the valid chars
                    for (int i = 0; i < len; i++)
                    {
                        result.append(chars[i]);
                    }
                    break;
                }
            }
            
        }
        catch (IOException e)
        {
            log.error("SocketUtil: readStrFromStream IOException: " + e);
            throw e;
        }
       	log.debug("end reading string from stream");
        return result.toString();
    }
    
    public static String readLineFromStream(InputStream in)
        throws IOException
    {
        // build buffered reader
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        return reader.readLine();
    }

	public static String getNowTime()
	{
		return new Date().toString();
	}

}
