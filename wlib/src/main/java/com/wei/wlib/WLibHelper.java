package com.wei.wlib;

import android.content.Context;
import android.text.TextUtils;
import android.widget.Toast;

public class WLibHelper {

    private Context context;

    private static WLibHelper wlibHelper;

    public static synchronized WLibHelper getInstance(Context c) {
        if (wlibHelper == null && c != null) {
            wlibHelper = new WLibHelper(c);
        }
        return wlibHelper;
    }

    private WLibHelper(Context context) {
        this.context = context.getApplicationContext();
    }

    public void showToast(String msg) {
        if (TextUtils.isEmpty(msg)) return;
        if (context==null) return;
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

}
