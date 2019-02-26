package com.wei.ai;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.media.Image;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.wei.ai.db.InfoBean;

public class InfoDetailDialog extends Dialog{

    private Context context;

    public InfoDetailDialog(@NonNull Context context) {
        this(context, R.style.dialog_base_style);
    }

    public InfoDetailDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
        this.context = context;
    }

    private TextView tv;
    private ImageView iv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_info_detail);
        tv = findViewById(R.id.tv);
        iv = findViewById(R.id.iv);
    }

    private void setData(InfoBean bean) {
        try {
            if (!TextUtils.isEmpty(bean.getHead())) {
                Log.e("IDD", "头像不为空");
            } else {
                Log.e("IDD", "头像为空");
            }
            tv.setText("姓名："
                    + bean.getName() + "\n" + "性别：" + bean.getSex()
                    + "\n" + "出生日期："
                    + bean.getBirthday() + "\n" + "地址："
                    + bean.getAddress() + "\n" + "身份号码：" + bean.getCard()
                    + "\n" + "签发机关：" + bean.getDepartment() + "\n"
                    + "有效期限：" + bean.getDate());
        } catch (Exception e){

        }
    }



}
