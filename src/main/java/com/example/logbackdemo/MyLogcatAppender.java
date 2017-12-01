package com.example.logbackdemo;

import android.util.Log;

import com.socks.library.KLog;

import java.util.Map;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Layout;
import ch.qos.logback.core.UnsynchronizedAppenderBase;

public class MyLogcatAppender extends UnsynchronizedAppenderBase<ILoggingEvent> {
    /**
     * Max tag length enforced by Android
     * http://developer.android.com/reference/android/util/Log.html#isLoggable(java.lang.String, int)
     */
    private static final int MAX_TAG_LENGTH = 23;
    private PatternLayoutEncoder encoder = null;
    private PatternLayoutEncoder tagEncoder = null;
    private boolean checkLoggable = false;

    /**
     * As in most cases, the default constructor does nothing.
     */
    public MyLogcatAppender() {
    }

    /**
     * Checks that required parameters are set, and if everything is in order,
     * activates this appender.
     */
    @Override
    public void start() {
        KLog.e("tagEncoder  : " + this.tagEncoder);
        // null
        KLog.e("encoder     : " + this.encoder);
        // ch.qos.logback.classic.encoder.PatternLayoutEncoder@41ab5518
        KLog.e("Layout      : " + this.encoder.getLayout());
        // ch.qos.logback.classic.PatternLayout("%d{HH} %-5relative [%thread][%file:%M:%line] - %msg%n")
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

        super.start();
    }

    /**
     * Writes an event to Android's logging mechanism (logcat)
     *
     * @param event
     *            the event to be logged
     */
    public void append(ILoggingEvent event) {

        if (!isStarted()) {
            return;
        }

        String tag = getTag(event);


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
                KLog.e(this.encoder.getLayout().doLayout(event));
                KLog.e(event.getMessage());

                // 设置mdc，根据mdc处理
                Map<String, String> mdcPropertyMap = event.getMDCPropertyMap();
                KLog.e("first ---- " + mdcPropertyMap.get("first"));
                KLog.e("last  ---- " + mdcPropertyMap.get("last"));


                long timeStamp = event.getTimeStamp();
                String formattedMessage = this.encoder.getLayout().doLayout(event);
                String loggerName = event.getLoggerName();
                String level = event.getLevel().toString();
                String threadName = event.getThreadName();

                logging_event log = new logging_event();
                log.setTimestmp(timeStamp+"");
                log.setFormatted_message(formattedMessage);
                log.setLogger_name(loggerName);
                log.setLevel_string(level);
                log.setThread_name(threadName);
                log.save();

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
     * Gets the pattern-layout encoder for this appender's <i>logcat</i> tag
     *
     * @return the pattern encoder
     */
    public PatternLayoutEncoder getTagEncoder() {
        return this.tagEncoder;
    }

    /**
     * Sets the pattern-layout encoder for this appender's <i>logcat</i> tag
     * <p>
     * The expanded text of the pattern must be less than 23 characters as
     * limited by Android. Layouts that exceed this limit are truncated,
     * and a star is appended to the tag to indicate this.
     * <p>
     * The <code>tagEncoder</code> result is limited to 22 characters plus a star to
     * indicate truncation (<code>%logger{0}</code> has no length limit in logback,
     * but <code>LogcatAppender</code> limits the length internally). For example,
     * if the <code>tagEncoder</code> evaluated to <code>foo.foo.foo.foo.foo.bar.Test</code>,
     * the tag seen in Logcat would be: <code>foo.foo.foo.foo.foo.ba*</code>.
     * Note that <code>%logger{23}</code> yields more useful results
     * (in this case: <code>f.f.foo.foo.bar.Test</code>).
     *
     * @param encoder
     *            the pattern-layout encoder; specify {@code null} to
     *            automatically use the logger's name as the tag
     */
    public void setTagEncoder(PatternLayoutEncoder encoder) {
        this.tagEncoder = encoder;
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