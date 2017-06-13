package com.han.nexttools;

import android.app.Application;

import cn.jpush.android.api.JPushInterface;


/**
 * Created by Han on 2017/6/12.
 */

public class MApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        JPushInterface.setDebugMode(true);
        JPushInterface.init(this);
    }
}
