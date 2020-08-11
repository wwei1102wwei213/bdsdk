package com.wei.ai;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.wei.ai.db.InfoBean;

import java.io.File;
import java.io.FileInputStream;

public class InfoDetailDialog extends Dialog{

    private Context context;

    public InfoDetailDialog(@NonNull Context context) {
        this(context, R.style.dialog_base_style);
    }

    public InfoDetailDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
        this.context = context;
        initView();
    }

    private TextView tv;
    private ImageView iv;

    private void initView() {
        View v = LayoutInflater.from(context).inflate(R.layout.dialog_info_detail, null);
        setContentView(v);
        tv = v.findViewById(R.id.tv);
        iv = v.findViewById(R.id.iv);
        /*try {
            getWindow().setGravity(Gravity.CENTER);
        } catch (Exception e){
-+-+
        }*/

    }

    public void setData(InfoBean bean) {
        try {
            tv.setText("姓名："
                    + bean.getName() + "\n" + "性别：" + bean.getSex()
                    + "\n" + "出生日期："
                    + bean.getBirthday() + "\n" + "地址："
                    + bean.getAddress() + "\n" + "身份号码：" + bean.getCard()
                    + "\n" + "签发机关：" + bean.getDepartment() + "\n"
                    + "有效期限：" + bean.getDate());
            File temp = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/cardpic/"+bean.getCard()+".bmp");
            if (temp.exists()) {
                FileInputStream fis = new FileInputStream(temp);
                Bitmap bmp = BitmapFactory.decodeStream(fis);
                fis.close();
                iv.setImageBitmap(bmp);
            }
        } catch (Exception e){
            Log.e("ITEM_S", e.getMessage());
        }
    }



}
