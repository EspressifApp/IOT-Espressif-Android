package com.espressif.iot.util;

public class AccountUtil
{
    private AccountUtil() {
    }

    public static final int TYPE_NONE = 0;
    public static final int TYPE_EMAIL = 1;
    public static final int TYPE_PHONE = 2;
    
    public static int getAccountType(String account) {
        if (isPhoneNumber(account)) {
            return TYPE_PHONE;
        } else if (isEmail(account)) {
            return TYPE_EMAIL;
        }

        return TYPE_NONE;
    }

    public static boolean isEmail(String accountStr) {
        String strs[] = accountStr.split("@");
        if (strs.length != 2)
        {
            return false;
        }
        char firstChar = accountStr.charAt(0);
        if (firstChar == '@' || firstChar == '.' || firstChar == '+')
        {
            return false;
        }
        char lastChar = accountStr.charAt(accountStr.length() - 1);
        if (lastChar == '@' || lastChar == '.')
        {
            return false;
        }
        
        char atFrontChar = strs[0].charAt(strs[0].length() - 1);
        char atBehindChar = strs[1].charAt(0);
        if (atFrontChar == '.' || atFrontChar == '+')
        {
            return false;
        }
        if (atBehindChar == '.')
        {
            return false;
        }
        
        return true;
    }

    private static boolean isPhoneNumber(String accountStr) {
        //APP will support phone number login in the future
        return false;
    }
}
