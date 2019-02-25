package com.wei.ai.utils;

import android.os.Environment;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

public class FileUtils {


    public static void writeLineFile(File file, String content){
        try {
            FileWriter writer = new FileWriter(file);
            BufferedWriter out = new BufferedWriter(writer);
            out.write(content+"\r\n"); // \r\n即为换行
            out.flush();
            writer.close();
            out.close();
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

}
