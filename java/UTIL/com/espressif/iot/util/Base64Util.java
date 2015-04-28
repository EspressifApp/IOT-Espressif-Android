package com.espressif.iot.util;

import android.util.Base64;

public class Base64Util
{
    private static final int BASE64_FLAG = Base64.NO_WRAP;
    
    /**
     * Decode the Base64-encoded data in input and return the data in
     * a new byte array.
     *
     * <p>The padding '=' characters at the end are not omitted
     *
     * @param input the input array to decode
     * @param offset the position within the input array at which to start
     * @param len    the number of bytes of input to decode
     *
     * @throws IllegalArgumentException if the input contains
     * incorrect padding
     */
    public static byte[] decode(byte[] input,int offset,int len)
    {
        return Base64.decode(input, offset, len, BASE64_FLAG);
    }
    
    /**
     * Base64-encode the given data and return a newly allocated
     * byte[] with the result.
     *
     * @param input  the data to encode
     * @param offset the position within the input array at which to
     *               start
     * @param len    the number of bytes of input to encode
     */
    public static byte[] encode(byte[] input, int offset,int len)
    {
        return Base64.encode(input, offset, len, BASE64_FLAG);
    }
    
    /**
     * Base64-encode the given data and return a newly allocated
     * byte[] with the result.
     *
     * @param input  the data to encode
     */
    public static byte[] encode(byte[] input)
    {
        return Base64.encode(input, BASE64_FLAG);
    }
    
    /**
     * Decode the Base64-encoded data in input and return the data in
     * a new byte array.
     *
     * <p>The padding '=' characters at the end are not omitted
     *
     * @param input the input array to decode
     *
     * @throws IllegalArgumentException if the input contains
     * incorrect padding
     */
    public static byte[] decode(byte[] input)
    {
        return Base64.decode(input, BASE64_FLAG);
    }
    
    public static void main(String args[])
    {
        byte b = 0;
        byte[] originBytes = new byte[256];
        System.out.println("#####origin len: " + originBytes.length);
        for (int i = 0; i < originBytes.length; i++)
        {
            originBytes[i] = b++;
            System.out.print("#####:" + originBytes[i] + ",");
        }
        byte[] encodedBytes = Base64.encode(originBytes, Base64.NO_WRAP);
        System.out.println();
        System.out.println("#####encoded len: " + encodedBytes.length);
        for (int i = 0; i < encodedBytes.length; i++)
        {
            System.out.print("#####:" + encodedBytes[i] + ",");
        }
        byte[] decodedBytes = Base64.decode(encodedBytes, Base64.NO_WRAP);
        System.out.println();
        System.out.println("#####decoded len: " + decodedBytes.length);
        for (int i = 0; i < decodedBytes.length; i++)
        {
            System.out.print("#####:" + decodedBytes[i] + ",");
        }
        System.out.println();
    }
}
