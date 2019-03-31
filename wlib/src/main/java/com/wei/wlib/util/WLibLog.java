package com.wei.wlib.util;

import android.util.Log;

public class WLibLog {

    private static final String TAG = "WLibLog";

    private static boolean isDebug = true;

    public static void setIsDebug(boolean isDebug) {
        WLibLog.isDebug = isDebug;
    }

    public static void e (Throwable e) {
        if (isDebug) {
//            e.printStackTrace();
            Log.e("WLibLog", e.getMessage());
            try {
                Log.e(TAG,"异常信息：" + e.toString());
                StackTraceElement[] stack = e.getStackTrace();
                StringBuilder builder = new StringBuilder();
                for(int i = 0;i<stack.length;i++){
                    builder.append(stack[i].toString()+"\n");
                }
                Log.e(TAG, "异常信息详情：" + builder.toString());
            }catch (Exception ex){
                ex.printStackTrace();
            }
        }
    }

    public static void e (Throwable e, String where) {
        if (isDebug) {
//            e.printStackTrace();
            Log.e("WLibLog", where);
            Log.e("WLibLog", e.getMessage());
            try {
                Log.e(TAG,"异常信息：" + e.toString());
                StackTraceElement[] stack = e.getStackTrace();
                StringBuilder builder = new StringBuilder();
                for(int i = 0;i<stack.length;i++){
                    builder.append(stack[i].toString()+"\n");
                }
                Log.e(TAG, "异常信息详情：" + builder.toString());
            }catch (Exception ex){
                ex.printStackTrace();
            }
        }
    }

    public static void e(Exception e) {
        if (isDebug) {
//            e.printStackTrace();
            Log.e("WLibLog", e.getMessage());
            try {
                Log.e(TAG,"异常信息：" + e.toString());
                StackTraceElement[] stack = e.getStackTrace();
                StringBuilder builder = new StringBuilder();
                for(int i = 0;i<stack.length;i++){
                    builder.append(stack[i].toString()+"\n");
                }
                Log.e(TAG, "异常信息详情：" + builder.toString());
            }catch (Exception ex){
                ex.printStackTrace();
            }
        }
    }

    public static void e (String errorStr) {
        if (isDebug) {
            Log.e(TAG, errorStr);
        }
    }

    public static void e (String tag, String s) {
        if (isDebug) {
            Log.e(TAG, tag+"\n"+s);
        }
    }


    public static void h (String tag, String msg) {
        if (isDebug) {
            Log.d(TAG, tag+"\n"+msg);
        }
    }

    public static void h (String msg) {
        if (isDebug) {
            Log.d(TAG, msg);
        }
    }

}
