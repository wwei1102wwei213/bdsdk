package com.wei.wlib;

import android.os.Environment;

public class WLibConfig {

    public static String root = Environment.getExternalStorageDirectory().getAbsolutePath();
    public static String appRoot = root + "/wlib";
    public static String appBook = appRoot + "/books";//小说内容目录
    public static String appUpdate = appRoot + "/update";//升级包目录
    public static String appFonts = appRoot + "/fonts";//字体
    public static String appTemp = appRoot + "/temp";//临时文件
    public static String appDown = appRoot + "/download";//下载文件
    public static String appThem = appDown + "/them";//主题图片

    public static final String WLIB_CONFIG_UPDATE_APP = "update.apk";



}
