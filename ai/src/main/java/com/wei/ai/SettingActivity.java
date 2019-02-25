package com.wei.ai;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.wei.ai.utils.SPLongUtils;

public class SettingActivity extends Activity{

    private int MATCH_SCORE;
    private long MAX_ONCE_CHECK_TIME;
    private int CHECK_SIZE;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        MAX_ONCE_CHECK_TIME = SPLongUtils.getInt(this, "mbad_once_check_time", 30000);
        CHECK_SIZE = SPLongUtils.getInt(this, "mbad_check_size", 80);
        MATCH_SCORE = SPLongUtils.getInt(this, "mbad_match_score", 55);
        initViews();
    }

    private EditText et_score, et_check_time, et_face_size;
    private Button btn;
    private void initViews() {
        et_score = findViewById(R.id.et_score);
        et_check_time = findViewById(R.id.et_check_time);
        et_face_size = findViewById(R.id.et_face_size);
        et_score.setText(MATCH_SCORE+"");
        et_check_time.setText(MAX_ONCE_CHECK_TIME/1000 + "");
        et_face_size.setText(CHECK_SIZE+"");
        btn = findViewById(R.id.btn_sure);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toEdit();
            }
        });
    }

    private void toEdit() {
        String scoreStr = et_score.getText().toString();
        String timeStr = et_check_time.getText().toString();
        String sizeStr = et_face_size.getText().toString();
        if (TextUtils.isEmpty(scoreStr) || TextUtils.isEmpty(timeStr) || TextUtils.isEmpty(sizeStr)) {
            showToast("数据不能为空");
            return;
        }
        try {
            int score = Integer.parseInt(scoreStr);
            int time = Integer.parseInt(timeStr);
            int size = Integer.parseInt(sizeStr);

            if (score==MATCH_SCORE&&size==CHECK_SIZE&&MAX_ONCE_CHECK_TIME==(time*1000)) {
                showToast("没有更改");
                return;
            }
            MAX_ONCE_CHECK_TIME = SPLongUtils.getInt(this, "mbad_once_check_time", 30000);
            CHECK_SIZE = SPLongUtils.getInt(this, "mbad_check_size", 80);
            MATCH_SCORE = SPLongUtils.getInt(this, "mbad_match_score", 55);
            if (score!=MATCH_SCORE) {
                SPLongUtils.saveInt(this, "mbad_match_score", score);
            }
            if (size!=CHECK_SIZE) {
                SPLongUtils.saveInt(this, "mbad_check_size", size);
            }
            if (MAX_ONCE_CHECK_TIME!=(time*1000)) {
                SPLongUtils.saveInt(this, "mbad_once_check_time", time*1000);
            }
            showToast("修改成功");
        } catch (Exception e){
            showToast("数据不符合规则");
        }
    }

    private void showToast(String msg){
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
