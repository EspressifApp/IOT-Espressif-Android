package com.espressif.iot.base.net.proxy;

import org.apache.log4j.Logger;

import android.util.Log;

public class MeshLog
{
    private MeshLog() {}
    
    public static void initLog4jLevel(org.apache.log4j.Level level) {
        setLog4jLevel(BlockingFinishThread.class, level);
        setLog4jLevel(EspMeshSocketImpl.class, level);
        setLog4jLevel(EspMeshSocketManager.class, level);
        setLog4jLevel(EspProxyServerImpl.class, level);
        setLog4jLevel(EspProxyTaskFactory.class, level);
        setLog4jLevel(EspProxyTaskImpl.class, level);
        setLog4jLevel(EspSocket.class, level);
        setLog4jLevel(EspSocketUtil.class, level);
        setLog4jLevel(MeshCommunicationUtils.class, level);
    }
    
    private static void setLog4jLevel(Class<?> cls, org.apache.log4j.Level level) {
        Logger.getLogger(cls).setLevel(level);
    }
    
    private static final boolean PRINT_LOG = true;
    
    private static enum Level {
        V, D, I, W, E
    }
    
    private static void printAndroid(Level level, String tag, String msg) {
        if (!PRINT_LOG) {
            return;
        }
        
        switch (level) {
            case V:
                Log.v(tag, msg);
                break;
            case D:
                Log.d(tag, msg);
                break;
            case I:
                Log.i(tag, msg);
                break;
            case W:
                Log.w(tag, msg);
                break;
            case E:
                Log.e(tag, msg);
                break;
        }
    }
    
    private static void printLog4j(Level level, Class<?> cls, String msg) {
        if (!PRINT_LOG) {
            return;
        }
        
        Logger logger = Logger.getLogger(cls);
        switch (level) {
            case V:
                logger.debug(msg);
                break;
            case D:
                logger.debug(msg);
                break;
            case I:
                logger.info(msg);
                break;
            case W:
                logger.warn(msg);
                break;
            case E:
                logger.error(msg);
                break;
        }
    }
    
    public static void vAndroid(boolean debug, String tag, String msg) {
        if (debug) {
            printAndroid(Level.V, tag, msg);
        }
    }
    
    public static void vLog4j(boolean debug, Class<?> cls, String msg) {
        if (debug) {
            printLog4j(Level.V, cls, msg);
        }
    }
    
    public static void v(boolean debug, boolean useLog4j, Class<?> cls, String msg) {
        if (useLog4j) {
            vLog4j(debug, cls, msg);
        } else {
            vAndroid(debug, cls.getSimpleName(), msg);
        }
    }
    
    public static void dAndroid(boolean debug, String tag, String msg) {
        if (debug) {
            printAndroid(Level.D, tag, msg);
        }
    }
    
    public static void dLog4j(boolean debug, Class<?> cls, String msg) {
        if (debug) {
            printLog4j(Level.D, cls, msg);
        }
    }
    
    public static void d(boolean debug, boolean useLog4j, Class<?> cls, String msg) {
        if (useLog4j) {
            dLog4j(debug, cls, msg);
        } else {
            dAndroid(debug, cls.getSimpleName(), msg);
        }
    }
    
    public static void iAndroid(boolean debug, String tag, String msg) {
        if (debug) {
            Log.i(tag, msg);
            printAndroid(Level.I, tag, msg);
        }
    }
    
    public static void iLog4j(boolean debug, Class<?> cls, String msg) {
        if (debug) {
            printLog4j(Level.I, cls, msg);
        }
    }
    
    public static void i(boolean debug, boolean useLog4j, Class<?> cls, String msg) {
        if (useLog4j) {
            iLog4j(debug, cls, msg);
        } else {
            iAndroid(debug, cls.getSimpleName(), msg);
        }
    }
    
    public static void wAndroid(boolean debug, String tag, String msg) {
        if (debug) {
            printAndroid(Level.W, tag, msg);
        }
    }
    
    public static void wLog4j(boolean debug, Class<?> cls, String msg) {
        if (debug) {
            printLog4j(Level.W, cls, msg);
        }
    }
    
    public static void w(boolean debug, boolean useLog4j, Class<?> cls, String msg) {
        if (useLog4j) {
            wLog4j(debug, cls, msg);
        } else {
            wAndroid(debug, cls.getSimpleName(), msg);
        }
    }
    
    public static void eAndroid(boolean debug, String tag, String msg) {
        if (debug) {
            printAndroid(Level.E, tag, msg);
        }
    }
    
    public static void eLog4j(boolean debug, Class<?> cls, String msg) {
        if (debug) {
            printLog4j(Level.E, cls, msg);
        }
    }
    
    public static void e(boolean debug, boolean useLog4j, Class<?> cls, String msg) {
        if (useLog4j) {
            eLog4j(debug, cls, msg);
        } else {
            eAndroid(debug, cls.getSimpleName(), msg);
        }
    }
}
