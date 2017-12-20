package com.example.logbackdemo.apag;



import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

/**
 * Logback日志类
 */
public class LogB {

    private LogB(){}
    public static LogB getInstance () {
        return SingleHolder.sInstance;
    }
    private static class SingleHolder {
        private static final LogB sInstance = new LogB();
    }

    private Logger logger = LoggerFactory.getLogger("Log");


    public static void i(String advert_id) {
    }

    public static void i(String type, int tag) {
    }

    public static void i(String type, int tag, String msg) {
    }


    public static void d(String msg) {
        MDC.put("table", "BootLog");
        getInstance().logger.debug(msg);
    }

    public static void w(String msg) {
        MDC.put("table", "MDCLog");
        getInstance().logger.warn(msg);
    }

    public static void e(String msg) {
        getInstance().logger.error(msg);
    }

}
