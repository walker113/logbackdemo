package com.example.logbackdemo.sql;



import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.slf4j.Marker;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.MessageDigest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.core.util.Duration;

/**
 * LogBack日志封装类
 * @author zzl
 */
public class LogTool {
    static final String MDC_TABLE = "table";
    static final String MDC_TYPE = "type";

    public static final String TYPE_EXCEPTION = "exception";
    private static Logger sLog;
    private static final String FQCN = LogTool.class.getName();

    private static Method s_innerMethod;

    /**
     * 默认保存到TraceLog
     */
    private static final String DEFAULT = TraceLog.class.getSimpleName();

    static {
        sLog = (Logger) LoggerFactory.getLogger("Log");
        try {
            s_innerMethod = sLog.getClass().getDeclaredMethod("filterAndLog_0_Or3Plus", String.class, Marker.class, Level.class, String.class, Object[].class, Throwable.class);
            s_innerMethod.setAccessible(true);
        } catch (NoSuchMethodException | SecurityException e) {
            sLog.error("load failed, Exception:" + e);
        }
    }

    /*** TRACE ***/
    public static void t(String msg) {
        t(DEFAULT, msg);
    }
    public static void t(String table, String msg) {
        invokeLogMethod(table, Level.TRACE, msg);
    }

    /*** DEBUG ***/
    public static void d(String msg) {
        d(DEFAULT, msg);
    }
    public static void d(String table, String msg) {
        invokeLogMethod(table, Level.DEBUG, msg);
    }

    /*** INFO ***/
    public static void i(String msg) {
        i(DEFAULT, msg);
    }
    public static void i(String table, String msg) {
        invokeLogMethod(table, Level.INFO, msg);
    }

    /*** WARN ***/
    public static void w(String msg) {
        w(DEFAULT, msg);
    }
    public static void w(String table, String msg) {
        invokeLogMethod(table, Level.WARN, msg);
    }
    public static void w(Exception e) {
        MDC.put(MDC_TYPE, TYPE_EXCEPTION);
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        w(DEFAULT, sw.toString());
    }

    /*** ERROR ***/
    public static void e(String msg) {
        e(DEFAULT, msg);
    }
    public static void e(String table, String msg) {
        invokeLogMethod(table, Level.ERROR, msg);
    }
    public static void e(Exception e) {
        MDC.put(MDC_TYPE, TYPE_EXCEPTION);
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        e(DEFAULT, sw.toString());
    }

//    public static void e(String table, Throwable throwable) {
//        String exceptionStackTrace = getExceptionStackTrace(throwable);
//        if (TextUtils.isEmpty(exceptionStackTrace)) return;
//        invokeLogMethod(table, Level.ERROR, exceptionStackTrace);
//    }


    private static void invokeLogMethod(String localFQCN, Marker marker, Level level, String msg, Object[] params, Throwable t) {
//        if (Config.switch_log.equals(Config.OFF)) return;

        try {
            s_innerMethod.invoke(sLog, localFQCN, marker, level, msg, params, t);
        } catch (SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            sLog.error("execute s_innerMethod failed, Exception:" + e);
        }
    }

    private static void invokeLogMethod(String table, Level level, String msg) {
//        if (Config.switch_log.equals(Config.OFF)
//                || TextUtils.isEmpty(msg)) return;

        MDC.put(MDC_TABLE, table);

        try {
            s_innerMethod.invoke(sLog, FQCN, null, level, msg, null, null);
        } catch (SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            sLog.error("execute s_innerMethod failed, Exception:" + e);
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


    public static String sha1Hash (String toHash) {
        String hash = null;
        try {
            MessageDigest digest = MessageDigest.getInstance( "SHA-1" );
            byte[] bytes = toHash.getBytes("UTF-8");
            digest.update(bytes, 0, bytes.length);
            bytes = digest.digest();

            // This is ~55x faster than looping and String.formating()
            hash = bytesToHex( bytes );
        }
        catch( Throwable e ) {
            e.printStackTrace();
        }
        return hash;
    }
    // http://stackoverflow.com/questions/9655181/convert-from-byte-array-to-hex-string-in-java
    final private static char[] hexArray = "0123456789ABCDEF".toCharArray();
    private static String bytesToHex( byte[] bytes ) {
        char[] hexChars = new char[ bytes.length * 2 ];
        for( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[ j ] & 0xFF;
            hexChars[ j * 2 ] = hexArray[ v >>> 4 ];
            hexChars[ j * 2 + 1 ] = hexArray[ v & 0x0F ];
        }
        return new String( hexChars ).toLowerCase();
    }

    public static void removeMDCs () {
        MDC.remove(MDC_TABLE);
        MDC.remove(MDC_TYPE);
    }

    /**
     * 设置日志级别
     * @param level
     */
    public static void setLevel(Level level) {
        sLog.setLevel(level);
    }

    /**
     * 20 milli/millisecond
     * 20 second
     * 1 hour
     * 1 day
     * 设置日志保留时间
     * @param masHistory
     */
    public static void setLitePalAppenderCleanTime (String masHistory) {
        try {
            checkFormat(masHistory);
        } catch (Exception e) {
            e(e);
            return;
        }

        LitePalAppender litePalAppender = (LitePalAppender) sLog.getAppender("LITE_PAL_APPENDER");
        litePalAppender.setMaxHistory(masHistory);
    }


    private final static int UNIT_GROUP = 3;
    private final static String DOUBLE_PART = "([0-9]*(.[0-9]+)?)";
    private final static String UNIT_PART = "(|milli(second)?|second(e)?|minute|hour|day)s?";
    private static final Pattern DURATION_PATTERN = Pattern.compile(DOUBLE_PART + "\\s*" + UNIT_PART, Pattern.CASE_INSENSITIVE);
    private static void checkFormat(String durationStr) {
        Matcher matcher = DURATION_PATTERN.matcher(durationStr);

        if (! matcher.matches()) {
            throw new IllegalArgumentException("String value [" + durationStr + "] is not in the expected format.");
        }

        String unitStr = matcher.group(UNIT_GROUP);
        if (!(unitStr.equalsIgnoreCase("milli")
                || unitStr.equalsIgnoreCase("millisecond")
                || unitStr.length() == 0
                || unitStr.equalsIgnoreCase("second")
                || unitStr.equalsIgnoreCase("seconde")
                || unitStr.equalsIgnoreCase("minute")
                || unitStr.equalsIgnoreCase("hour")
                || unitStr.equalsIgnoreCase("day")))
            throw new IllegalStateException("Unexpected " + unitStr);
    }
}
