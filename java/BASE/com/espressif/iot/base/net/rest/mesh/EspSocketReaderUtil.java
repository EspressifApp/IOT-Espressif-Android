package com.espressif.iot.base.net.rest.mesh;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.log4j.Logger;

public class EspSocketReaderUtil
{
    // one of them must exist at the header beginning
    private static final String[] VALID_HEAD_START_ARRAY = new String[] {"POST ", "GET ", "HTTP"};
    
    private static final String VALID_CONTENT_LENGTH = "Content-Length: ";
    
    private static final Logger log = Logger.getLogger(EspSocketReaderUtil.class);
    
    private static final char LINE_FEED = '\n';
    
    private static final char CARRIAGE_RETURN = '\r';
    
    private static final char[] ESCAPE = new char[] {CARRIAGE_RETURN, LINE_FEED};
    
    public static void main(String args[])
    {
        int result = getBodyLength(VALID_CONTENT_LENGTH + " 0\r\n");
        System.out.println(result);
        System.out.println("OK");
    }
    
    /**
     * Get the length of the content by "Content-Length: %d" in the header
     * 
     * @param header the header String
     * @return the length of the body by "Content-Length: %d" in the header or -1 if the header is invalid
     */
    private static int getBodyLength(String header)
    {
        int result = -1;
        int position = header.indexOf(VALID_CONTENT_LENGTH);
        int beginIndex = position + VALID_CONTENT_LENGTH.length();
        if (beginIndex == -1)
        {
            return result;
        }
        int endIndex = header.indexOf(CARRIAGE_RETURN, position);
        if (endIndex == -1)
        {
            return result;
        }
        try
        {
            result = Integer.parseInt(header.substring(beginIndex, endIndex));
        }
        catch (NumberFormatException invalidFormatException)
        {
        	invalidFormatException.printStackTrace();
        }
        return result;
    }
    
    /**
     * Read Header by BufferedReader and check the contentLength
     * 
     * @param reader the BufferedReader of the socket's InputStream
     * @return the header String or null if the header String is invalid
     * @throws IOException
     */
    private static String __readHeader(BufferedReader reader)
    {
        StringBuffer resultSb = new StringBuffer("");
        String oneLineStr = null;
        // whether the end with valid format, '\r\n'
        boolean isValidEnd = false;
        // whether the head start with valid format, ignore the invalid String
        boolean isValidStart = false;
        
        // read content by LF('\n')
        try
        {
            while ((oneLineStr = reader.readLine()) != null)
            {
                	
				log.debug("__readHeader(): oneLineStr:" + oneLineStr);
				log.debug("__readHeader(): oneLineStr.length():" + oneLineStr.length());
                
                if (!isValidStart)
                {
                    // check whether the head start is valid
                    for (String validHeaderStart : VALID_HEAD_START_ARRAY)
                    {
                        if (oneLineStr.startsWith(validHeaderStart))
                        {
                            isValidStart = true;
                            log.debug("__readHeader(): isValidStart = true");
                            break;
                        }
                    }
                    if (!isValidStart)
                    {
                        // ignore rubbish String before header
                        log.debug("__readHeader(): the rubbish data:\"" + "\" is ignored");
                        continue;
                    }
                }
                
                // when encounter \r\n, the oneLineStr.length() will equal 0
                if (oneLineStr.length() == 0)
                {
                    isValidEnd = true;
                    log.debug("__readHeader(): isValidEnd = true");
                    break;
                }
                
                log.debug("__readHeader(): append oneLineStr and ESCAPE");
                
                resultSb.append(oneLineStr);
                resultSb.append(ESCAPE);
            }
        }
        catch (IOException e)
        {
        	e.printStackTrace();
            return null;
        }
        
        // add ESCAPE in the end of header
        resultSb.append(ESCAPE);
        
        String resultStr = resultSb.toString();
        
        // Content-Length must be including
        boolean isValidContent = false;
        
        // Although when isValidEnd equals true, isValidStart must equal true,
        // check isValidStart here just to make code more readable.
        if (isValidStart && isValidEnd)
        {
        	log.debug("__readHeader(): The header String start and end is valid");
            isValidContent = getBodyLength(resultStr) != -1;
        }
        
        if (isValidContent)
        {
            log.debug("__readHeader(): The header String is:\n" + resultStr);
            return resultStr;
        }
        
        else
        {
			log.warn("__readHeader():The header String is invalid, return null, start");
			for (int i = 0; i < resultStr.length(); i++)
			{
				log.warn("__readHeader():The header String is " + i + ":"
						+ (0 + resultStr.charAt(i)));
			}
			log.warn("__readHeader():The invalid header String is " + resultStr);
			log.warn("__readHeader():The header String is invalid, return null, end");
			return null;
        }
    }
    
