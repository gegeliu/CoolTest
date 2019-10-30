package com.ipgou.cooltest;

import android.content.Context;

/**
 * Created by lazen at 2019/10/30 17:35
 */
public class App {
    private static String TAG = "App";
    private static Context mContext = null;

    private static class LazyHolder {
        private static final App INSTANCE = new App();
    }
    public static final App getApp() {
        return LazyHolder.INSTANCE;
    }

    private App() {
    }

    public Context getContext() {
        return mContext;
    }
    public void setContext( Context context ){
        mContext = context;
    }

}
