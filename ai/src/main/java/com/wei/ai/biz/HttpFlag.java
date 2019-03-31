package com.wei.ai.biz;

import com.wei.wlib.http.WLibHttpFlag;

/**
 * Created by Administrator on 2018/3/30 0030.
 */

public class HttpFlag extends WLibHttpFlag {

    //默认API服务地址
    public static String BASE_URL = "http://39.98.221.101/chemicalFactory/android/";

    //签到
    public static final int FLAG_ATTENDANCE = 10;
    public static final String URL_ATTENDANCE = BASE_URL + "insertAttendance";

    //登陆
    public static final int FLAG_LOGIN = 11;
    public static final String URL_LOGIN = BASE_URL + "login";
    //姓名查询安全信息
    public static final int FLAG_SEARCH_NAME_FOR_SAFE = 12;
    public static final String URL_SEARCH_NAME_FOR_SAFE = BASE_URL + "searchSecurityInfoByName";
    //姓名查询违章信息
    public static final int FLAG_SEARCH_NAME_FOR_UNSAFE = 13;
    public static final String URL_SEARCH_NAME_FOR_UNSAFE = BASE_URL + "searchViolationInfoByName";
    //刷脸查询安全信息
    public static final int FLAG_SEARCH_FACE_FOR_SAFE = 14;
    public static final String URL_SEARCH_FACE_FOR_SAFE = BASE_URL + "searchSecurityInfoByImg";
    //刷脸查询违章信息
    public static final int FLAG_SEARCH_FACE_FOR_UNSAFE = 15;
    public static final String URL_SEARCH_FACE_FOR_UNSAFE = BASE_URL + "searchViolationInfoByImg";
    //添加违章信息
    public static final int FLAG_INSERT_VIOLATION_INFO = 16;
    public static final String URL_INSERT_VIOLATION_INFO = BASE_URL + "insertViolationInfo";
    //全部违章信息
    public static final int FLAG_SEARCH_VIOLATION_BY_PAGE = 17;
    public static final String URL_SEARCH_VIOLATION_BY_PAGE = BASE_URL + "searchViolationByPage";


    //切换服务器地址
    public static void changeBaseUrl() {

    }

}
