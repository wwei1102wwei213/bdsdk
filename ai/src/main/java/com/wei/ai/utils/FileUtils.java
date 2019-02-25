package com.wei.ai.utils;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

public class FileUtils {


    public void writeLineFile(String filename, String[] content){
        try {
            FileOutputStream out = new FileOutputStream(filename);
            OutputStreamWriter outWriter = new OutputStreamWriter(out, "UTF-8");
            BufferedWriter bufWrite = new BufferedWriter(outWriter);
            for (int i = 0; i < content.length; i++) {
                bufWrite.write(content[i] + "\r\n");
            }
            bufWrite.close();
            outWriter.close();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("读取" + filename + "出错！");
        }
    }

}