    /**
     * Read Body by BufferedReader and check the contentLength
     * 
     * @param reader the BufferedReader of the socket's InputStream
     * @return the header String or null if the header String is invalid
     * @throws IOException
     */
    private static String __readBody(BufferedReader reader, int contentLength)
    {
        StringBuffer result = new StringBuffer("");
        
        char[] chars = new char[contentLength];
        int totalLen = 0;
        int validLen;
        log.debug("__readBody(): contentLength = " + contentLength);
        try
        {
            log.debug("__readBody(): totalLen = " + totalLen);
            while (totalLen < contentLength)
            {
                validLen = reader.read(chars);
                if (validLen < 0)
                {
                    log.warn("__readBody(): validLen = " + validLen);
                    return null;
                }
                result.append(chars, 0, validLen);
                totalLen += validLen;
            }
            log.debug("__readBody(): result = " + result);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return null;
        }
        if (totalLen != contentLength)
        {
        	log.debug("__readBody(): contentLength is invalid");
            return null;
        }
        if (result.charAt(result.length() - 1) != LINE_FEED)
        {
            log.debug("__readBody(): the ending char is invalid: " + (int)(result.charAt(result.length() - 1)));
            return null;
        }
        return result.toString();
    }
    
    /**
     * Read header entity by socket's InputStream
     * 
     * @param reader the BufferedReader of the socket's InputStream
     * @return the header entity of EspSocketResponseEntity or null if the header String is invalid
     */
    public static EspSocketResponseBaseEntity readHeaderEntity(BufferedReader reader)
    {
        String header = __readHeader(reader);
        if (header != null)
        {
            return new EspSocketResponseBaseEntity(header);
        }
        else
        {
            return null;
        }
    }
    
    /**
     * Read header and body entity by socket's InputStream
     * 
     * @param reader the BufferedReader of the socket's InputStream
     * @return the header body entity of EspSocketResponseEntity or null if the header String is invalid
     */
    public static EspSocketResponseBaseEntity readHeaderBodyEntity(BufferedReader reader)
    {
        String header = __readHeader(reader);
        String body = null;
        int contentLength = -1;
        // get body
        if (header != null)
        {
            contentLength = getBodyLength(header);
            log.debug("contentLength: " + contentLength);
            if (contentLength > 0)
            {
                body = __readBody(reader, contentLength);
            }
        }
        
        
        // return result according to head and body
        if (header != null && contentLength == 0)
        {
            return new EspSocketResponseBaseEntity(header);
        }
        else if (header != null && body != null)
        {
            return new EspSocketResponseBaseEntity(header, body);
        }
        else
        {
            return null;
        }
    }
    
    /**
     * Read header by socket's InputStream
     * 
     * @param in the socket's InputStream
     * @return the header String or null if the header String is invalid
     */
    public static String readHeader(InputStream in)
    {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        return __readHeader(reader);
    }
    
    /**
     * Read header and body by socket's InputStream
     * @param in the socket's InputStream
     * @return the header body("\r\n\r\n" is delimiter) String or null if the header String is invalid
     */
    public static String readHeaderBody(InputStream in)
    {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        String header = __readHeader(reader);
        String body = null;
        if (header != null)
        {
            int contentLength = getBodyLength(header);
            body = __readBody(reader, contentLength);
        }
        if (header == null || body == null)
        {
            return null;
        }
        else
        {
            return header + body;
        }
    }
    
}
