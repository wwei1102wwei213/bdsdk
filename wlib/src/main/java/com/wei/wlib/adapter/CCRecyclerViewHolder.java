package com.wei.wlib.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * <br/><br/>
 * Created by Yanhaifeng on 2017/6/19.
 */

class CCRecyclerViewHolder<T> extends RecyclerView.ViewHolder {

    private CCAdapterHolder<T> mAdapterHolder;

    public CCRecyclerViewHolder(View itemView, CCAdapterHolder<T> holder) {
        super(itemView);
        mAdapterHolder = holder;
    }

    public CCAdapterHolder<T> getAdapterHolder() {
        return mAdapterHolder;
    }
}
