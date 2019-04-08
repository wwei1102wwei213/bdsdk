package com.wei.ai;

import com.ta.TAApplication;
import com.wei.wlib.WLibManager;
import com.wei.wlib.util.WLibLog;

public class BaseApplication extends TAApplication{
    private static BaseApplication instance;
    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        //网络初始化
        WLibManager.getInstance().initOkHttp();
        //设置网络请求日志
        WLibLog.setIsDebug(BuildConfig.DEBUG);
    }
    public static synchronized BaseApplication getInstance() {
        return instance;
    }
}
