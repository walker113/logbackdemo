package com.example.logbackdemo;

import android.content.Context;
import android.util.Log;


public class SqlImpl extends SQLCipherAppender {
    @Override
    String getDbPassword() {
        return null;
    }

    @Override
    Context getApplicationContext() {
        return LogApplication.getContext();
    }
}
