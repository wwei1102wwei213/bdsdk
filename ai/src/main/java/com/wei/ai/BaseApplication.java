package com.wei.ai;

import com.ta.TAApplication;

public class BaseApplication extends TAApplication{
    private static BaseApplication instance;
    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }
    public static synchronized BaseApplication getInstance() {
        return instance;
    }
}
