package com.espressif.iot.util;

import java.util.Random;

public class RandomUtil
{
    
    /**
     * map int to String
     * 
     * @param i int value
     * @return 0-9: "0"-"9" 10-25: "a"-"z"
     */
    private static String map(int i)
    {
        if (i < 10)
            return Integer.toString(i);
        else
        {
            char c = (char)('a' + i - 10);
            return Character.toString(c);
        }
    }
    
    /**
     * random 40 places token, the value range is "0-9" and "a-z"
     * 
     * @return random token like "a23b5678e012345z7890123e567890r234r6789x"
     */
    public static String random40()
    {
        return randomString(40);
    }
    
    /**
     * Generate a target length String, the value range is "0-9" and "a-z"
     * 
     * @param length
     * @return
     */
    public static String randomString(int length)
    {
        Random random = new Random();
        String token = "";
        for (int i = 0; i < length; i++)
        {
            int x = random.nextInt(36);
            token += map(x);
        }
        return token;
    }
}
