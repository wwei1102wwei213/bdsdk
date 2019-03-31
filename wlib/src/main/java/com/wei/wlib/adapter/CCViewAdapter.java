package com.wei.wlib.adapter;

import android.view.View;

/**
 * <br/><br/>
 * Created by Yanhaifeng on 2017/6/19.
 */

public interface CCViewAdapter<T> {

    /**
     * Create the holder of item layout base on the type.
     *
     * @param type Item type
     * @return Holder with item layout.
     */
    CCAdapterHolder<T> createHolder(int type);

    CCAdapterHolder<T> getHolder(View convertView);

    int getPosition(View convertView);
}
