package com.wei.wlib.downloader;

import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * 文件下载器
 * @author
 * @version
 */
public class JFileDownloader{
    private String urlPath;
    private String destFilePath;
    private int threadCount;
    private JFileDownloadThread[] threads;

    private JFileDownloadListener fileDownloadListener; // 进度监听器
    private JFileDownloaderNotificationThread notificationThread; // 通知进度线程

    private File destFile;
    /**
     * 下载过程中文件的后缀名。
     */
    public final static String DOWNLOADING_SUFFIX = ".jd";
    /**
     * 默认使用的线程数量。<br>
     * 如果不设置线程数量参数（threadCount），则默认线程启动数量为1，即单线程下载。
     */
    public static final int DEFAULT_THREADCOUNT = 1;
    /**
     * 生成JFileDownloader对象。
     * @param urlPath 要下载的目标文件URL路径
     * @param destFilePath 要保存的文件目标（路径+文件名）
     * @param threadCount 下载该文件所需要的线程数量
     */
    public JFileDownloader(String urlPath, String destFilePath, int threadCount) {
        this.urlPath = urlPath;
        this.destFilePath = destFilePath;
        this.threadCount = threadCount;
    }
    /**
     * 生成JFileDownloader对象，其中下载线程数量默认是1，也就是选择单线程下载。
     * @param urlPath urlPath 要下载的目标文件URL路径
     * @param destFilePath destFilePath 要保存的文件目标（路径+文件名）
     */
    public JFileDownloader(String urlPath, String destFilePath) {
        this(urlPath, destFilePath, DEFAULT_THREADCOUNT);
    }
    /**
     * 默认的构造方法，使用构造方法后必须要调用set方法来设置url等下载所需配置。
     */
    public JFileDownloader() {

    }
    /**
     * 开始下载方法（流程分为3步）。
     * <br><ul>
     * <li>检验URL的合法性<br>
     * <li>计算下载所需的线程数量和每个线程需下载多少大小的文件<br>
     * <li>启动各线程。
     * </ul>
     * @author
     * @throws Exception 如果设置的URL，includes等参数不合法，则抛出该异常
     */
    public void startDownload() throws Exception {
        checkSettingfValidity(); // 检验参数合法性

        URL url = new URL(urlPath);
        HttpURLConnection conn = (HttpURLConnection)url.openConnection();
        conn.setConnectTimeout(20 * 1000);
        // 获取文件长度
        long size = conn.getContentLength();
//        int size = conn.getInputStream().available();
        if(size < 0 || null == conn.getInputStream()){
            throw new Exception("网络连接错误，请检查URL地址是否正确");
        }
        conn.disconnect();


        // 计算每个线程需要下载多少byte的文件
        long perSize = size % threadCount == 0 ? size / threadCount : (size / threadCount + 1);
        // 建立目标文件（文件以.jd结尾）
        destFile = new File(destFilePath + DOWNLOADING_SUFFIX);
        destFile.createNewFile();

        threads = new JFileDownloadThread[threadCount];

        // 启动进度通知线程
        notificationThread = new JFileDownloaderNotificationThread(threads, fileDownloadListener, destFile, size);
        notificationThread.start();

        // 初始化若干个下载线程
        for(int i = 0; i < threadCount; i++){
            if(i != (threadCount - 1)){
                threads[i] = new JFileDownloadThread(urlPath, destFile,
                        i * perSize, perSize, notificationThread);
            }else{
                threads[i] = new JFileDownloadThread(urlPath, destFile,
                        i * perSize, size - (threadCount - 1) * perSize, notificationThread);
            }
            threads[i].setPriority(8);
//            threads[i].start();
        }
        // 启动若干个下载线程（因为下载线程JFileDownloaderNotificationThread中使用了threads属性，所以必须等下载线程全部初始化以后才能启动线程）
        for(JFileDownloadThread thread : threads){
            thread.start();
        }



    }
    /**
     * 取消所有下载线程。
     * @author wwei
     */
    public void cancelDownload(){
        if(null != threads && 0 != threads.length && null != notificationThread){
            for(JFileDownloadThread thread : threads){ // 终止所有下载线程
                thread.cancelThread();
            }
            notificationThread.cancelThread(); // 终止通知线程
            System.out.println("下载已被终止。");
            return;
        }
        System.out.println("下载线程还未启动，无法终止。");
    }

    /**
     * 设置要下载的目标文件URL路径。
     * @author wwei
     * @param urlPath 要下载的目标文件URL路径
     * @return 返回当前JFileDownloader对象
     */
    public JFileDownloader setUrlPath(String urlPath) {
        this.urlPath = urlPath;
        return this;
    }
    /**
     * 设置要保存的目标文件（路径+文件名）。
     * @param destFilePath 要保存的文件目标（路径+文件名）
     * @return 返回当前JFileDownloader对象
     */
    public JFileDownloader setDestFilePath(String destFilePath) {
        this.destFilePath = destFilePath;
        return this;
    }
    /**
     * 设置下载该文件所需要的线程数量。
     * @param threadCount 下载该文件所需要的线程数量
     * @return 返回当前JFileDownloader对象
     */
    public JFileDownloader setThreadCount(int threadCount) {
        this.threadCount = threadCount;
        return this;
    }

    //观察者模式来获取下载进度
    /**
     * 设置监听器，以获取下载进度。
     */
    public JFileDownloader setFileDownloadListener(JFileDownloadListener fileDownloadListener) {
        this.fileDownloadListener = fileDownloadListener;
        return this;
    }
    /**
     * 通过该方法移出相应的监听器对象。
     * @param fileDownloadListener 要移除的监听器对象
     */
    public void removeFileDownloadListener(JFileDownloadListener fileDownloadListener) {
        fileDownloadListener = null;
    }


    /**
     * 检验设置的参数是否合法。
     * @throws Exception 目标文件URL路径不合法，或者线程数小于1，则抛出该异常
     */
    private void checkSettingfValidity() throws Exception {
        if(null == urlPath || "".equals(urlPath)){
            throw new Exception("目标文件URL路径不能为空");
        }
        if(threadCount < 1){
            throw new Exception("线程数不能小于1");
        }
    }

}
