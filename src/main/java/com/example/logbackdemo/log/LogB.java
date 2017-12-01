package com.example.logbackdemo.log;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * LogBack 日志处理
 */
public class LogB {
    public static final String L_APP = "";
    public static final String L_SERVER = "";

    public static final String FORMAT_RECORD = "advert_id";

    private Logger logger_server;
    private Logger logger_app;
    private Logger logger_network;
    private Logger logger_Exception;
    public void init() {
        logger_server = LoggerFactory.getLogger(L_SERVER);
        logger_app = LoggerFactory.getLogger(L_APP);
    }

    public void d(String msg) {
        logger_server.debug((String) msg);
    }


    public void i(String advert_id, int play_count) {

    }



}
