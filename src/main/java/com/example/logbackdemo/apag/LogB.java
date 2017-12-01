package com.example.logbackdemo.apag;


import com.jingang.ad_fabuyun.utils.LogUpload;
import com.jingang.ad_fabuyun.utils.RecordUtil;

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
        RecordUtil.getInstance().updateRecodeTable(advert_id, 1);
    }

    public static void i(String type, int tag) {
        LogUpload.getLogUpload().dbUpdate(type, tag);
    }

    public static void i(String type, int tag, String msg) {
        LogUpload.getLogUpload().dbUpdate(type, tag, msg);
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
