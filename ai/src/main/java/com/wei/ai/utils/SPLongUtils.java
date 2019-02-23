package com.yyspbfq.filmplay.utils.sp;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import com.orhanobut.logger.Logger;
import com.yyspbfq.filmplay.utils.BLog;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by Administrator on 2018/5/16 0016.
 */

public class SPLongUtils {

    private final static String SP_NAME = "filmplay_long_config";

    public static boolean isFirst(Context context) {
        try {
            SharedPreferences sp = context.getSharedPreferences(SP_NAME, 0);
            return sp.getBoolean("filmplay_first_run", true);
        }catch (Exception e){
            BLog.e(e);
        }
        return true;
    }

    public static void saveFirst(Context context, boolean isFirst) {
        try {
            SharedPreferences sp = context.getSharedPreferences(SP_NAME, 0);
            sp.edit().putBoolean("filmplay_first_run", isFirst).apply();
        }catch (Exception e){
            BLog.e(e);
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
            BLog.e(e);
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
            BLog.e(e);
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
            BLog.e(e);
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
            BLog.e(e);
        }
        return defValue;
    }

    /**
     * 获取字符值
     *
     * @param context
     * @param key
     * @param defValue
     * @return
     */
    public static <T> Object getString(Context context, String key, String defValue, Class<T> mClass) {
        try {
            SharedPreferences sp = context.getSharedPreferences(SP_NAME, 0);
            String result = sp.getString(key, defValue);
            if (TextUtils.isEmpty(result)) {
                return defValue;
            }
            Object o = new Gson().fromJson(result, mClass);
            return o;
        } catch (JsonParseException je){
            BLog.e(je);
        } catch (Exception e){
            BLog.e(e);
        }
        return defValue;
    }

    private static final String KEY_INVITE_CODE_URL = "key_invite_code_url";
    public static void saveInviteCodeUrl(Context context, String url) {
        if (url==null) return;
        try {
            saveString(context, KEY_INVITE_CODE_URL, url);
        } catch (Exception e){
            BLog.e(e);
        }
    }

    public static String getInviteCodeUrl(Context context) {
        return getString(context, KEY_INVITE_CODE_URL, "");
    }

    private static final String KEY_SEARCH_RECORD = "key_search_record";
    public static void saveSearchRecord(Context context, String tag) {
        if (TextUtils.isEmpty(tag)) return;
        try {
            Object old = getString(context, KEY_SEARCH_RECORD, null, new TypeToken<List<String>>(){}.getRawType());
            if (old==null||TextUtils.isEmpty(old.toString())) {
                List<String> temp = new ArrayList<>();
                temp.add(tag);
                Logger.d("temp 1:"+temp.toString());
                saveString(context, KEY_SEARCH_RECORD, new Gson().toJson(temp));
            } else {
                List<String> newData = new ArrayList<>();
                newData.add(tag);
                List<String> temp = (List<String>) old;
                Logger.d("temp 2:"+temp.toString());
                if (temp.contains(tag)) {
                    temp.remove(tag);
                }
                newData.addAll(temp);
                saveString(context, KEY_SEARCH_RECORD, new Gson().toJson(newData));
            }
        } catch (Exception e){
            BLog.e(e);
        }
    }

    public static List<String> getSearchRecord(Context context) {
        List<String> result = new ArrayList<>();
        try {
            Object old = getString(context, KEY_SEARCH_RECORD, null, new TypeToken<List<String>>() {
            }.getRawType());
            Logger.d("old:"+old);
            if (old != null && !TextUtils.isEmpty(old.toString())) {
                return (List<String>) old;
            }
        } catch (Exception e) {
            BLog.e(e);
        }
        return result;
    }

    public static void clearSearchRecord (Context context) {
        saveString(context, KEY_SEARCH_RECORD, "");
    }

}
