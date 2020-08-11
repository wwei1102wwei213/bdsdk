package com.wei.ai.biz;

import android.text.TextUtils;

import com.wei.wlib.http.WLibHttpFlag;

/**
 * Created by Administrator on 2018/3/30 0030.
 */

public class HttpFlag extends WLibHttpFlag {
    //默认API服务地址
    public static String BASE_URL = "";
    //签到数据上报
    public static final int FLAG_INSERT_ATTENDANCE = 20;
    public static String URL_INSERT_ATTENDANCE = BASE_URL + "send";
    //获取签到单号
    public static final int FLAG_GET_ATTENDANCE_NUM = 21;
    public static String URL_GET_ATTENDANCE_NUM = BASE_URL + "config";

    //切换服务器地址
    public static void changeBaseUrl(String host) {
        if (!TextUtils.isEmpty(host)) {
            if (!host.startsWith("http")) {
                host = "http://" + host;
            }
            if (!host.endsWith("/")) {
                BASE_URL = host + "/";
            } else {
                BASE_URL = host;
            }
        }
        URL_INSERT_ATTENDANCE = BASE_URL + "send";
        URL_GET_ATTENDANCE_NUM = BASE_URL + "config";
    }

}
