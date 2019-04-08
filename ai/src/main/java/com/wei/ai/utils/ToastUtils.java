package com.wei.ai.utils;

import android.content.Context;
import android.text.TextUtils;
import android.widget.Toast;

import com.wei.ai.BaseApplication;


public class ToastUtils {

    public static void showToast(String msg) {
        if (TextUtils.isEmpty(msg)) return;
        showToastDefault(BaseApplication.getInstance(), msg, Toast.LENGTH_SHORT);
    }

    /*public static void showToastCenter(Context context, CharSequence text, int duration) {
        try {
            LayoutInflater inflater = LayoutInflater.from(context);
            View v = inflater.inflate(R.layout.custom_dialog_loading, null);
            View pb = v.findViewById(R.id.pb_dialog);
            pb.setVisibility(View.GONE);
            TextView tv = (TextView) v.findViewById(R.id.custom_dialog_loading_tv_msg);
            tv.setText(!TextUtils.isEmpty(text)?text : "");
            Toast toast = new Toast(context.getApplicationContext());
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.setDuration(duration);
            toast.setView(v);
            toast.show();
        } catch (Exception e){

        }
    }*/

    public static void showToastDefault(Context context, CharSequence text, int duration) {
        Toast.makeText(context, text, duration).show();
        try {
            /*LayoutInflater inflater = LayoutInflater.from(context);
            TextView tv = (TextView)inflater.inflate(R.layout.custom_toast_layout, null);
            tv.setText(text);
            Toast toast = new Toast(context.getApplicationContext());
            toast.setDuration(Toast.LENGTH_SHORT);
            toast.setView(tv);
            toast.show();*/
        } catch (Exception e){

        }
    }



}
