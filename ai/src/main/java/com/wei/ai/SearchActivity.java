package com.wei.ai;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.wei.ai.db.CheckDataBean;
import com.wei.ai.db.DBHelper;
import com.wei.ai.db.InfoBean;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class SearchActivity extends Activity{

    private int page = 0;
    private SearchAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        initViews();
        initData();
    }

    private Spinner spinner;
    private long[] dates;
    private EditText et_card;
    private TextView tv_page;
    private void initViews() {
        try {
            findViewById(R.id.btn_pre).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    toPre();
                }
            });
            findViewById(R.id.btn_next).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    toNext();
                }
            });
            findViewById(R.id.tv_back).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    finish();
                }
            });
            findViewById(R.id.btn_search).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    toSearch();
                }
            });

            et_card = findViewById(R.id.et_card_num);

            tv_page = findViewById(R.id.tv_page);

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            dates = new long[30];
            String[] args = new String[31];
            args[0] = "请选择时间";
            long now = System.currentTimeMillis();
            long zero = (now+TimeZone.getDefault().getRawOffset())/(1000*3600*24L)*(1000*3600*24L)- TimeZone.getDefault().getRawOffset();
            for (int i=0;i<dates.length;i++) {
                dates[i] = zero - i*24*3600*1000L;
                args[i+1] = sdf.format(new Date(dates[i]));
            }
            spinner= (Spinner) findViewById(R.id.spinner);
            //创建ArrayAdapter对象
            ArrayAdapter<String> stringArrayAdapter=new ArrayAdapter<String>(this,R.layout.item_spinner,args);
            spinner.setAdapter(stringArrayAdapter);
            ListView lv = findViewById(R.id.lv);
            adapter = new SearchAdapter(this, null);
            lv.setAdapter(adapter);
            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    clickItem(adapter.getItem(i));
                }
            });
        } catch (Exception e){
            e.printStackTrace();
        }


    }

    private void clickItem(String card){
        if (TextUtils.isEmpty(card)) return;
        InfoBean bean = DBHelper.getInstance().queryInfoData(BaseApplication.getInstance(), card);
        Log.e("ITEM_S", bean.toString());
        if (bean!=null) {
            InfoDetailDialog dialog = new InfoDetailDialog(this);
            dialog.setData(bean);
            dialog.show();
        }
    }

    private void initData() {
        try {
            List<CheckDataBean> beans = DBHelper.getInstance().queryCheckAll(BaseApplication.getInstance());
            if (beans!=null&&beans.size()>0) {
                adapter.update(beans);
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private void toPre() {
        try {
            if (page==0) {
                Toast.makeText(this, "当前已经是第一页", Toast.LENGTH_SHORT).show();
                return;
            }
            page--;
            List<CheckDataBean> list = DBHelper.getInstance().queryCheckObject(
                    BaseApplication.getInstance(), pos==0?0:dates[pos-1], null, cardNumber, page);
            tv_page.setText("第 "+(page+1)+" 页");
            adapter.update(list);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private void toNext() {
        try {
            if (adapter.getCount()<10) {
                Toast.makeText(this, "没有下一页了", Toast.LENGTH_SHORT).show();
                return;
            }
            page++;
            List<CheckDataBean> list = DBHelper.getInstance().queryCheckObject(
                    BaseApplication.getInstance(), pos==0?0:dates[pos-1], null, cardNumber, page);
            if (list==null||list.size()==0) {
                page--;
                Toast.makeText(this, "没有下一页了", Toast.LENGTH_SHORT).show();
                return;
            } else {
                tv_page.setText("第 "+(page+1)+" 页");
                adapter.update(list);
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private String cardNumber;
    private int pos;
    private void toSearch() {
        try {
            pos = spinner.getSelectedItemPosition();
            cardNumber = et_card.getText().toString();
            if (pos==0&&TextUtils.isEmpty(cardNumber)) {
                Toast.makeText(this, "请选择查询条件", Toast.LENGTH_SHORT).show();
                return;
            }
            Log.e("ITEM_S", cardNumber);
            page = 0;
            tv_page.setText("第 "+(page+1)+" 页");
            List<CheckDataBean> list = DBHelper.getInstance().queryCheckObject(
                    BaseApplication.getInstance(), pos==0?0:dates[pos-1], null, cardNumber, page);
            adapter.update(list);
        } catch (Exception e){

        }
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
