package com.example.logbackdemo;


import android.database.sqlite.SQLiteStatement;

import com.socks.library.KLog;

import org.litepal.util.LogUtil;

import java.io.File;
import java.sql.SQLException;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.UnsynchronizedAppenderBase;

public class LitePalAppender extends UnsynchronizedAppenderBase<ILoggingEvent> {

    @Override
    public void start() {


        this.started = false;
        clearExpiredLogs();

        super.start();

    }

    private void clearExpiredLogs() {

    }



    @Override
    protected void append(ILoggingEvent eventObject) {
        long timeStamp = eventObject.getTimeStamp();
        String formattedMessage = eventObject.getFormattedMessage();
        String loggerName = eventObject.getLoggerName();
        String level = eventObject.getLevel().toString();
        String threadName = eventObject.getThreadName();

        Object[] argumentArray = eventObject.getArgumentArray();
        String[] args = bindLoggingEventArguments(argumentArray);


        StackTraceElement stackTraceElement = bindCallerData(eventObject.getCallerData());

        logging_event log = new logging_event();
        log.setTimestmp(timeStamp+"");
        log.setFormatted_message(formattedMessage);
        log.setLogger_name(loggerName);
        log.setLevel_string(level);

        String table = formattedMessage.substring(0, 5);

        /*
         * 新增播放记录
         * table = Ad_Record,msg = advert_id,play_count = 1
         * 新增下发内容
         * table = Ad_Log, int type, int tag
         * 不同数据、不同处理
         * 根据类型或者调用之前的日志代码
         *
         * 表结构的冲突
         * 数据存储解析的冲突
         * L.d("advert_id=1234");
         * L.w();
         *
         */
        log.setThread_name(threadName);

        if (args.length == 4) {
            log.setArg0(args[0]);
            log.setArg1(args[1]);
            log.setArg2(args[2]);
            log.setArg3(args[3]);
        }

        if (stackTraceElement != null) {
            log.setCaller_filename(stackTraceElement.getFileName());
            log.setCaller_class(stackTraceElement.getClassName());
            log.setCaller_method(stackTraceElement.getMethodName());
            log.setCaller_line(stackTraceElement.getLineNumber() + "");
        }

        log.save();




    }

    private StackTraceElement bindCallerData(StackTraceElement[] callerDataArray) {
        if (callerDataArray != null && callerDataArray.length > 0) {
            StackTraceElement callerData = callerDataArray[0];
//            if (callerData != null) {
//                stmt.bindString(CALLER_FILENAME_INDEX, callerData.getFileName());
//                stmt.bindString(CALLER_CLASS_INDEX, callerData.getClassName());
//                stmt.bindString(CALLER_METHOD_INDEX, callerData.getMethodName());
//                stmt.bindString(CALLER_LINE_INDEX, Integer.toString(callerData.getLineNumber()));
//            }
            return callerData;
        }
        return null;
    }

    private static final int  ARG0_INDEX = 7;
    private String[] bindLoggingEventArguments(Object[] argArray) {
        int arrayLen = argArray != null ? argArray.length : 0;
        String[] args = new String[arrayLen];
        for (int i = 0; i < arrayLen && i < 4; i++) {
            args[i] = asStringTruncatedTo254(argArray[i]);
        }
        return args;
    }

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



    @Override
    protected void finalize() throws Throwable {
        super.finalize();
    }

    @Override
    public void stop() {
        super.stop();
    }

}
