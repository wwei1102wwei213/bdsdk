package com.wei.wlib.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;

/**
 * md5加密工具
 */
public class WLibMd5 {

    /** 16进制的字符集 */
    private final static char [] hexDigitsChar = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    /**
     * MD5加密字符串
     *
     * @param source 源字符串
     *
     * @return 加密后的字符串
     *
     */
    public static String getMD5(String source) {
        String mdString = null;
        if (source != null) {
            try {
                mdString = getMD5(source.getBytes("UTF-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return mdString;
    }

    /**
     * MD5加密以byte数组表示的字符串
     *
     * @param source 源字节数组
     *
     * @return 加密后的字符串
     */
    public static String getMD5(byte[] source) {

        if(source == null) return null;

        String s = null;
        final int temp = 0xf;
        final int arraySize = 32;
        final int strLen = 16;
        final int offset = 4;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(source);
            byte [] tmp = md.digest();
            char [] str = new char[arraySize];
            int k = 0;
            for (int i = 0; i < strLen; i++) {
                byte byte0 = tmp[i];
                str[k++] = hexDigitsChar[byte0 >>> offset & temp];
                str[k++] = hexDigitsChar[byte0 & temp];
            }
            s = new String(str);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return s;
    }

    /**
     * 获取文件的md5值
     * @param file 目标文件
     * @return MD5字符串
     */
    public static String getFileMD5String(File file) {

        if (file == null || !file.isFile()) return null;
        MessageDigest digest;
        FileInputStream in = null;
        byte buffer[] = new byte[1024];
        int len;
        try {
            digest = MessageDigest.getInstance("MD5");
            in = new FileInputStream(file);
            while ((len = in.read(buffer, 0, 1024)) != -1) {
                digest.update(buffer, 0, len);
            }
            return bytesToHexString(digest.digest());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (null != in) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 转换字节数组为16进制字符串
     * @param bytes
     * @return
     */
    private static String bytesToHexString(byte[] bytes) {

        if (bytes == null || bytes.length <= 0) return null;
        StringBuilder stringBuilder = new StringBuilder("");
        for (int i = 0; i < bytes.length; i++) {
            int v = bytes[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }
}
