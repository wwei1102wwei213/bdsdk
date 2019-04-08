package com.wei.ai;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.wei.ai.biz.HttpFlag;
import com.wei.ai.utils.SPLongUtils;
import com.wei.ai.utils.ToastUtils;

/**
 * Created by Administrator on 2019-04-07.
 */

public class SettingHostDialog extends Dialog {

    private Context context;

    public SettingHostDialog(@NonNull Context context) {
        this(context, R.style.dialog_base_style);
    }

    public SettingHostDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
        this.context = context;
        initView();
    }

    private EditText et;
    private Button btn;
    private void initView() {
        View v = LayoutInflater.from(context).inflate(R.layout.dialog_setting_host, null);
        setContentView(v);
        et = v.findViewById(R.id.et_host);
        v.findViewById(R.id.btn_sure).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveHost();
            }
        });

    }

    private void saveHost(){
        String mHost = et.getText().toString();
        if (TextUtils.isEmpty(mHost) || TextUtils.isEmpty(mHost.trim())) {
            ToastUtils.showToast("请设置服务地址");
            return;
        }
        SPLongUtils.saveString(context, "config_base_host" ,mHost);
        HttpFlag.changeBaseUrl(mHost);
        ToastUtils.showToast("设置成功");
        dismiss();
        if (callback!=null) callback.onSettingFinished(mHost);

    }

    public void setData(String current) {
        if (et!=null) et.setText(current);
    }

    private SettingFinishedCallback callback;

    public void setCallback(SettingFinishedCallback callback) {
        this.callback = callback;
    }

    public interface SettingFinishedCallback {
        void onSettingFinished(String host);
    }

}