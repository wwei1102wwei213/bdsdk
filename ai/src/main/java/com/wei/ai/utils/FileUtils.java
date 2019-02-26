package com.wei.ai.utils;

import android.os.Environment;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

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

}
