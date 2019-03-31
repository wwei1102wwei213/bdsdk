package com.wei.wlib.downloader;

/**
 * Created by Administrator on 2016/3/7.
 */
import java.io.File;

/**
 *
 * 该接口用于监听JFileDownloader下载的进度。
 *
 * @author
 * @version
 */
public interface JFileDownloadListener {
    /**
     * 该方法可获得文件的下载进度信息。
     * @author
     * @param progress 文件下载的进度值，范围（0-100）。0表示文件还未开始下载；100则表示文件下载完成。
     * @param speed 此时下载瞬时速度（单位：kb/每秒）。
     * @param remainTime 此时剩余下载所需时间（单位为毫秒）。
     */
    public void downloadProgress(int progress, double speed, long remainTime);
    /**
     * 文件下载完成会调用该方法。
     * @author
     * @param file 返回下载完成的File对象。
     * @param downloadTime 下载所用的总时间（单位为毫秒）。
     */
    public void downloadCompleted(File file, long downloadTime);

    void onFail();

}
