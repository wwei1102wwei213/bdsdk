package com.wei.ai.utils;

import android.os.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;

public class FileUtils {


    public static void writeLineFile(File file, String content){
        try {
            FileWriter writer = null;
            try {
                // 打开一个写文件器，构造函数中的第二个参数true表示以追加形式写文件
                writer = new FileWriter(file, true);
                writer.write(content+"\r\n");
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if(writer != null){
                        writer.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private static final String INFO_TXT = "/info.txt";
    private static final String CHECK_TXT = "/check.txt";
    public static void writeInfoData(String content) {
        try {
            File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+INFO_TXT);
            if (!file.exists()) {
                file.createNewFile();
            }
            writeLineFile(file, content);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void writeCheckData(String content) {
        try {
            File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+CHECK_TXT);
            if (!file.exists()) {
                file.createNewFile();
            }
            writeLineFile(file, content);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void clear() {
        try {
            File file1 = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+INFO_TXT);
            if (file1.exists()) {
                file1.delete();
            }
            File file2 = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+CHECK_TXT);
            if (file2.exists()) {
                file2.delete();
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 复制单个文件
     * @param oldPath String 原文件路径 如：c:/fqf.txt
     * @param newPath String 复制后路径 如：f:/fqf.txt
     * @return boolean
     */
    public static void copyFile(String oldPath, String newPath) {
        try {
            File newFile = new File(newPath);
            if (newFile.exists()) return;
            int bytesum = 0;
            int byteread = 0;
            File oldfile = new File(oldPath);
            if (oldfile.exists()) { //文件存在时
                InputStream inStream = new FileInputStream(oldPath); //读入原文件
                FileOutputStream fs = new FileOutputStream(newPath);
                byte[] buffer = new byte[1444];
                int length;
                while ( (byteread = inStream.read(buffer)) != -1) {
                    bytesum += byteread; //字节数 文件大小
                    System.out.println(bytesum);
                    fs.write(buffer, 0, byteread);
                }
                inStream.close();
            }
        }
        catch (Exception e) {
            System.out.println("复制单个文件操作出错");
            e.printStackTrace();

        }

    }


}
