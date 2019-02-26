package com.wei.ai;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.wei.ai.db.CheckDataBean;
import com.wei.ai.db.DBHelper;

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
    private EditText et_name, et_card;
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

            et_name = findViewById(R.id.et_name);
            et_card = findViewById(R.id.et_card_num);

            tv_page = findViewById(R.id.tv_page);

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            dates = new long[30];
            String[] args = new String[31];
            args[0] = "请选择时间";
            long zero=System.currentTimeMillis()/(1000*3600*24)*(1000*3600*24)- TimeZone.getDefault().getRawOffset();
            for (int i=0;i<dates.length;i++) {
                dates[i] = zero - i*24*3600*1000L;
                args[i+1] = sdf.format(new Date(dates[i]));
//            Log.e("spinner", args[i]+","+dates[i]);
            }
            spinner= (Spinner) findViewById(R.id.spinner);
            //创建ArrayAdapter对象
            ArrayAdapter<String> stringArrayAdapter=new ArrayAdapter<String>(this,R.layout.item_spinner,args);
            spinner.setAdapter(stringArrayAdapter);
            ListView lv = findViewById(R.id.lv);
            adapter = new SearchAdapter(this, null);
            lv.setAdapter(adapter);
        } catch (Exception e){
            e.printStackTrace();
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
                    BaseApplication.getInstance(), pos==0?0:dates[pos-1], name, cardNumber, page);
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
                    BaseApplication.getInstance(), pos==0?0:dates[pos-1], name, cardNumber, page);
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

    private String name, cardNumber;
    private int pos;
    private void toSearch() {
        try {
            pos = spinner.getSelectedItemPosition();
            name = et_name.getText().toString();
            cardNumber = et_card.getText().toString();
            if (pos==0&&TextUtils.isEmpty(name)&&TextUtils.isEmpty(cardNumber)) {
                Toast.makeText(this, "请选择查询条件", Toast.LENGTH_SHORT).show();
                return;
            }
            page = 0;
            tv_page.setText("第 "+(page+1)+" 页");
            List<CheckDataBean> list = DBHelper.getInstance().queryCheckObject(
                    BaseApplication.getInstance(), pos==0?0:dates[pos-1], name, cardNumber, page);
            adapter.update(list);
        } catch (Exception e){

        }
    }

    private void getList() {
        try {
            tv_page.setText("第 "+(page+1)+" 页");
            List<CheckDataBean> list = DBHelper.getInstance().queryCheckObject(
                    BaseApplication.getInstance(), pos==0?0:dates[pos-1], name, cardNumber, page);

        } catch (Exception e){

        }

    }
}
