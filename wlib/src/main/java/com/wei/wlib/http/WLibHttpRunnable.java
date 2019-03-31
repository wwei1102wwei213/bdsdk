package com.wei.wlib.http;

import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import com.wei.wlib.util.WLibLog;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;


public class WLibHttpRunnable implements Runnable{

    private static final String CONTENT_TYPE = "application/json";//application/octet-stream
    private  String HTTP_TYPE = "POST";
    private static final String CHARSET = "UTF-8";
    private Handler mHandler;
    private Map<String, String> map;
    private String url;

    public WLibHttpRunnable(Handler handler, String url, Map<String, String> map, String type) {
        mHandler = handler;
        this.map = map;
        this.url = url;
        HTTP_TYPE = type;
    }

    @Override
    public void run() {
        Message msg = mHandler.obtainMessage();
        try {
            String result = getData(url, map);
            if (TextUtils.isEmpty(result)){
                msg.what = 2;
            } else {
                msg.what = 1;
                msg.obj = result;
            }
        } catch (Exception e){
            WLibLog.e(e);
            msg.what = 2;
        }
        mHandler.sendMessage(msg);
        mHandler = null;
    }


    private String getData(String url, Map<String, String> map){
        StringBuilder sb = new StringBuilder();
        try {
            URL u = new URL(url);
            URLConnection conn = u.openConnection();
            HttpURLConnection connection = (HttpURLConnection) conn;
            connection.setRequestMethod(HTTP_TYPE);//"POST" "GET"
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(30000);
            connection.setUseCaches(false);
            connection.setInstanceFollowRedirects(true);
            connection.setRequestProperty("Charsert", CHARSET);
            /*connection.setRequestProperty("User-Agent", "books"+ Settings.getUA());
            connection.setRequestProperty("Cookie", CookieManger.getInstance().getCookies());
            connection.setRequestProperty(Honst.KEY_CHANNEL, CommonUtils.getChannelName());
            connection.setRequestProperty(Honst.KEY_SEX, SystemUtils.getSexStr());*/
            connection.setRequestProperty("Accept-Encoding", "gzip");
            connection.connect();
            DataOutputStream out = new DataOutputStream(connection.getOutputStream());
            if (map != null) {
                String params = handleParams(map);
                if (params!=null) {
                    out.write(params.getBytes());
                }
            }
            out.flush();
            out.close();

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            int code = connection.getResponseCode();
            if (200 == code){
                String temp = in.readLine();
                while (temp != null) {
                    sb.append(temp);
                    temp = in.readLine();
                }
            }
            in.close();

            connection.disconnect();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    private int bytesToInt(byte[] src, int offset) {
        int value;
        value = (int) ((src[offset] & 0xFF)
                | ((src[offset + 1] & 0xFF) << 8)
                | ((src[offset + 2] & 0xFF) << 16)
                | ((src[offset + 3] & 0xFF) << 24));
        return value;
    }


    private boolean fill(InputStream in, byte[] buffer) throws IOException {
        int length = buffer.length;
        int hasRead = 0;
        while (true) {
            int offset = hasRead;
            int count = length - hasRead;
            int currentRead = in.read(buffer, offset, count);
            if (currentRead >= 0) {
                hasRead += currentRead;
                if (hasRead == length) {
                    return true;
                }
            }
            if (currentRead == -1) {
                return false;
            }
        }
    }

    private String handleParams(Map<String, String> map) {
        StringBuilder sb = null;
        for (String key:map.keySet()) {
            if (sb==null) {
                sb = new StringBuilder(key + "=" + map.get(key));
            } else {
                sb.append("&").append(key).append("=").append(map.get(key));
            }
        }
        return sb==null?null:sb.toString();
    }

}


    /*chucked读取数据方式*/
            //InputStream inputStream = conn.getInputStream();
    //解析返回数据
            /*while (!Thread.currentThread().isInterrupted()) {
                byte[] seqBuffer = new byte[4];
                fill(inputStream, seqBuffer);
                int seq = bytesToInt(seqBuffer, 0);
                if (seq==0) break;
                //read pkg
                byte[] pbPkg = new byte[seq];
                fill(inputStream, pbPkg);
                sb.append(new String(pbPkg, CHARSET));
            }*/