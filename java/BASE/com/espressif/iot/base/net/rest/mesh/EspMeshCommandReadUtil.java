package com.espressif.iot.base.net.rest.mesh;

import java.io.IOException;
import java.io.InputStream;

import org.apache.log4j.Logger;

public class EspMeshCommandReadUtil
{
    private static final Logger log = Logger.getLogger(EspMeshCommandReadUtil.class);
 
    private static final boolean LOG_VERBOSE = false;
    
    // {"command":
    private static final String COMMAND_HEADER = "{\"command\":";
    
    private static final String COMMAND_HEADER_SECTION1 = "{";
    
    private static final String COMMAND_HEADER_SECTION2 = "\"command\":";
    
    private static final int COMMAND_HEADER_SECTION1_LEN = COMMAND_HEADER_SECTION1.length();
    
    private static final int COMMAND_HEADER_SECTION2_LEN = COMMAND_HEADER_SECTION2.length();
    
    // "xxxxxxxx"}\r\n
    private static final String ESCAPE = "\r\n";
    
    private static final String COMMAND_BODY_START = "\"";
    
    private static final int COMMAND_BODY_START_LEN = COMMAND_BODY_START.length();
    
    private static final String COMMAND_BODY_END = "\"}" + ESCAPE;
    
    private static final int COMMAND_BODY_END_LEN = COMMAND_BODY_END.length();
    
    // TLV is used by mesh command, at the moment the type len in mesh command is 1
    private static final int COMMAND_TYPE_LEN = 1;
    
    // TLV is used by mesh command, at the moment the len of following bytes len in mesh command is 1
    private static final int COMMAND_LEN_LEN = 1;
    
    /**
     * read specific count bytes and transform them into String
     * @param inputStream the inputStream to be read
     * @param count the count of bytes to be read
     * @return the String of the specific count of bytes
     * @throws IOException
     */
    private static String __readBytes(InputStream inputStream, int count)
        throws IOException
    {
        byte[] buffer = new byte[count];
        int bufferIndex = 0;
        while (bufferIndex < count)
        {
            int readCount = inputStream.read(buffer, bufferIndex, count - bufferIndex);
            if (readCount > 0)
            {
                bufferIndex += readCount;
            }
        }
        if(LOG_VERBOSE)
        {
            // print String
            StringBuilder sb = new StringBuilder();
            sb.append(new String(buffer));
            log.debug("__readBytes() Str:" + sb);
            // print hex
            sb = new StringBuilder();
            for (int i = 0; i < count; ++i)
            {
                int value = buffer[i] & 0xff;
                String hexStr = Integer.toHexString(value);
                if (hexStr.length() == 1)
                {
                    hexStr = "0" + hexStr;
                }
                sb.append("0x");
                sb.append(hexStr);
                sb.append("  ");
                if ((count + 1) % 16 == 0)
                {
                    sb.append("\n");
                }
            }
            log.debug("__readBytes() hex:" + sb);
        }
        // change byte[] to char
        StringBuilder result = new StringBuilder();
        for (int index = 0; index < count; ++index)
        {
            char ch = (char)(buffer[index] & 0xff);
            result.append(ch);
        }
        return result.toString();
    }
    
    private static String __readOneByte(InputStream inputStream)
        throws IOException
    {
        return __readBytes(inputStream, 1);
    }
    
    private static String __readCommandHeader(InputStream inputStream) throws IOException
    {
        log.debug("__readCommandHeader()");
        // read until COMMAND_HEADER_SECTION1 occur, discard rubbish message
        String commandHeaderSection1Str = null;
        while (!COMMAND_HEADER_SECTION1.equals(commandHeaderSection1Str))
        {
            commandHeaderSection1Str = __readOneByte(inputStream);
        }
        // check whether the follow String is COMMAND_HEADER_SECTION2
        String commandHeaderSection2Str = null;
        commandHeaderSection2Str = __readBytes(inputStream, COMMAND_HEADER_SECTION2_LEN);
        if (COMMAND_HEADER_SECTION2.equals(commandHeaderSection2Str))
        {
            return COMMAND_HEADER;
        }
        else
        {
            log.warn("__readCommandHeader() command header section2 is invalid, return null");
            return null;
        }
    }
    
    private static String __readCommandBody(InputStream inputStream) throws IOException
    {
        log.debug("__readCommandBody()");
        // check whether the body start is valid
        String commandBodyStart = __readBytes(inputStream, COMMAND_BODY_START_LEN);
        if (!COMMAND_BODY_START.equals(commandBodyStart))
        {
            log.warn("__readCommandBody() command body start is invalid, return null");
            return null;
        }
        // read body
        StringBuilder commandBody = new StringBuilder();
        // read body command type
        String commandType = __readBytes(inputStream, COMMAND_TYPE_LEN);
        commandBody.append(commandType);
        log.debug("__readCommandBody() command type:" + (int)commandType.charAt(0));
        // read body command len
        String commandLen = __readBytes(inputStream, COMMAND_LEN_LEN);
        commandBody.append(commandLen);
        log.debug("__readCommandBody() command len:" + (int)commandLen.charAt(0));
        // extract following command body by commandLen
        int followCommandLen = commandLen.getBytes()[0] & 0xff;
        if (commandBody.length() == 0)
        {
            log.warn("__readCommandBody() command body is empty, return null");
            return null;
        }
        String followCommand = __readBytes(inputStream, followCommandLen);
        commandBody.append(followCommand);
        // check whether the body end is valid
        String commandBodyEnd = __readBytes(inputStream, COMMAND_BODY_END_LEN);
        if(!COMMAND_BODY_END.equals(commandBodyEnd))
        {
            log.warn("__readCommandBody() command body end is invalid, return null");
            return null;
        }
        return commandBodyStart + commandBody + commandBodyEnd;
    }
    
    /**
     * read command response received from mesh device
     * 
     * @param inputStream the socket's inputStream
     * @return the command response
     */
    public static String readCommand(InputStream inputStream)
    {
        log.debug("readCommand()");
        String header = null;
        String body = null;
        try
        {
            while (body == null)
            {
                // read header
                while (header == null)
                {
                    // read command header, ignore rubbish message,
                    header = __readCommandHeader(inputStream);
                    if (header == null)
                    {
                        log.warn("readCommand() read one invalid header");
                    }
                }
                // read body
                body = __readCommandBody(inputStream);
                // the body could be invalid, if the body invalid, give up the response, receive the next one
                if (body == null)
                {
                    log.warn("readCommand() read one invalid body");
                    header = null;
                }
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return null;
        }
        return header + body;
    }
}
