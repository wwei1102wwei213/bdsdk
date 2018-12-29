package com.wei.ai.utils;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;

/**
 * Created by Administrator on 2018/4/23 0023.
 */

public class WLibPermissionsBiz {

    private Context context;
    // 要申请的权限
    private String[] permissions;
    // 回调
    private RequestPermissionsListener listener;
    // 需要转跳权限设置
    private boolean jumpAppSettingAble = true;

    private int REQUEST_CODE;

    public void setJumpAppSettingAble(boolean jumpAppSettingAble) {
        this.jumpAppSettingAble = jumpAppSettingAble;
    }

    public WLibPermissionsBiz(Context context, String[] permissions, int code, RequestPermissionsListener listener){
        this.context = context;
        this.permissions = permissions;
        this.listener = listener;
        this.REQUEST_CODE = code;
    }

    //检查权限
    public void toCheckPermission(){
        // 版本判断。当手机系统大于 23 时，才有必要去判断权限是否获取
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            boolean isNeed = false;
            // 检查该权限是否已经获取
            // 权限是否已经 授权 GRANTED---授权  DINIED---拒绝
            for (int i = 0;i<permissions.length;i++) {
                int x = ContextCompat.checkSelfPermission(context, permissions[i]);
                if (x!= PackageManager.PERMISSION_GRANTED)  isNeed = true;
            }
            // 如果没有授予该权限，就去提示用户请求
            if (isNeed) {
                startRequestPermission();
            } else {
                if (listener!=null) listener.RequestComplete(true);
            }
        } else {
            if (listener!=null) listener.RequestComplete(true);
        }
    }

    //权限回调判断
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                boolean isOk = true;
                for (int i=0;i<grantResults.length;i++){
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) isOk = false;
                }
                if (!isOk){
                    // 判断用户是否 点击了不再提醒。(检测该权限是否还可以申请)
                    if (jumpAppSettingAble) {
                        boolean isHint = false;
                        for (int i=0;i<permissions.length;i++){
                            if (!((Activity)context).shouldShowRequestPermissionRationale(permissions[i])) isHint = true;
                        }
                        if (isHint) {// 提示用户去应用设置界面手动开启权限
                            showDialogTipUserGoToAppSetting();
                        }
                    }
                    if (listener!=null) listener.RequestComplete(false);
                } else {
                    if (listener!=null) listener.RequestComplete(true);
                }
            }
        }
    }

    //跳转设置界面开启权限
    private void showDialogTipUserGoToAppSetting() {
        new AlertDialog.Builder(context)
                .setTitle("功能权限未开启")
                .setMessage("请在-应用设置-权限-中，允许相应功能，若权限未开启将影响部分功能的使用！")
                .setPositiveButton("立即开启", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 跳转到应用设置界面
                        goToAppSetting();
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).setCancelable(false).show();
    }

    // 跳转到当前应用的设置界面
    private void goToAppSetting() {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", ((Activity)context).getPackageName(), null);
        intent.setData(uri);
        ((Activity)context).startActivityForResult(intent, 123);
    }

    // 开始提交请求权限
    private void startRequestPermission() {
        ActivityCompat.requestPermissions(
                (Activity)context, permissions, REQUEST_CODE);
    }

    public interface RequestPermissionsListener{
        //isOk为true时执行下一步, 用户拒绝时已经有提示，所以一般情况下,为false时不进行任何处理
        void RequestComplete(boolean isOk);
    }

}
