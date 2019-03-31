package com.wei.wlib.downloader;

import java.io.File;
import java.math.BigDecimal;

/**
 * 该线程为通知下载进度的线程。
 * 用于在下载未完成时通知用户下载的进度，范围（0-100）。0表示文件还未开始下载；100则表示文件下载完成。
 * 此时下载瞬时速度（单位：kb/每秒）。
 * 在完成时返回下载完成的File对象给用户。返回下载所用的总时间（单位为毫秒）给用户。
 * @author
 * @version
 */
public class JFileDownloaderNotificationThread extends Thread {
    private JFileDownloadThread[] threads;
    private JFileDownloadListener fileDownloadListener;
    private File destFile;
    private long destFileSize;
    private boolean isRunning; // 线程运行停止标志
    private boolean notificationTag; // 通知标志
    /**
     * 通过该方法构建一个进度通知线程。
     * @param threads 下载某文件需要的所有线程。
     * @param fileDownloadListener 要通知进度的监听器对象。
     * @param destFile 下载的文件对象。
     */
    public JFileDownloaderNotificationThread(JFileDownloadThread[] threads,
                                             JFileDownloadListener fileDownloadListener, File destFile, long destFileSize) {
        this.threads = threads;
        this.fileDownloadListener = fileDownloadListener;
        this.destFile = destFile;
        this.destFileSize = destFileSize;
    }

    /**
     * 不断地循环来就检查更新进度。
     */
    @Override
    public void run() {
        isRunning = true;
        long startTime = 0;
        if(null != fileDownloadListener){
            startTime = System.currentTimeMillis(); // 文件下载开始时间
        }

        long oldTemp = 0; // 上次已下载数据长度
        long oldTime = 0; // 上次下载的当前时间

        while(isRunning){
            if(notificationTag){ // 如果此时正等待检查更新进度。
                // 计算此时的所有线程下载长度的总和
                long temp = 0;
                for(JFileDownloadThread thread : threads){
                    temp += thread.currentLength;
                }
//                System.out.println("temp: " + temp);
//                System.out.println("destFileSize: " + destFileSize);
                // 换算成进度
                int progress = (int) ((double)temp * 100 / (double)destFileSize);

                // 把进度通知给监听器
                if(null != fileDownloadListener){
                    // 计算瞬时速度
                    long detaTemp = temp - oldTemp; // 两次更新进度的时间段内的已下载数据差
                    long detaTime = System.currentTimeMillis() - oldTime; // 两次更新进度的时间段内的时间差
                    // 两次更新进度的时间段内的速度作为瞬时速度
                    double speed = ((double)detaTemp / 1024) / ((double)(detaTime) / 1000);

                    // 保留小数点后2位，最后一位四舍五入
                    speed = new BigDecimal(speed).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();

                    // 计算剩余下载时间
                    double remainTime = (double)(destFileSize - temp) / speed;
                    if(Double.isInfinite(remainTime) || Double.isNaN(remainTime)){
                        remainTime = 0;
                    }else{
                        remainTime = new BigDecimal(remainTime).setScale(0, BigDecimal.ROUND_HALF_UP).longValue();
                    }

                    // 通知监听者进度和速度以及下载剩余时间
                    fileDownloadListener.downloadProgress(progress, speed, (long)remainTime);

                    // 重置上次已下载数据长度和上次下载的当前时间
                    oldTemp = temp;
                    oldTime = System.currentTimeMillis();
                }

                // 如果下载进度达到100，则表示下载完毕
                if(100 <= progress){
                    // 给下载好的文件进行重命名，即去掉DOWNLOADING_SUFFIX后缀
                    String oldPath = destFile.getPath();
                    File newFile = new File(oldPath.substring(0, oldPath.lastIndexOf(".")));
                    // 检查去掉后的文件是否存在。如果存在，则删除原来的文件并重命名下载的文件（即：覆盖原文件）
                    if(newFile.exists()){
                        newFile.delete();
                    }
                    System.out.println(destFile.renameTo(newFile));// 重命名
                    // 通知监听器，并传入新的文件对象
                    if(null != fileDownloadListener){
                        fileDownloadListener.downloadCompleted(newFile, System.currentTimeMillis() - startTime);
                    }
                    isRunning = false; // 文件下载完就结束通知线程。
                }
                notificationTag = false;
            }
            // 设置为每100毫秒进行检查并更新通知
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }

    }
    /**
     * 调用这个方法，则会使得线程处于待检查更新进度状态。
     * @author
     */
    public synchronized void notificationProgress(){
        notificationTag = true;
    }
    /**
     * 取消该通知线程
     * @author wwei
     */
    public void cancelThread(){
        isRunning = false;
    }


}
