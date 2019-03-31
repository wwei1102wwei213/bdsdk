package com.wei.wlib.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.wei.wlib.R;

import java.util.List;

public abstract class CCRecyclerAdapter<T> extends RecyclerView.Adapter<CCRecyclerViewHolder> implements CCViewAdapter<T> {

    private Context mContext;
    private List<T> mAdapterContent;
    private LayoutInflater mLayoutInflater;

    private boolean mIsLooped;
    private int mLoopFactor;

    public CCRecyclerAdapter(Context context, List<T> adapterContent) {
        mContext = context;
        if (adapterContent instanceof CCAdapterHandler) {
            ((CCAdapterHandler) adapterContent).setContentObserver(new CCAdapterHandler.ContentObserver() {
                @Override
                public void onContentChanged() {
                    notifyDataSetChanged();
                }
            });
        }
        mAdapterContent = adapterContent;
        mLayoutInflater = LayoutInflater.from(context);
    }

    public void setLooped(boolean isLooped) {
        setLooped(isLooped, -1);
    }

    public void setLooped(boolean isLooped, int factor) {
        mIsLooped = isLooped;
        mLoopFactor = factor;
    }

    public int getRealPositionInLoop(int loopedPosition) {
        return loopedPosition % mAdapterContent.size();
    }

    public int getLoopedInitializePosition() {
        int halfSize = getLoopedSize() / 2;
        return halfSize - halfSize % mAdapterContent.size();
    }

    public Context getContext() {
        return mContext;
    }

    @Override
    public CCRecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        CCAdapterHolder<T> holder = createHolder(viewType);
        View convertView = mLayoutInflater.inflate(holder.getResource(), parent, false);
        holder.initializeView(convertView);
        return new CCRecyclerViewHolder(convertView, holder);
    }

    @Override
    public void onBindViewHolder(CCRecyclerViewHolder holder, int position) {
        position %= mAdapterContent.size();
        View itemView = holder.itemView;
        CCAdapterHolder adapterHolder = holder.getAdapterHolder();
        itemView.setTag(R.string.cc_adapter_holder_tag_key, adapterHolder);
        itemView.setTag(R.string.cc_adapter_holder_index_tag_key, position);
        adapterHolder.updateView(mAdapterContent.get(position), position);
    }

    @Override
    public int getItemCount() {
        if (mAdapterContent == null) {
            return 0;
        }
        return getLoopedSize();
    }

    private int getLoopedSize() {
        int size = mAdapterContent.size();
        if (mIsLooped) {
            if (mLoopFactor == -1) {
                size = Integer.MAX_VALUE;
            } else {
                size *= Math.max(0, mLoopFactor);
            }
        }
        return size;
    }

    @Override
    public CCAdapterHolder<T> getHolder(View convertView) {
        return (CCAdapterHolder<T>) convertView.getTag(R.string.cc_adapter_holder_tag_key);
    }

    @Override
    public int getPosition(View convertView) {
        return (Integer) convertView.getTag(R.string.cc_adapter_holder_index_tag_key);
    }
}
