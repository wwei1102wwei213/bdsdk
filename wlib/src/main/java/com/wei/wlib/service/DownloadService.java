package com.wei.wlib.service;

import android.app.DownloadManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.Nullable;
import com.wei.wlib.R;
import com.wei.wlib.WLibConfig;

import java.io.File;


/**
 * 下载APK服务 并安装
 */
public class DownloadService extends Service {
    private static final String TAG = DownloadService.class.getSimpleName();

    public static final String DOWNLOAD_APK_NAME = "download_apk_name";
    public static final String DOWNLOAD_APK_URL = "download_apk_url";
    public static final String DOWNLOAD_APK_DIR = "/Download/APK";

    private static DownloadManager mDm;
    private BroadcastReceiver mReceiver;
    private long mDownLoadAPKId = -1;
    private String mNewestAppName;
    private String mDownloadUrl;

    private boolean mIsDownloading = false;

    @Override
    public void onCreate() {
        super.onCreate();
        mDm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new QueryProgressBinder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (mIsDownloading) {

        } else {
            mReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0);
                    if (id == mDownLoadAPKId) {
                        if (null != mReceiver) {
                            unregisterReceiver(mReceiver);
                        }
                        mIsDownloading = false;
                        installApk();
                    }
                }
            };
            registerReceiver(mReceiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
            mNewestAppName = intent.getStringExtra(DOWNLOAD_APK_NAME);
            mDownloadUrl = intent.getStringExtra(DOWNLOAD_APK_URL);
            deleteOldFile();
            startDownLoad();
        }
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void deleteOldFile() {
        File file1 = new File(Environment.getExternalStorageDirectory().getPath() + DOWNLOAD_APK_DIR, mNewestAppName);
        File file2 = new File(Environment.getExternalStorageDirectory().getPath() + DOWNLOAD_APK_DIR, WLibConfig.WLIB_CONFIG_UPDATE_APP);
        if (null != file1 && file1.exists()) {
            file1.delete();
        }
        if (null != file2 && file2.exists()) {
            file2.delete();
        }
    }

    private void startDownLoad() {
        DownloadManager.Request request = new DownloadManager.Request(
                Uri.parse(mDownloadUrl));
        request.setMimeType("application/vnd.android.package-archive");
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI);
        request.setTitle(getString(R.string.wlib_downloading_apk));
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir(DOWNLOAD_APK_DIR, mNewestAppName);
        mDownLoadAPKId = mDm.enqueue(request);
        mIsDownloading = true;
    }

    private void installApk() {
        Intent installIntent = new Intent(Intent.ACTION_VIEW);
        installIntent.setDataAndType(Uri.parse("file://" + Environment.getExternalStorageDirectory().getPath() + DOWNLOAD_APK_DIR + File.separator + mNewestAppName),
                "application/vnd.android.package-archive");
        installIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        installIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(installIntent);
        stopSelf();
    }

    private class QueryProgressBinder extends Binder {
        public Cursor getQueryCursor() {
            DownloadManager.Query query = new DownloadManager.Query();
            Cursor cursor = mDm.query(query.setFilterById(mDownLoadAPKId));
            return cursor;
        }

        public int getProgress() {
            Cursor cursor = getQueryCursor();
            if (null != cursor && cursor.moveToFirst()) {
                int bytesTotal = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
                int bytesDownload = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                int progress = (int) (bytesDownload * 1f / bytesTotal * 100);
                return progress;
            }
            return -1;
        }

        public int getTotalBytes() {
            Cursor cursor = getQueryCursor();
            if (null != cursor && cursor.moveToFirst()) {
                int bytesTotal = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
                cursor.close();
                return bytesTotal;
            }
            return -1;
        }

        public int getDownloadBytes() {
            Cursor cursor = getQueryCursor();
            if (null != cursor && cursor.moveToFirst()) {
                int bytesDownload = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                cursor.close();
                return bytesDownload;
            }

            return -1;
        }
    }
}
