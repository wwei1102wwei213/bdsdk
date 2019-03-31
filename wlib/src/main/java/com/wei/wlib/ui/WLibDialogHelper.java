package com.wei.wlib.ui;

import android.app.Dialog;
import android.content.Context;
import android.view.WindowManager;
import android.widget.TextView;

import com.wei.wlib.R;

public class WLibDialogHelper {

    public static Dialog createProgressDialog(Context context, String msg, boolean cancel, boolean outside) {
        Dialog progressDialog = new Dialog(context, R.style.wlib_dialog_style_transparent);
        progressDialog.setContentView(R.layout.wlib_custom_dialog_loading);
        progressDialog.setCancelable(cancel);
        progressDialog.setCanceledOnTouchOutside(outside);
        //下面代码多余了，样式中已经定义了android:windowBackground
        //        progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        TextView textView = (TextView) progressDialog.findViewById(R.id.custom_dialog_loading_tv_msg);
        textView.setText(msg);
        return progressDialog;
    }

    public static Dialog createProgressDialog(Context context, String msg) {
        return createProgressDialog(context, msg, false, false);
    }

    public static void show(Dialog dialog) {
        if (dialog != null && !dialog.isShowing()) {
            try {
                dialog.show();
            } catch (WindowManager.BadTokenException e) {
                //Unable to add window -- token android.os.BinderProxy@2965cfdb is not valid; is your activity running?
            }
        }
    }

    public static void dismiss(Dialog dialog) {
        if (dialog != null && dialog.isShowing()) {
            try {
                dialog.dismiss();
            } catch (WindowManager.BadTokenException e) {
                //Unable to add window -- token android.os.BinderProxy@2965cfdb is not valid; is your activity running?
            }
        }
    }

    public static void update(Dialog dialog, String msg) {
        if (dialog != null) {
            TextView textView = (TextView) dialog.findViewById(R.id.custom_dialog_loading_tv_msg);
            textView.setText(msg);
        }
    }

}
