package com.example.logbackdemo;

import android.app.Application;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import org.litepal.LitePalApplication;
import org.litepal.tablemanager.Connector;


public class LogApplication extends LitePalApplication {

    public SQLiteDatabase litepalDB;
    public static Context getContext() {
        return context;
    }

    private static Context context;
    @Override
    public void onCreate() {
        super.onCreate();

        // 数据库初始化
        litepalDB = Connector.getDatabase();
        context = getApplicationContext();
    }

}
