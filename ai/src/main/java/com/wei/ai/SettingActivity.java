package com.wei.ai;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.aip.utils.PreferencesUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.wei.ai.biz.HttpFlag;
import com.wei.ai.utils.SPLongUtils;
import com.wei.wlib.http.WLibHttpListener;

import java.util.List;

public class SettingActivity extends Activity implements WLibHttpListener{

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
//        Factory.resp(this, HttpFlag.FLAG_GET_ATTENDANCE_NUM, null).post(null);
    }

    private EditText et_score, et_check_time, et_face_size;
    private Button btn;
    private TextView tv_sign_num, tv_host;
    private String signNum;
    private String mHost;
    private void initViews() {
        ((TextView) findViewById(R.id.tv_key)).setText(PreferencesUtil.getString("activate_key", ""));
        et_score = findViewById(R.id.et_score);
        et_check_time = findViewById(R.id.et_check_time);
        et_face_size = findViewById(R.id.et_face_size);
        et_score.setText(MATCH_SCORE+"");
        et_check_time.setText(MAX_ONCE_CHECK_TIME/1000 + "");
        et_face_size.setText(CHECK_SIZE+"");
        tv_sign_num = findViewById(R.id.tv_sign_num);
        tv_host = findViewById(R.id.tv_host);
        btn = findViewById(R.id.btn_sure);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toEdit();
            }
        });
        findViewById(R.id.btn_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        /*signNum = SPLongUtils.getString(this, "config_sign_table_num", "");
        tv_sign_num.setText(signNum);
        tv_sign_num.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSignDialog();
            }
        });*/
        mHost = SPLongUtils.getString(this, "config_base_host", "");
        tv_host.setText(mHost);
        tv_host.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showHostDialog();
            }
        });
    }
    private ChooseSignDialog dialog;
    private List<String> signList = null;
    private boolean signChange = false;
    private void showSignDialog() {
        if (signList==null||signList.size()==0) {
            showToast("获取签到单失败");
        } else {
            if (dialog==null) {
                dialog = new ChooseSignDialog(this);
                dialog.setCancelable(true);
                dialog.setCallback(new ChooseSignDialog.SettingFinishedCallback() {
                    @Override
                    public void onSettingFinished() {
                        signChange = true;
                        signNum = SPLongUtils.getString(SettingActivity.this, "config_sign_table_num", "");
                        tv_sign_num.setText(signNum);
                    }
                });
            }
            dialog.setData(signList, signNum);
            dialog.show();
        }
    }

    private SettingHostDialog mSettingDialog;
    private void showHostDialog() {
        if (mSettingDialog==null) {
            mSettingDialog = new SettingHostDialog(this);
            mSettingDialog.setCancelable(true);
            mSettingDialog.setCallback(new SettingHostDialog.SettingFinishedCallback() {
                @Override
                public void onSettingFinished(String host) {
                    mHost = host;
                    tv_host.setText(host);
                }
            });
        }
        mSettingDialog.setData(mHost);
        mSettingDialog.show();
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

            if (score==MATCH_SCORE&&size==CHECK_SIZE&&MAX_ONCE_CHECK_TIME==(time*1000)&&!signChange) {
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
            finish();
        } catch (Exception e){
            showToast("数据不符合规则");
        }
    }

    @Override
    public void handleResp(Object formatData, int flag, Object tag, String response, String hint) {
        if (flag == HttpFlag.FLAG_GET_ATTENDANCE_NUM) {
            try {
               signList = new Gson().fromJson(formatData.toString(), new TypeToken<List<String>>(){}.getType());
            } catch (Exception e){
                showToast("获取签到单失败");
            }
        }
    }

    @Override
    public void handleLoading(int flag, Object tag, boolean isShow) {

    }

    @Override
    public void handleError(int flag, Object tag, int errorType, String response, String hint) {
        if (flag == HttpFlag.FLAG_GET_ATTENDANCE_NUM) {
            if (!TextUtils.isEmpty(hint)) {
                showToast(hint);
            } else {
                showToast("获取签到单失败");
            }
        }
    }

    @Override
    public void handleAfter(int flag, Object tag) {

    }

    private void showToast(String msg){
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    //输入法是否收起
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        try {
            if (ev.getAction() == MotionEvent.ACTION_DOWN) {
                View v = getCurrentFocus();
                if (isShouldHideKeyboard(v, ev)) {
                    hideKeyboard(v.getWindowToken());
                }
            }
        } catch (Exception e) {

        }
        return super.dispatchTouchEvent(ev);
    }

    /**
     * 根据EditText所在坐标和用户点击的坐标相对比，来判断是否隐藏键盘，因为当用户点击EditText时则不能隐藏
     *
     * @param v
     * @param event
     * @return
     */
    private boolean isShouldHideKeyboard(View v, MotionEvent event) {
        if (v != null && (v instanceof EditText)) {
            int[] l = {0, 0};
            v.getLocationInWindow(l);
            int left = l[0],
                    top = l[1],
                    bottom = top + v.getHeight(),
                    right = left + v.getWidth();
            if (event.getX() > left && event.getX() < right
                    && event.getY() > top && event.getY() < bottom) {
                // 点击EditText的事件，忽略它。
                return false;
            } else {
                return true;
            }
        }
        // 如果焦点不是EditText则忽略，这个发生在视图刚绘制完，第一个焦点不在EditView上，和用户用轨迹球选择其他的焦点
        return false;
    }

    /**
     * 获取InputMethodManager，隐藏软键盘
     * @param token
     */
    private void hideKeyboard(IBinder token) {
        try {
            if (token != null) {
                InputMethodManager im = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                im.hideSoftInputFromWindow(token, InputMethodManager.HIDE_NOT_ALWAYS);
            }

        } catch (Exception e){

        }
    }
}
