package com.wei.wlib.util;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.wei.wlib.R;
import com.wei.wlib.WLibConfig;
import com.wei.wlib.service.DownloadService;

import java.io.File;
import java.util.List;

public class WLibTools {

    /**
     * 启动下载APK服务
     * @param context
     * @param downUrl
     */
    public static void startDownloadApkService(Context context, String downUrl) {
        if (downUrl==null) return;
        try {
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                String downloadDir = Environment.getExternalStorageDirectory().getPath() + DownloadService.DOWNLOAD_APK_DIR;
                File file = new File(downloadDir);
                if (!file.exists()) {
                    file.mkdirs();
                }
                String apkName = TextUtils.isEmpty(downUrl) ? WLibConfig.WLIB_CONFIG_UPDATE_APP : downUrl.substring(downUrl.lastIndexOf("/"));
                Intent downloadIntent = new Intent(context, DownloadService.class);
                downloadIntent.putExtra(DownloadService.DOWNLOAD_APK_NAME, apkName);
                downloadIntent.putExtra(DownloadService.DOWNLOAD_APK_URL, downUrl);
                context.startService(downloadIntent);
            } else {
                Toast.makeText(context, context.getResources().getString(R.string.wlib_not_find_sdcard), Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e){
            WLibLog.e(e);
        }
    }

    /**
     * 判断应用是否已经启动
     *
     * @param context     一个context
     * @param packageName 要判断应用的包名
     * @return boolean
     */
    public static boolean isAppAlive(Context context, String packageName) {

        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> processInfos = activityManager.getRunningAppProcesses();
        for (int i = 0; i < processInfos.size(); i++) {
            if (processInfos.get(i).processName.equals(packageName)) {
                //app进程存在
                return true;
            }
        }
        //app还没启动
        return false;
    }

    /**
     * 根据传入控件的坐标和用户的焦点坐标，判断是否隐藏键盘，如果点击的位置在控件内，则不隐藏键盘
     *
     * @param view  控件view
     * @param event 焦点位置
     * @return 是否隐藏
     */
    public static void hideKeyboard(MotionEvent event, View view,
                                    Activity activity) {
        try {
            if (view != null && view instanceof EditText) {
                int[] location = {0, 0};
                view.getLocationInWindow(location);
                int left = location[0], top = location[1], right = left
                        + view.getWidth() + dip2px(activity, 30f), bootom = top + view.getHeight();//right加30dp,包含清空图标
                // 判断焦点位置坐标是否在空间内，如果位置在控件外，则隐藏键盘
                if (event.getRawX() < left || event.getRawX() > right
                        || event.getY() < top || event.getRawY() > bootom) {
                    // 隐藏键盘
                    IBinder token = view.getWindowToken();
                    InputMethodManager inputMethodManager = (InputMethodManager) activity
                            .getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputMethodManager.hideSoftInputFromWindow(token,
                            InputMethodManager.HIDE_NOT_ALWAYS);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void showSoftKeyboard(Context mContext, View view) {
        if (view.requestFocus()) {
            InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    public static boolean hasSdcard() {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public static int dip2px(Context context, int dp) {
        DisplayMetrics metrics = getDisplayMetrics(context);
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, metrics);
    }

    public static DisplayMetrics getDisplayMetrics(Context context) {
        DisplayMetrics metrics = context.getApplicationContext()
                .getResources()
                .getDisplayMetrics();
        return metrics;
    }

}
