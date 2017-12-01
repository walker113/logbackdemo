package com.example.logbackdemo.apag;

import android.text.TextUtils;

import com.jingang.ad_fabuyun.constant.Config;

import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.slf4j.Marker;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

public class LogC {
    public static final String TAB = "table";
    private static Logger innerLogger;

    private static final String FQCN = LogC.class.getName();

    private static Method innerMethod;

    public static int load(Logger logger) {
        innerLogger = logger;
        try {
            innerMethod = innerLogger.getClass().getDeclaredMethod("filterAndLog_0_Or3Plus", String.class, Marker.class, Level.class, String.class, Object[].class, Throwable.class);
            innerMethod.setAccessible(true);
        } catch (NoSuchMethodException | SecurityException e) {
            innerLogger.error("load failed, Exception:" + e);
            return -1;
        }
        return 0;
    }

    static {
        innerLogger = (Logger) LoggerFactory.getLogger("Log");
        try {
            innerMethod = innerLogger.getClass().getDeclaredMethod("filterAndLog_0_Or3Plus", String.class, Marker.class, Level.class, String.class, Object[].class, Throwable.class);
            innerMethod.setAccessible(true);
        } catch (NoSuchMethodException | SecurityException e) {
            innerLogger.error("load failed, Exception:" + e);
        }
    }

    /**
     * Level 级别设置
     *
     * @param level
     */
    public static void setLevel(Level level) {
        innerLogger.setLevel(level);
    }

    public static void d(String table, String msg) {
        innerLogMethod(table, FQCN, null, Level.WARN, msg);
    }


    public static void e(String table, String msg) {
        innerLogMethod(table, FQCN, null, Level.ERROR, msg);
    }

    public static void e_exception(String table, Throwable throwable) {
        String exceptionStackTrace = getExceptionStackTrace(throwable);
        if (TextUtils.isEmpty(exceptionStackTrace)) return;
        innerLogMethod(table, FQCN, null, Level.ERROR, exceptionStackTrace);
    }

    public static void i(String table, String msg) {
        innerLogMethod(table, FQCN, null, Level.INFO, msg);
    }

    public static void w(String table, String msg) {
        MDC.put("table", "BootLog");
        innerLogMethod(table, FQCN, null, Level.WARN, msg);
    }

    private static void innerLogMethod(String localFQCN, Marker marker, Level level, String msg, Object[] params, Throwable t) {
        if (Config.switch_log.equals(Config.OFF)) return;

        try {
            innerMethod.invoke(innerLogger, localFQCN, marker, level, msg, params, t);
        } catch (SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            innerLogger.error("execute innerMethod failed, Exception:" + e);
        }
    }

    private static void innerLogMethod(String table, String localFQCN, Marker marker, Level level, String msg) {
        if (Config.switch_log.equals(Config.OFF)) return;
        MDC.put(TAB, table);

        try {
            innerMethod.invoke(innerLogger, localFQCN, marker, level, msg, null, null);
        } catch (SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            innerLogger.error("execute innerMethod failed, Exception:" + e);
        }
    }

    public static String getExceptionStackTrace(Throwable throwable) {
        StringWriter sw = null;
        PrintWriter printWriter = null;
        try {
            if(throwable != null) {
                sw = new StringWriter();
                printWriter = new PrintWriter(sw);
                throwable.printStackTrace(printWriter);
                printWriter.flush();
                sw.flush();
                return sw.toString();
            }
            else
                return null;
        }finally {
            try {
                if(sw != null)
                    sw.close();
                if(printWriter != null)
                    printWriter.close();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
