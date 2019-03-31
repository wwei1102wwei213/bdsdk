package com.wei.wlib.http;


/**
 * Created by Administrator on 2018/3/30 0030.
 */

public class WLibHttpFlag {

    public static final int RESULT_OK = 1;

    public static final int RESULT_DATA_FORMAT = -1;
    public static final int RESULT_DATA_NOT_FORMAT = 1;

    public static final int HTTP_ERROR_NETWORK = 1;             //网络异常
    public static final int HTTP_ERROR_DISCONNECT = 2;          //连接异常
    public static final int HTTP_ERROR_RESULT_EMPTY = 3;        //result数据为空
    public static final int HTTP_ERROR_FORMAT = 4;              //数据解析异常
    public static final int HTTP_ERROR_CODE = 5;                //数据code异常
    public static final int HTTP_ERROR_DATA_EMPTY = 6;          //data数据为空
    public static final int HTTP_ERROR_OTHER = 7;               //其他异常
    public static final int HTTP_ERROR_BASE_URL_CHANGED = 8;    //域名切换



}
