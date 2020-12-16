package com.newolf.speechtotextdemo;

import android.app.Application;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.Utils;

/**
 * 功能描述
 *
 * @author NeWolf
 * @since 2020-11-20
 */
public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Utils.init(this);
        LogUtils.getConfig().setGlobalTag("wolf");
    }
}
