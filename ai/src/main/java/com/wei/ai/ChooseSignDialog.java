package com.wei.ai;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.wei.ai.utils.SPLongUtils;
import com.wei.ai.utils.ToastUtils;

import java.util.ArrayList;
import java.util.List;

public class ChooseSignDialog extends Dialog {

    private Context context;

    public ChooseSignDialog(@NonNull Context context) {
        this(context, R.style.dialog_base_style);
    }

    public ChooseSignDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
        this.context = context;
        initView();
    }

    private Spinner spinner;
    private Button btn;
    private void initView() {
        View v = LayoutInflater.from(context).inflate(R.layout.dialog_choose_sign, null);
        setContentView(v);
        spinner = v.findViewById(R.id.spinner);
        btn = v.findViewById(R.id.btn_sure);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveSign();
            }
        });
        data = new ArrayList<>();
        stringArrayAdapter = new ArrayAdapter<String>(context,R.layout.item_spinner,data);
        spinner.setAdapter(stringArrayAdapter);
    }

    private ArrayAdapter<String> stringArrayAdapter;
    private List<String> data;
    private void saveSign(){
        if (spinner.getSelectedItemPosition()==0) {
            ToastUtils.showToast("请设置签到单号");
            return;
        }
        SPLongUtils.saveString(context, "config_sign_table_num" ,data.get(spinner.getSelectedItemPosition()));
        ToastUtils.showToast("设置成功");
        dismiss();
        if (callback!=null) callback.onSettingFinished();

    }

    public void setData(List<String> list) {
        data.clear();
        data.add("请选择签到单号");
        data.addAll(list);
        ArrayAdapter<String> stringArrayAdapter=new ArrayAdapter<String>(context,R.layout.item_spinner,data);
        spinner.setAdapter(stringArrayAdapter);
    }

    public void setData(List<String> list, String current) {
        data.clear();
        data.add("请选择签到单号");
        data.addAll(list);
        stringArrayAdapter.notifyDataSetChanged();
        int select = 0;
        for (int i=1;i<data.size();i++) {
            if (current.equals(data.get(i))) {
                select = i;
                break;
            }
        }
        spinner.setSelection(select);
    }

    private SettingFinishedCallback callback;

    public void setCallback(SettingFinishedCallback callback) {
        this.callback = callback;
    }

    public interface SettingFinishedCallback {
        void onSettingFinished();
    }

}
