package com.wei.ai;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.wei.ai.db.CheckDataBean;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Administrator on 2019-02-25.
 */

public class SearchAdapter extends BaseAdapter{

    private Context context;
    private List<CheckDataBean> list;
    private SimpleDateFormat sdf;
    public SearchAdapter(Context context, List<CheckDataBean> list) {
        this.context = context;
        if (list==null) list = new ArrayList<>();
        this.list = list;
        sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public String getItem(int i) {
        return list.get(i).getCard_number();
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        ViewHolder vh = null;
        if (view==null) {
            view = LayoutInflater.from(context).inflate(R.layout.item_search_lv, viewGroup, false);
            vh = new ViewHolder();
            vh.name = view.findViewById(R.id.tv_name);
            vh.sex = view.findViewById(R.id.tv_sex);
            vh.card_num = view.findViewById(R.id.tv_card_num);
            vh.time = view.findViewById(R.id.tv_time);
            vh.detail = view.findViewById(R.id.tv_detail);
            vh.line = view.findViewById(R.id.line);
            view.setTag(vh);
        } else {
            vh = (ViewHolder) view.getTag();
        }
        CheckDataBean bean = list.get(i);
        try {
            vh.name.setText(bean.getName()==null?"":bean.getName());
            vh.card_num.setText(bean.getCard_number()==null?"":bean.getCard_number());
            vh.sex.setText(bean.getSex()==null?"":bean.getSex());
            try {
                vh.time.setText(sdf.format(new Date(bean.getCreate_time())));
            } catch (Exception e){

            }
            vh.detail.setText(bean.getStatus()==1?"成功":"失败");
            if (bean.getStatus()==1){
                vh.detail.setTextColor(Color.GREEN);
            } else {
                vh.detail.setTextColor(Color.RED);
            }
            vh.line.setVisibility(i==0?View.VISIBLE:View.GONE);
        } catch (Exception e){
            e.printStackTrace();
        }



        return view;
    }

    public void update(List<CheckDataBean> list) {
        if (list==null) list = new ArrayList<>();
        this.list = list;
        notifyDataSetChanged();
    }

    class ViewHolder {
        TextView name, sex, card_num, time, detail;
        View line;
    }

}
