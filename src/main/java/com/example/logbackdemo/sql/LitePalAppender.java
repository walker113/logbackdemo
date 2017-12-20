package com.example.logbackdemo.sql;

import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;


import org.litepal.crud.DataSupport;
import org.slf4j.MDC;

import java.util.HashMap;
import java.util.Map;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Layout;
import ch.qos.logback.core.UnsynchronizedAppenderBase;
import ch.qos.logback.core.util.Duration;

/**
 * LitePalAppender类,配合Logback
 * @author zzl
 */
public class LitePalAppender extends UnsynchronizedAppenderBase<ILoggingEvent> {
    /**
     * Max tag length enforced by Android
     * http://developer.android.com/reference/android/util/Log.html#isLoggable(java.lang.String, int)
     */
    private static final int MAX_TAG_LENGTH = 23;
    private PatternLayoutEncoder encoder = null;
    private PatternLayoutEncoder tagEncoder = null;
    private boolean checkLoggable = false;

    private Duration maxHistory;
    private long lastCleanupTime = 0;


    /**
     * As in most cases, the default constructor does nothing.
     */
    public LitePalAppender() {}

    /**
     * Get the maximum history in time duration of records to keep
     *
     * @return max history in time duration (e.g., "1 day")
     */
    public String getMaxHistory() {
        return maxHistory != null ? maxHistory.toString() : "";
    }

    /**
     * Gets the maximum history in milliseconds
     * @return the max history in milliseconds
     */
    public long getMaxHistoryMs() {
        return maxHistory != null ? maxHistory.getMilliseconds() : 0;
    }

    /**
     * Set the maximum history in time duration of records to keep
     *
     * @param maxHistory
     *                max history in time duration (e.g., "2 days")
     */
    public void setMaxHistory(String maxHistory) {
        this.maxHistory = Duration.valueOf(maxHistory);
    }


    /*
     * (non-Javadoc)
     * @see ch.qos.logback.core.UnsynchronizedAppenderBase#start()
     */
    @Override
    public void start() {
        if ((this.encoder == null) || (this.encoder.getLayout() == null)) {
            addError("No layout set for the appender named [" + name + "].");
            return;
        }

        // tag encoder is optional but needs a layout
        if (this.tagEncoder != null) {
            final Layout<?> layout = this.tagEncoder.getLayout();

            if (layout == null) {
                addError("No tag layout set for the appender named [" + name + "].");
                return;
            }

            // prevent stack traces from showing up in the tag
            // (which could lead to very confusing error messages)
            if (layout instanceof PatternLayout) {
                String pattern = this.tagEncoder.getPattern();
                if (!pattern.contains("%nopex")) {
                    this.tagEncoder.stop();
                    this.tagEncoder.setPattern(pattern + "%nopex");
                    this.tagEncoder.start();
                }

                PatternLayout tagLayout = (PatternLayout) layout;
                tagLayout.setPostCompileProcessor(null);
            }
        }

        clearExpiredLogs();

        super.start();
    }

    private boolean isMainThread() {
        return Looper.getMainLooper().getThread() == Thread.currentThread();
    }
    /**
     * Removes expired logs from the database
     */
    private void clearExpiredLogs() {
        // 避免在主线程清除数据
        if (isMainThread()) return;

        if (lastCheckExpired(this.maxHistory, this.lastCleanupTime)) {
            this.lastCleanupTime = System.currentTimeMillis();
            final long expiryMs = System.currentTimeMillis() - this.maxHistory.getMilliseconds();
            DataSupport.deleteAll(TraceLog.class, "time <= ? ", String.valueOf(expiryMs));
        }
    }

    /**
     * Determines whether it's time to clear expired logs
     * @param expiry max time duration between checks
     * @param lastCleanupTime timestamp (ms) of last cleanup
     * @return true if last check has expired
     */
    private boolean lastCheckExpired(Duration expiry, long lastCleanupTime) {
        boolean isExpired = false;
        if (expiry != null && expiry.getMilliseconds() > 0) {
            final long now = System.currentTimeMillis();
            final long timeDiff = now - lastCleanupTime;
            isExpired = (lastCleanupTime <= 0) || (timeDiff >= expiry.getMilliseconds());
        }
        return isExpired;
    }


    /*
     * (non-Javadoc)
     * @see java.lang.Object#finalize()
     */
    @Override
    protected void finalize() throws Throwable {

    }

    /*
     * (non-Javadoc)
     * @see ch.qos.logback.core.UnsynchronizedAppenderBase#stop()
     */
    @Override
    public void stop() {
        this.lastCleanupTime = 0;
    }

    /**
     * 是否为异常数据
     * @return
     */
    private boolean isException () {
        String exception = MDC.get(LogTool.MDC_TYPE);
        return !TextUtils.isEmpty(exception) && exception.equals(LogTool.TYPE_EXCEPTION);
    }
    private boolean isTable (String tableName) {
        String table = MDC.get(LogTool.MDC_TABLE);
        return !TextUtils.isEmpty(table) && table.equals(tableName);
    }

