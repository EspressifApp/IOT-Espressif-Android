package com.espressif.iot.util;

import java.util.ArrayList;
import java.util.List;

public class RouterUtil
{
    /**
     * Get the broadcast router of the specified router
     * 
     * @param router the specified router
     * @return the broadcast router of the router
     */
    public static String getBroadcastRouter(String router)
    {
        int level = RouterUtil.getRouterLevel(router);
        return router.substring(0, 2 * level) + router.substring(2*level).replaceAll("[f|F]", "0");
    }
    
    /**
     * Get the level of the router
     * 
     * @param router the router
     * @return the level of the router
     */
    public static int getRouterLevel(String router)
    {
        int count = 0;
        for (int index = router.length() - 1; index > 0; index = index - 2)
        {
            if (router.charAt(index) == 'f' && router.charAt(index - 1) == 'f' || router.charAt(index) == 'F'
                && router.charAt(index - 1) == 'F')
            {
                count++;
            }
        }
        return router.length() / 2 - count;
    }
    
    /**
     * Get the value of the specified level of the router
     * 
     * @param router the router
     * @param level the level
     * @return the value of the specified level of the router
     */
    private static int getValue(String router, int level)
    {
        int totalLevel = getRouterLevel(router);
        int beginIndex = (totalLevel - level) * 2;
        int endIndex = (totalLevel - level + 1) * 2;
        String valueStr = router.substring(beginIndex, endIndex);
        return Integer.parseInt(valueStr, 16);
    }
    
    /**
     * Check whether the childRouter is the child of the router
     * 
     * @param router the router
     * @param childRouter the child router
     * @return whether the childRouter is the child of the router
     */
    private static boolean isChild(String router, String childRouter)
    {
        int routerLevel = getRouterLevel(router);
        int childRouterLevel = getRouterLevel(childRouter);
        if (routerLevel < childRouterLevel)
        {
            for (int level = 1; level <= routerLevel; level++)
            {
                if (getValue(router, level) != getValue(childRouter, level))
                {
                    return false;
                }
            }
            return true;
        }
        else
        {
            return false;
        }
    }
    
    /**
     * Check whether the directChildRouter is the direct child of the router
     * 
     * @param router the router
     * @param directChildRouter the direct child router
     * @return whether the directChildRouter is the direct child of the router
     */
    private static boolean isDirectChild(String router, String directChildRouter)
    {
        int routerLevel = getRouterLevel(router);
        int childRouterLevel = getRouterLevel(directChildRouter);
        if (childRouterLevel - routerLevel != 1)
        {
            return false;
        }
        return isChild(router, directChildRouter);
    }
    
    /**
     * Get the router's parent
     * 
     * @param allRouters all of the routers
     * @param router the specified router
     * @return null or the router's parent
     */
    public static String getParentRouter(List<String> allRouters, String router)
    {
        int level = getRouterLevel(router);
        if (level <= 0)
        {
            return null;
        }
        else
        {
            String parentRouter = router.substring(2, router.length()) + "FF";
            for (String routerInList : allRouters)
            {
                if (routerInList.equalsIgnoreCase(parentRouter))
                {
                    return routerInList;
                }
            }
            return null;
        }
    }
    
    /**
     * Get the direct child router list of the specified router
     * 
     * @param allRouters all of the routers
     * @param router the specified router
     * @return the direct child router list of the specified router
     */
    public static List<String> getDirectChildRouterList(List<String> allRouters, String router)
    {
        List<String> result = new ArrayList<String>();
        for (String routerInList : allRouters)
        {
            if (isDirectChild(router, routerInList))
            {
                result.add(routerInList);
            }
        }
        return result;
    }
    
    /**
     * Get the all child router list of the specified router
     * 
     * @param allRouters all of the routers
     * @param router the specified router
     * @return the all child router list of the specified router
     */
    public static List<String> getAllChildRouterList(List<String> allRouters, String router)
    {
        List<String> result = new ArrayList<String>();
        for (String routerInList : allRouters)
        {
            if (isChild(router, routerInList))
            {
                result.add(routerInList);
            }
        }
        return result;
    }
    
    private static void testGetParentRouter()
    {
        String router1 = "030201FF";
        String router2 = "012FFFFF";
        String router1Parent = "0201FFFF";
        List<String> allRouters = new ArrayList<String>();
        allRouters.add(router1);
        allRouters.add(router2);
        allRouters.add(router1Parent);
        if (getParentRouter(allRouters, router1).equals(router1Parent) && getParentRouter(allRouters, router2) == null)
        {
            System.out.println("getParentRouter() pass");
        }
        else
        {
            System.out.println("getParentRouter() fail");
        }
    }
    
    private static void testGetDirectChildRouterList()
    {
        String router = "01FFFFFF";
        List<String> allRouters = new ArrayList<String>();
        allRouters.add("01FFFFFF");
        allRouters.add("0102FFFF");
        allRouters.add("030201FFFF");
        allRouters.add("0201FFFF");
        List<String> childRouters = new ArrayList<String>();
        childRouters.add("0201FFFF");
        List<String> result = getDirectChildRouterList(allRouters, router);
        boolean isPass = true;
        for (String childRouter : childRouters)
        {
            if (!result.contains(childRouter))
            {
                isPass = false;
                break;
            }
        }
        for (String _result : result)
        {
            if (!childRouters.contains(_result))
            {
                isPass = false;
                break;
            }
        }
        if (isPass)
        {
            System.out.println("getDirectChildRouterList() pass");
        }
        else
        {
            System.out.println("getDirectChildRouterList() fail");
        }
    }
    
    private static void testGetAllChildRouterList()
    {
        String router = "01FFFFFF";
        List<String> allRouters = new ArrayList<String>();
        allRouters.add("01FFFFFF");
        allRouters.add("0102FFFF");
        allRouters.add("030201FFFF");
        allRouters.add("0201FFFF");
        List<String> childRouters = new ArrayList<String>();
        childRouters.add("0201FFFF");
        childRouters.add("030201FFFF");
        List<String> result = getAllChildRouterList(allRouters, router);
        boolean isPass = true;
        for (String childRouter : childRouters)
        {
            if (!result.contains(childRouter))
            {
                isPass = false;
                break;
            }
        }
        for (String _result : result)
        {
            if (!childRouters.contains(_result))
            {
                isPass = false;
                break;
            }
        }
        if (isPass)
        {
            System.out.println("getAllChildRouterList() pass");
        }
        else
        {
            System.out.println("getAllChildRouterList() fail");
        }
    }
    
    private static void testGetBroadcastRouter()
    {
        String router = "01FFFFFF";
        if (getBroadcastRouter(router).equals("01000000"))
        {
            System.out.println("testGetBroadcastRouter() pass");
        }
        else
        {
            System.out.println("testGetBroadcastRouter() fail");
        }
    }
    
    public static void main(String args[])
    {
        testGetBroadcastRouter();
        testGetAllChildRouterList();
        testGetDirectChildRouterList();
        testGetParentRouter();
    }
}
