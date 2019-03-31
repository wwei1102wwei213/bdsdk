package com.wei.wlib.biz.update;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.orhanobut.logger.Logger;
import com.wei.wlib.WLibConfig;
import com.wei.wlib.util.WLibLog;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;

/**
 * APP升级帮助类
 */
public class WLibUpdate {

    public static final int MSG_CONFIG = 1;
    public static final int MSG_CONFIG_SUCCEEDED = 2;
    public static final int MSG_CONFIG_FILED = 3;

    public static final int MSG_INSTALL = 4;
    public static final int MSG_UPDATE_PROGRESS = 5;

    public static final int MSG_DOWNLOAD_ERROR = 6;

    public static final String tag = WLibUpdate.class.getSimpleName();

    private static WLibUpdate ourInstance;

    private final OkHttpClient client = new OkHttpClient();
    private Gson gson = new Gson();
    private Activity context;

    private Handler mHandler = new UIHandler(this);

    private WLibUpdateDialog dialog;
//    private static String sign;

    public static WLibUpdate getInstance(Activity context) {

        synchronized (WLibUpdate.class) {
            if (ourInstance == null) {
                ourInstance = new WLibUpdate(context);
            } else {
                ourInstance.context = context;
            }
        }
        return ourInstance;
    }

    public static void giveUp() {
        if (ourInstance != null) {
            ourInstance.release();
            ourInstance = null;
        }
    }

    public static class UIHandler extends Handler {

        private WeakReference<WLibUpdate> mWeakReference;

        public UIHandler(WLibUpdate update) {
            super(Looper.getMainLooper());
            mWeakReference = new WeakReference<>(update);
        }

        @Override
        public void handleMessage(Message msg) {

            WLibUpdate update = mWeakReference.get();
            if (update == null) return;
            switch (msg.what) {
                case MSG_CONFIG:
                    if (msg.arg1 == MSG_CONFIG_SUCCEEDED) {
                        WLibUpdateConfig config = (WLibUpdateConfig) msg.obj;
                        if (config != null) {
                            try {
                                if (config.versionCode!=null&&config.downurl!=null) {
//                                    PreferencesUtils.putInt(update.context, Constant.SETTING_VERSION_CODE, Integer.parseInt(config.versionCode));
                                    if (update.haveNewer(Integer.parseInt(config.versionCode))) {
                                        update.showUpdateDialog(config);
//                                        sign = config.sign;
                                    }
                                }
                            } catch (Exception e){
                                WLibLog.e(e);
                            }
                        }
                    }
                    break;
                case MSG_INSTALL:
                    File file = (File) msg.obj;
                    update.installApk(file);
                    break;
                case MSG_UPDATE_PROGRESS:
                    update.updateProgress(msg.arg1);
                    break;
                case MSG_DOWNLOAD_ERROR:
                    update.handError((String) msg.obj);
            }
        }
    }

    private void handError(String string) {
        showToast(string);
        release();
    }

    private void showToast(String text) {
        if (TextUtils.isEmpty(text)) return;
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
    }

    private WLibUpdate(Activity context) {
        this.context = context;
    }

    public void updateProgress(int progress) {
        String format = "正在下载%d%%";
        dialog.setButtonText(String.format(format, progress));
        dialog.setProgress(progress);
    }

    /**
     * 是否有更新的版本
     *
     * @param newVersion
     * @return
     */
    public boolean haveNewer(int newVersion) {
        if (WLibUpdateHelper.getVersionCode(context) < newVersion) {
            return true;
        } else {
            return false;
        }
    }