    /*
     * (non-Javadoc)
     * @see ch.qos.logback.core.UnsynchronizedAppenderBase#append(java.lang.Object)
     */
    @Override
    public void append(ILoggingEvent event) {
        if (!isStarted()) {
            return;
        }
        clearExpiredLogs();

        String tag = getTag(event);
        // 保存到数据表 Level > DEBUG
        if (event.getLevel().levelInt > Level.TRACE_INT) {
            if (isTable(TraceLog.class.getSimpleName())) {
                TraceLog.parse(event, isException());
            }
        }
        LogTool.removeMDCs();

        // 控制台打印
        switch (event.getLevel().levelInt) {
            case Level.ALL_INT:
            case Level.TRACE_INT:
                if (!checkLoggable || Log.isLoggable(tag, Log.VERBOSE)) {
                    Log.v(tag, this.encoder.getLayout().doLayout(event));
                }
                break;

            case Level.DEBUG_INT:
                if (!checkLoggable || Log.isLoggable(tag, Log.DEBUG)) {
                    Log.d(tag, this.encoder.getLayout().doLayout(event));
                }
                break;

            case Level.INFO_INT:
                if (!checkLoggable || Log.isLoggable(tag, Log.INFO)) {
                    Log.i(tag, this.encoder.getLayout().doLayout(event));
                }
                break;

            case Level.WARN_INT:
                if (!checkLoggable || Log.isLoggable(tag, Log.WARN)) {
                    Log.w(tag, this.encoder.getLayout().doLayout(event));
                }
                break;

            case Level.ERROR_INT:
                if (!checkLoggable || Log.isLoggable(tag, Log.ERROR)) {
                    Log.e(tag, this.encoder.getLayout().doLayout(event));
                }
                break;

            case Level.OFF_INT:
            default:
                break;
        }
    }


    /**
     * Gets the first 254 characters of an object's string representation. This is
     * used to truncate a logging event's argument binding if necessary.
     *
     * @param o the object
     * @return up to 254 characters of the object's string representation; or empty
     * string if the object string is itself null
     */
    private String asStringTruncatedTo254(Object o) {
        String s = null;
        if (o != null) {
            s = o.toString();
        }
        if (s != null && s.length() > 254) {
            s = s.substring(0, 254);
        }
        return s == null ? "" : s;
    }

    private static final short PROPERTIES_EXIST = 0x01;
    private static final short EXCEPTION_EXISTS = 0x02;

    /**
     * Computes the reference mask for a logging event, including
     * flags to indicate whether MDC properties or exception info
     * is available for the event.
     *
     * @param event the logging event to evaluate
     * @return the 16-bit reference mask
     */
    private static short computeReferenceMask(ILoggingEvent event) {
        short mask = 0;

        int mdcPropSize = 0;
        if (event.getMDCPropertyMap() != null) {
            mdcPropSize = event.getMDCPropertyMap().keySet().size();
        }
        int contextPropSize = 0;
        if (event.getLoggerContextVO().getPropertyMap() != null) {
            contextPropSize = event.getLoggerContextVO().getPropertyMap().size();
        }

        if (mdcPropSize > 0 || contextPropSize > 0) {
            mask = PROPERTIES_EXIST;
        }
        if (event.getThrowableProxy() != null) {
            mask |= EXCEPTION_EXISTS;
        }
        return mask;
    }

    /**
     * Merges a log event's properties with the properties of the logger context.
     * The context properties are first in the map, and then the event's properties
     * are appended.
     *
     * @param event the logging event to evaluate
     * @return the merged properties map
     */
    private Map<String, String> mergePropertyMaps(ILoggingEvent event) {
        Map<String, String> mergedMap = new HashMap<String, String>();
        // we add the context properties first, then the event properties, since
        // we consider that event-specific properties should have priority over
        // context-wide properties.
        Map<String, String> loggerContextMap = event.getLoggerContextVO().getPropertyMap();
        if (loggerContextMap != null) {
            mergedMap.putAll(loggerContextMap);
        }

        Map<String, String> mdcMap = event.getMDCPropertyMap();
        if (mdcMap != null) {
            mergedMap.putAll(mdcMap);
        }

        return mergedMap;
    }


    /**
     * Gets the pattern-layout encoder for this appender's <i>logcat</i> message
     *
     * @return the pattern-layout encoder
     */
    public PatternLayoutEncoder getEncoder() {
        return this.encoder;
    }
    /**
     * Sets the pattern-layout encoder for this appender's <i>logcat</i> message
     *
     * @param encoder the pattern-layout encoder
     */
    public void setEncoder(PatternLayoutEncoder encoder) {
        this.encoder = encoder;
    }

    /**
     * Sets whether to ask Android before logging a message with a specific
     * tag and priority (i.e., calls {@code android.util.Log.html#isLoggable}).
     * <p>
     * See http://developer.android.com/reference/android/util/Log.html#isLoggable(java.lang.String, int)
     *
     * @param enable
     *       {@code true} to enable; {@code false} to disable
     */
    public void setCheckLoggable(boolean enable) {
        this.checkLoggable = enable;
    }

    /**
     * Gets the enable status of the <code>isLoggable()</code>-check
     * that is called before logging
     * <p>
     * See http://developer.android.com/reference/android/util/Log.html#isLoggable(java.lang.String, int)
     *
     * @return {@code true} if enabled; {@code false} otherwise
     */
    public boolean getCheckLoggable() {
        return this.checkLoggable;
    }

    /**
     * Gets the logcat tag string of a logging event
     * @param event logging event to evaluate
     * @return the tag string, truncated if max length exceeded
     */
    protected String getTag(ILoggingEvent event) {
        // format tag based on encoder layout; truncate if max length
        // exceeded (only necessary for isLoggable(), which throws
        // IllegalArgumentException)
        String tag = (this.tagEncoder != null) ? this.tagEncoder.getLayout().doLayout(event) : event.getLoggerName();
        if (checkLoggable && (tag.length() > MAX_TAG_LENGTH)) {
            tag = tag.substring(0, MAX_TAG_LENGTH - 1) + "*";
        }
        return tag;
    }


}
