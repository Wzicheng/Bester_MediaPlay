package com.bester;

import android.app.Application;

import org.xutils.BuildConfig;
import org.xutils.x;

/**
 * Created by Wzich on 2017/9/25.
 */

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        x.Ext.init(this);
        //是否输出dubug日志
        x.Ext.setDebug(BuildConfig.DEBUG);
    }
}