    private void showUpdateDialog(final WLibUpdateConfig config) {

        if (dialog != null && dialog.isShowing()) {
            return;
        }

        WLibUpdateDialog.Builder builder = new WLibUpdateDialog.Builder(context);
        String format = "%s<font color='red'>V%s</font>";
        String title = String.format(format, config.title, config.versionName);
        dialog = builder.setText(Html.fromHtml(title), config.text).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    checkCache(Integer.parseInt(config.versionCode), config.downurl);
                }catch (Exception e){
                    WLibLog.e(e);
                }
            }
        }).create();
        dialog.setButtonText("马上升级");

        if (config.autoup.equals("1")) {  // 1:强制升级
            dialog.setCancelable(false);
            dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
                @Override
                public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                    return keyCode == KeyEvent.KEYCODE_BACK;
                }
            });
        } else {
            dialog.setCanceledOnTouchOutside(false);
            dialog.setClose();
        }
        dialog.show();
    }

    /**
     * 本地是否有下载过的
     *
     * @param newVersion
     */
    private void checkCache(int newVersion, String downurl) {
        if (!WLibUpdateHelper.SDCardExist()) {
            return;
        }
        try {
            File dir = new File(WLibConfig.appUpdate);
            if (!dir.exists() && !dir.isDirectory()) {
                dir.mkdirs();
            }
            String format = "update_%d.apk";
            File updateFile = new File(dir, String.format(format, newVersion));
            String temp = "update_%d.temp";
            File tempFile = new File(dir, String.format(temp, newVersion));
            if (updateFile.exists() && updateFile.isFile()) {
                installApk(updateFile);
            } else if (tempFile.exists() && tempFile.isFile()) {
                showToast("数据异常，请重新下载");
                //有些手机会存在临时文件
                TextView tv = dialog.getButtonText();
                try {
                    if (tv != null && "马上升级".equals(tv.getText().toString())) {
                        tempFile.delete();
                    }
                } catch (Exception e){
                    WLibLog.e(e);
                }
            } else {
                dialog.setButtonClickable(false);
                downloadInThread(downurl, tempFile, updateFile);
            }
        } catch (Exception e){
            WLibLog.e(e);
        }

    }

    /**
     * 安装apk
     */
    private void installApk(File apkFile) {
        try {
            //比较包名
            if (!WLibUpdateHelper.comparePackage(context, apkFile)) {
                showToast("安装包错误，您可能遭到DNS劫持，请稍后再重新升级");
                if (apkFile.isFile() && apkFile.exists()) {
                    apkFile.delete();
                }
                release();
                return;
            }
            //比较md5
        /*if (!WLibUpdateHelper.compareMd5(apkFile, sign)) {
            ToastUtils.showToast("安装包错误，请重新升级");
            if (apkFile.isFile() && apkFile.exists()) {
                apkFile.delete();
            }
            release();
            return;
        }*/

            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setDataAndType(Uri.parse("file://" + apkFile.toString()), "application/vnd.android.package-archive");
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            i.setFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
            context.startActivity(i);
            release();
//            android.os.Process.killProcess(android.os.Process.myPid());
        } catch (Exception e){
            WLibLog.e(e);
        }

    }

    public void release() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
        mHandler.removeCallbacksAndMessages(null);
    }

    private void downloadInThread(final String url, final File tempFile, final File updateFile) {
        new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    publishProgress(0);
                    download(url, tempFile);
                    Message msg = Message.obtain();

                    if (tempFile.exists() && tempFile.isFile()) {

                        if (updateFile.exists()) {
                            updateFile.delete();
                        }

                        // 下载完了重命名文件
                        if (tempFile.renameTo(updateFile)) {
                            msg.what = MSG_INSTALL;
                            msg.obj = updateFile;
                            mHandler.sendMessage(msg);
                        } else {
                            throw new IOException("文件重命名失败");
                        }
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                    try {
                        if (tempFile.exists() && tempFile.isFile()) {
                            tempFile.delete();
                        }

                        if (updateFile.exists() && updateFile.isFile()) {
                            updateFile.delete();
                        }

                        Message msg = Message.obtain();
                        msg.what = MSG_DOWNLOAD_ERROR;
                        msg.obj = "下载失败";
                        mHandler.sendMessage(msg);
                    } catch (Exception ex){
                        WLibLog.e(ex);
                    }
                } catch (Exception e){
                    WLibLog.e(e);
                }
            }
        }.start();
    }

    public void checkConfig(String url, boolean isManual) {

        if (dialog != null && dialog.isShowing()) {
            return;
        }

        Request request = new Request.Builder().url(url).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Logger.i(tag, call.request().url().query() + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) {
                try {
                    ResponseBody body = response.body();
                    WLibUpdateConfig config = gson.fromJson(body.charStream(), WLibUpdateConfig.class);
                    Log.d(tag, call.request().url().query() + response);
                    Message msg = Message.obtain();
                    msg.what = MSG_CONFIG;
                    msg.arg1 = MSG_CONFIG_SUCCEEDED;
                    msg.obj = config;
                    mHandler.sendMessage(msg);
                } catch (Exception e) {
                    e.printStackTrace();
                    Message msg = Message.obtain();
                    msg.what = MSG_DOWNLOAD_ERROR;
                    msg.obj = "升级文件异常";
                    mHandler.sendMessage(msg);
                }

            }
        });
    }

    private synchronized void download(@NonNull String url, @NonNull File destFile) throws IOException {
        Request request = new Request.Builder().url(url).build();
        Response response = client.newCall(request).execute();
        ResponseBody body = response.body();
        long contentLength = body.contentLength();
        BufferedSource source = body.source();

        BufferedSink sink = Okio.buffer(Okio.sink(destFile));
        Buffer sinkBuffer = sink.buffer();

        long totalBytesRead = 0;
        int bufferSize = 2 * 1024;
        for (long bytesRead; (bytesRead = source.read(sinkBuffer, bufferSize)) != -1; ) {
            sink.emit();
            totalBytesRead += bytesRead;
            int progress = (int) ((totalBytesRead * 100) / contentLength);
            publishProgress(progress);
        }
        sink.flush();
        sink.close();
        source.close();
    }

    private void publishProgress(int progress) {
        Message msg = Message.obtain();
        msg.what = MSG_UPDATE_PROGRESS;
        msg.arg1 = progress;
        mHandler.sendMessage(msg);
    }
}
