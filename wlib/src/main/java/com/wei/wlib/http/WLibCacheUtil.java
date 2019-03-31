package com.wei.wlib.http;

import android.content.Context;

import com.wei.wlib.util.WLibMd5;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * 缓存帮助类
 */
public class WLibCacheUtil {

    public static void saveJSONWithURL(Context context, String jsonStr, String url){
        saveObject(context, jsonStr, WLibMd5.getMD5(url));
    }

    public static String getJSONWithURL(Context context, String url){
        return (String) readObject(context, WLibMd5.getMD5(url));
    }

    public static boolean deleteWithURL(Context context, String url) {
        File data = context.getFileStreamPath(WLibMd5.getMD5(url));
        return data.exists() && data.delete();
    }

    public static boolean saveObject(Context context, Serializable ser, String name){

        if(context == null){
            return false;
        }

        FileOutputStream fos = null;
        ObjectOutputStream oos = null;
        try{
            fos = context.openFileOutput(name, Context.MODE_PRIVATE);
            oos = new ObjectOutputStream(fos);
            oos.writeObject(ser);
            oos.flush();
            return true;
        }catch(Exception e){
            e.printStackTrace();
            return false;
        }finally{
            try {
                if(oos != null) {
                    oos.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                if(fos != null) {
                    fos.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 读取对象
     */
    public static Serializable readObject(Context context, String name){
        if(!isExistDataCache(context, name))
            return null;
        FileInputStream fis = null;
        ObjectInputStream ois = null;
        try{
            fis = context.openFileInput(name);
            ois = new ObjectInputStream(fis);
            return (Serializable)ois.readObject();
        }catch(FileNotFoundException e){
            e.printStackTrace();
        }catch(Exception e){
            e.printStackTrace();
            //反序列化失败 - 删除缓存文件
            if(e instanceof InvalidClassException){
                File data = context.getFileStreamPath(name);
                data.delete();
            }
        }finally{
            try {
                if(ois != null) {
                    ois.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                if(fis != null) {
                    fis.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * 判断缓存是否存在
     * @param name
     * @return
     */
    private static boolean isExistDataCache(Context context, String name)
    {

        boolean exist = false;

        if(context == null || name == null){
            return false;
        }

        File data = context.getFileStreamPath(name);
        if(data.exists())
            exist = true;
        return exist;
    }


}
