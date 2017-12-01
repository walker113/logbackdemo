package com.example.logbackdemo;

import android.app.Activity;
import android.os.Bundle;
import android.view.MenuItem;

import com.socks.library.KLog;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.slf4j.Marker;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.concurrent.Executors;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.android.SQLiteAppender;
import ch.qos.logback.core.util.StatusPrinter;

public class MainActivity extends Activity {

    private Logger mLog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MDC.put("first", "Dorothy");
        MDC.put("last", "Parker");
        // Note that the above example does not reference any logback classes
        // 在大多数情况下，就日志而言，你的类只需要导入SLF4J类。
        mLog = LoggerFactory.getLogger(MainActivity.class);
        mLog.error("hello world");

        // let us instruct logback to print its internal state by invoking the static print() method of the StatusPrinter class
        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        StatusPrinter.print(lc);

        // 设置级别后，低于设置级别无法打印
        ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger("com.foo");
        logger.setLevel(Level.INFO);

        Object entry = new Integer[]{100, 1000};
        logger.error("AD_log" + "The entry is {}.", entry);
        logger.error("table {} ", "adLog");

        Logger loggerR = LoggerFactory.getLogger("RecordLogger");
        KLog.e("loggerR : " + loggerR);
        loggerR.error("RecordLogger.RecordLogger.RecordLogger");

        int[] arr = new int[]{1};
        try {
            int a = arr[3];
        } catch (Exception e) {
//            loggerR.error("AD_log" + "[lid:{}] [{}]<< exception happened! detail:{}", getExceptionStackTrace(e));
        }
    }

    public static String getExceptionStackTrace(Throwable anexcepObj) {
        StringWriter sw = null;
        PrintWriter printWriter = null;
        try {
            if(anexcepObj != null) {
                sw = new StringWriter();
                printWriter = new PrintWriter(sw);
                anexcepObj.printStackTrace(printWriter);
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

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {



        return super.onOptionsItemSelected(item);
    }
}
