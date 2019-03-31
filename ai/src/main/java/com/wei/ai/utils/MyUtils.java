package com.wei.ai.utils;

import android.graphics.Bitmap;
import android.util.Base64;

import java.io.ByteArrayOutputStream;

public class MyUtils {

    public static String getStringForBitmap(Bitmap bitmap) {
        try {
            //第一步:将Bitmap压缩至字节数组输出流ByteArrayOutputStream
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 80, byteArrayOutputStream);
            //第二步:利用Base64将字节数组输出流中的数据转换成字符串String
            byte[] byteArray = byteArrayOutputStream.toByteArray();
            return new String(Base64.encodeToString(byteArray, Base64.DEFAULT));
        } catch (Exception e){

        }
        return null;
    }



}
