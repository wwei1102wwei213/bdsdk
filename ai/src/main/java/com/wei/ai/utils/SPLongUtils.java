package com.wei.ai.utils;

import android.content.Context;
import android.content.SharedPreferences;



/**
 * Created by Administrator on 2018/5/16 0016.
 */

public class SPLongUtils {

    private final static String SP_NAME = "mai_long_config";

    public static boolean isFirst(Context context) {
        try {
            SharedPreferences sp = context.getSharedPreferences(SP_NAME, 0);
            return sp.getBoolean("mai_first_run", true);
        }catch (Exception e){

        }
        return true;
    }

    public static void saveFirst(Context context, boolean isFirst) {
        try {
            SharedPreferences sp = context.getSharedPreferences(SP_NAME, 0);
            sp.edit().putBoolean("mai_first_run", isFirst).apply();
        }catch (Exception e){

        }
    }

    /**
     * 获取int值
     *
     * @param context
     * @param key
     * @param defValue
     * @return
     */
    public static int getInt(Context context, String key, int defValue) {
        try {
            SharedPreferences sp = context.getSharedPreferences(SP_NAME, 0);
            return sp.getInt(key, defValue);
        }catch (Exception e){

        }
        return defValue;
    }

    /**
     * 保存int
     *
     * @param context
     * @param key
     * @param value
     */
    public static void saveInt(Context context, String key, int value) {
        try {
            SharedPreferences sp = context.getSharedPreferences(SP_NAME, 0);
            sp.edit().putInt(key, value).apply();
        }catch (Exception e){

        }
    }

    /**
     * 保存字符串
     *
     * @param context
     * @param key
     * @param value
     */
    public static void saveString(Context context, String key, String value) {
        try {
            SharedPreferences sp = context.getSharedPreferences(SP_NAME, 0);
            sp.edit().putString(key, value).apply();
        }catch (Exception e){

        }
    }

    /**
     * 获取字符值
     *
     * @param context
     * @param key
     * @param defValue
     * @return
     */
    public static String getString(Context context, String key, String defValue) {
        try {
            SharedPreferences sp = context.getSharedPreferences(SP_NAME, 0);
            return sp.getString(key, defValue);
        }catch (Exception e){

        }
        return defValue;
    }

}
