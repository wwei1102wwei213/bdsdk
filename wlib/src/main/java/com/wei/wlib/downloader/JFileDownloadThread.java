package com.wei.wlib.downloader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 *
 * 真正的下载线程，该线程用于执行该线程所要负责下载的数据。
 *
 * @author
 * @version
 */
public class JFileDownloadThread extends Thread {
    private String urlPath;
    private File destFile;
    private long startPos;
    /**
     * 此线程需要下载的数据长度。
     */
    public long length;
    /**
     * 此线程现在已下载好了的数据长度。
     */
    public long currentLength;

    private JFileDownloaderNotificationThread notificationThread;
    private boolean isRunning = true;

    /**
     * 构造方法，可生成配置完整的JFileDownloadThread对象
     * @param urlPath 要下载的目标文件URL
     * @param destFile 要保存的目标文件
     * @param startPos 该线程需要下载目标文件第几个byte之后的数据
     * @param length 该线程需要下载多少长度的数据
     * @param notificationThread 通知进度线程
     */
    public JFileDownloadThread(String urlPath, File destFile, long startPos,
                               long length, JFileDownloaderNotificationThread notificationThread) {
        this.urlPath = urlPath;
        this.destFile = destFile;
        this.startPos = startPos;
        this.length = length;
        this.notificationThread = notificationThread;
    }
    /**
     * 该方法将执行下载功能，并把数据存储在目标文件中的相应位置。
     */
    @Override
    public void run() {
        RandomAccessFile raf = null;
        HttpURLConnection conn = null;
        InputStream is = null;
        try {
//            URL url = new URL("http://localhost:8080/firstserver/files/hibernate.zip");
            URL url = new URL(urlPath);
            conn = (HttpURLConnection)url.openConnection();
            conn.setConnectTimeout(20 * 1000);
            is = conn.getInputStream();
            raf = new RandomAccessFile(destFile, "rw");
            raf.setLength(conn.getContentLength()); // 设置保存文件的大小
//            raf.setLength(conn.getInputStream().available());

            // 设置读入和写入的文件位置
            is.skip(startPos);
            raf.seek(startPos);

            currentLength = 0; // 当前已下载好的文件长度
            byte[] buffer = new byte[1024 * 1024];
            int len = 0;
            while(currentLength < length && -1 != (len = is.read(buffer))){
                if(!isRunning){
                    break;
                }
                if(currentLength + len > length){
                    raf.write(buffer, 0, (int)(length - currentLength));
                    currentLength = length;
                    notificationThread.notificationProgress(); // 通知进度线程来更新进度
                    return;
                }else{
                    raf.write(buffer, 0, len);
                    currentLength += len;
                    notificationThread.notificationProgress(); // 通知进度线程来更新进度
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally{
            try {
                is.close();
                raf.close();
                conn.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
    /**
     * 取消该线程下载
     * @author
     */
    public void cancelThread(){
        isRunning = false;
    }


}
