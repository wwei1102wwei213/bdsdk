package com.wei.wlib.widget.common;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.wei.wlib.R;


/**
 * Created by Administrator on 2017/11/24 0024.
 *
 * @author wwei
 */
public class CustomTabLayout extends LinearLayout {

    private View[] tabs;
    private TextView[] titles;
    private View[] lines;
    private int count;
    private int tabTextColor = R.color.wlib_white;
    private int tabTextSize = 12;
    private int selectPosition = -1;
    private TabItemClickListener callback;
    private boolean flag = false;

    public CustomTabLayout(Context context) {
        super(context);
    }

    public CustomTabLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomTabLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void init(int count, TabItemClickListener callback){
        this.count = count;
        tabs = new View[count];
        titles = new TextView[count];
        lines = new View[count];
        this.callback = callback;
    }

    public void setFlag(boolean flag) {
        this.flag = flag;
    }

    public void setTabTextColor(int tabTextColor) {
        this.tabTextColor = tabTextColor;
    }

    public void setTabTextSize(int tabTextSize) {
        this.tabTextSize = tabTextSize;
    }

    public void addTab(String tabTile, final int position, boolean selected){
        if (position>count-1) {

            return;
        }
        /*try {
            View v;
            if (flag){
                v = LayoutInflater.from(getContext()).inflate(R.layout.wlib_item_custom_tab_weight, this, false);
            } else {
                v = LayoutInflater.from(getContext()).inflate(R.layout.wlib_item_custom_tab, this, false);
            }
            TextView tv = (TextView) v.findViewById(R.id.tv);
            tv.setText(tabTile);
            tv.setTextSize(tabTextSize);
            View v_line = v.findViewById(R.id.v_line);
            tv.setSelected(selected);
            if (selected){
                selectPosition = position;
                v_line.setVisibility(VISIBLE);
            } else {
                v_line.setVisibility(INVISIBLE);
            }
            v.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (position!=selectPosition){
                        if (callback!=null){
                            setTabSelect(position);
                            callback.onTabItemClickListener(position);
                        } else {
                            setTabSelect(position);
                        }
                    }
                }
            });
            titles[position] = tv;
            lines[position] = v_line;
            tabs[position] = v;
        }catch (Exception e){
            ExceptionUtils.ExceptionSend(e);
        }*/
    }

    public void setTabSelect(int position){
        try {
            for (int i=0;i<titles.length;i++){
                if (i==position){
                    titles[i].setSelected(true);
                } else {
                    titles[i].setSelected(false);
                }
            }
            for (int i=0;i<lines.length;i++){
                if (i==position){
                    lines[i].setVisibility(VISIBLE);
                } else {
                    lines[i].setVisibility(INVISIBLE);
                }
            }
            selectPosition = position;

        }catch (Exception e){

        }
    }

    public void initTabView(){
        try {
            removeAllViews();
            for (int i=0;i<tabs.length;i++){
                addView(tabs[i]);
            }
        }catch (Exception e){
            
        }
    }

    public interface TabItemClickListener{
        void onTabItemClickListener(int position);
    }
}
