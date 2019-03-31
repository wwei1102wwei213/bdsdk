package com.wei.wlib.biz.update;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.text.TextUtils;

import com.orhanobut.logger.Logger;
import com.wei.wlib.util.WLibMd5;
import com.wei.wlib.util.WLibLog;

import java.io.File;

/**
 * APP升级帮助类
 */
public class WLibUpdateHelper {

    public static int appVersionCode = 0;

    /**
     * @param context
     * @return versionCode 返回当前应用的版本编号
     */
    public static int getVersionCode(Context context) {
        if (appVersionCode == 0) {
            PackageManager pm = context.getPackageManager();//拿到包的管理器
            try {
                //封装了所有的功能清单中的数据
                PackageInfo info = pm.getPackageInfo(context.getPackageName(), 0);
                appVersionCode = info.versionCode;
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }
        return appVersionCode;
    }

    /**
     * 检查SD卡是否存在
     *
     * @return
     */
    public static boolean SDCardExist() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }


    /**
     * 比较指定apk包名
     *
     * @param context
     * @param file
     * @return
     */
    public static boolean comparePackage(Context context, File file) {
        try {
            PackageManager pm = context.getApplicationContext().getPackageManager();
            PackageInfo pkgInfo = pm.getPackageArchiveInfo(file.getPath(), PackageManager.GET_ACTIVITIES);
            if (pkgInfo==null) return true;
            String name = pkgInfo.applicationInfo.packageName;
            if (name.equals(context.getPackageName())) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e){
            WLibLog.e(e);
        }
        return true;
    }

    /**
     * 比较指定apkMD5值
     *
     * @param file
     * @return
     */
    public static boolean compareMd5(File file, String md5) {

        if(file == null) return false;
        if(TextUtils.isEmpty(md5))return true;//升级文件没有sign,不做md5检查直接升级

        String md5New = WLibMd5.getFileMD5String(file);
        if(TextUtils.isEmpty(md5New)) return false;
        Logger.d(md5New);
        Logger.d(md5);
        if (md5New.equalsIgnoreCase(md5)) {
            return true;
        } else {
            return false;
        }
    }

}
