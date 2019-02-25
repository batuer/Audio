package com.gusi.audio;

import android.app.Application;

import com.gusi.audio.utils.ToastUtils;

/**
 * @Author ylw  2019/2/23 18:51
 */
public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        ToastUtils.getInstance()
                .setContext(getApplicationContext());
    }
}
