package com.wei.ai;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.wei.ai.db.CheckDataBean;
import com.wei.ai.db.DBHelper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class SearchActivity extends Activity{

    private int page = 1;
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

            et_name = findViewById(R.id.et_name);
            et_card = findViewById(R.id.et_card_num);

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

    private long firstTime = 0;
    private long lastTime = 0;
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
        if (page<=1) {
            Toast.makeText(this, "当前已经是第一页", Toast.LENGTH_SHORT).show();
            return;
        }
        page--;

    }

    private void toNext() {

    }
}
